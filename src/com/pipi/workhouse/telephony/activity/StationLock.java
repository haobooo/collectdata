package com.pipi.workhouse.telephony.activity;

import java.util.Collections;
import java.util.Comparator;

import org.jraf.android.backport.switchwidget.Switch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pipi.workhouse.telephony.R;
import com.pipi.workhouse.telephony.adapter.LocationAdapter;
import com.pipi.workhouse.telephony.common.Constants;
import com.pipi.workhouse.telephony.service.StationLockService;
import com.pipi.workhouse.telephony.utils.CellLocationWrapper;
import com.pipi.workhouse.telephony.utils.Utils;
import com.pipi.workhouse.telephony.widget.ViewPageIndicator;


public class StationLock extends Activity {
	private static final String TAG = "StationLock";
	
	private static final int BASE_DECIMAL = 0;
	private static final int BASE_HEXA_DECIMAL = 1;
	private static final String DECIMAL_INPUT_STRING = "0123456789";
	private static final String HEXADECIMAL_INPUT_STRING = "0123456789abcdefABCDEF";
	
	private static final DigitsKeyListener decimalListener 
			= DigitsKeyListener.getInstance(DECIMAL_INPUT_STRING);
	
	private static final DigitsKeyListener hexaDecimalListener 
	= DigitsKeyListener.getInstance(HEXADECIMAL_INPUT_STRING);
	
	private View stationGsmContainer;
	private View stationCdmaContainer;
	private RadioGroup mBaseContainer;
	private RadioButton mDecimal;
	private RadioButton mHexaDecimal;
	private EditText stationLacView;
	private EditText stationCidView;
	private EditText stationBsdView;
	private EditText stationSidView;
	private EditText stationNidView;
	private Switch lockInView;
	private Switch lockOutView;
	private Switch alarmVibrateView;
	private Switch alarmRingView;
	private ListView mListView;
	private CheckBox mStartServiceView;
	private ViewPager mViewPager;
	private ViewPageIndicator mIndicator;
	private MyLocationAdapter mAdapter;
	private int mBase = BASE_DECIMAL;
	
	private RadioGroup.OnCheckedChangeListener mBaseListener = new RadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			
			if (group.getCheckedRadioButtonId() == R.id.decimal) {
				mBase = BASE_DECIMAL;
			} else {
				mBase = BASE_HEXA_DECIMAL;
			}
			
			resolveInputListener();
		}
		
	};
	
	private OnCheckedChangeListener mListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton view, boolean checked) {
			CellLocationWrapper location = (CellLocationWrapper) view.getTag();
			
			int checkMode = location.getLockMode();
			int alarmMode = location.getAlarmMode();
			
			switch(view.getId()) {
			case R.id.station_lock_in:
				if (checked) {
					checkMode |= Constants.LOCK_MODE_IN;
				} else {
					checkMode &= ~Constants.LOCK_MODE_IN;
				}
				break;
			case R.id.station_lock_out:
				if (checked) {
					checkMode |= Constants.LOCK_MODE_OUT;
				} else {
					checkMode &= ~Constants.LOCK_MODE_OUT;
				}
				break;
			case R.id.station_lock_alarm_vibrate:
				if (checked) {
					alarmMode |= Constants.LOCK_ALARM_MODE_VIB;
				} else {
					alarmMode &= ~Constants.LOCK_ALARM_MODE_VIB;
				}
				break;
			case R.id.station_lock_alarm_ring:
				if (checked) {
					alarmMode |= Constants.LOCK_ALARM_MODE_RING;
				} else {
					alarmMode &= ~Constants.LOCK_ALARM_MODE_RING;
				}
				break;
			}
			
			// Save the location.
			location.setLockMode(checkMode);
			location.setAlarmMode(alarmMode);
			saveLockMode(location);
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.station_lock);
		
		initViews();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		restoreCellLock();
	}
	
	public void onBack(View view) {
		finish();
	}
	
	private void restoreCellLock() {
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOCK_MODE_FILE, MODE_PRIVATE);
		String lockCell = sharedPreferences.getString(Constants.LOCK_CELL_KEY, "");
		
		if (lockCell.length() > 0) {
			String[] cells = lockCell.split(","); 
			int size = cells.length;
			for (int i = 0; i < size; i++) {
				if (TextUtils.isEmpty(cells[i])) continue;
				
				String[] tmpArray = cells[i].split("-");
				if (tmpArray.length == 2) {
					GsmCellLocation loc = new GsmCellLocation();
					
					int lac = Integer.parseInt(tmpArray[0]);
					int cid = Integer.parseInt(tmpArray[1]);
					loc.setLacAndCid(lac, cid);
					
					// Get the celllocation's lock mode.
					int lockMode = sharedPreferences.getInt(cells[i], 0);
					int alarmMode = Utils.getPreference(this, Constants.LOCK_ALARM_MODE_FILE, cells[i]);
					if (Constants.IS_DEBUG) Log.d(TAG, "[restoreCellLock] key=" + cells[i] + "; lockMode=" + lockMode + "; alarmMode=" + alarmMode);
					if (lockMode > 0) {
						CellLocationWrapper location = new CellLocationWrapper(loc);
						location.setLockMode(lockMode);
						location.setAlarmMode(alarmMode);
						
						mAdapter.addItem(location);
					}
				} else {
					CdmaCellLocation loc = new CdmaCellLocation();
					
					int bsd = Integer.parseInt(tmpArray[0]);
					int sid = Integer.parseInt(tmpArray[1]);
					int nid = Integer.parseInt(tmpArray[2]);
					loc.setCellLocationData(bsd, Integer.MAX_VALUE, Integer.MAX_VALUE, sid, nid);
					
					// Get the celllocation's lock mode.
					int lockMode = sharedPreferences.getInt(cells[i], 0);
					int alarmMode = Utils.getPreference(this, Constants.LOCK_ALARM_MODE_FILE, cells[i]);
					if (lockMode > 0) {
						CellLocationWrapper location = new CellLocationWrapper(loc);
						location.setLockMode(lockMode);
						location.setAlarmMode(alarmMode);
						
						mAdapter.addItem(location);
					}
				}
			}
		}
	}
	
	private void initViews() {
		// Initiate ViewPageIndicator.
		mIndicator = (ViewPageIndicator) findViewById(R.id.view_pager_indicator);
		mIndicator.setPointCount(2);
		
		
		// Initiate ViewPager.
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mViewPager.setAdapter(new PageAdapter(mViewPager));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}

			@Override
			public void onPageSelected(int position) {
				mIndicator.setPoint(position);
			}
			
		});
				
		mStartServiceView = (CheckBox) findViewById(R.id.title_right_button);
		mStartServiceView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton view, boolean checked) {
				if (checked) {
					AlertStartService();
				} else {
					AlertStopService();
				}
			}
			
		});
	}
	
	public void addLock(View view) {
		CellLocationWrapper location;
		int lockMode = 0;
		int alarmMode = 0;
		
		if (lockInView.isChecked()) {
			lockMode |= Constants.LOCK_MODE_IN;
		} 
		if (lockOutView.isChecked()) {
			lockMode |= Constants.LOCK_MODE_OUT;
		}
		
		if (alarmVibrateView.isChecked()) {
			alarmMode |= Constants.LOCK_ALARM_MODE_VIB;
		}
		
		if (alarmRingView.isChecked()) {
			alarmMode |= Constants.LOCK_ALARM_MODE_RING;
		}
		
		if (lockMode == 0) {
			Toast.makeText(this, "Set the lockMode first!", Toast.LENGTH_SHORT).show();
			return ;
		}
		
		if (alarmMode == 0) {
			Toast.makeText(this, "Set the alarm Mode first!", Toast.LENGTH_SHORT).show();
			return ;
		}
		
		if (stationGsmContainer.getVisibility() == View.VISIBLE) {
			try {
				int radix = 10;
				if (mBase == BASE_DECIMAL) {
					radix = 10;
				} else {
					radix = 16;
				}
				int lac = Integer.parseInt(stationLacView.getText().toString(), radix);
				int cid = Integer.parseInt(stationCidView.getText().toString(), radix);
				GsmCellLocation gsmLoc = new GsmCellLocation();
				gsmLoc.setLacAndCid(lac, cid);
				
				location = new CellLocationWrapper(gsmLoc);
				location.setLockMode(lockMode);
				location.setAlarmMode(alarmMode);
				mAdapter.addItem(location);
				saveLockMode(location);
			} catch (Exception e) {
				
			}
		} else if (stationCdmaContainer.getVisibility() == View.VISIBLE) {
			try {
				int radix = 10;
				if (mBase == BASE_DECIMAL) {
					radix = 10;
				} else {
					radix = 16;
				}
				
				int bsd = Integer.parseInt(stationBsdView.getText().toString(), radix);
				int sid = Integer.parseInt(stationSidView.getText().toString(), radix);
				int nid = Integer.parseInt(stationNidView.getText().toString(), radix);
				
				CdmaCellLocation cdmaLoc = new CdmaCellLocation();
				cdmaLoc.setCellLocationData(bsd, Integer.MAX_VALUE, Integer.MAX_VALUE, sid, nid);
				location = new CellLocationWrapper(cdmaLoc);
				location.setLockMode(lockMode);
				location.setAlarmMode(alarmMode);
				mAdapter.addItem(location);
				saveLockMode(location);
			} catch(Exception e) {
				
			}
		}
		
		// Clear the edit box.
		lockInView.setChecked(false);
		lockOutView.setChecked(false);
		alarmVibrateView.setChecked(false);
		alarmRingView.setChecked(false);
		stationLacView.setText("");
		stationCidView.setText("");
		stationBsdView.setText("");
		stationSidView.setText("");
		stationNidView.setText("");
	}
	
	private void saveLockMode(CellLocationWrapper location) {
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOCK_MODE_FILE, MODE_PRIVATE);
		Editor edit = sharedPreferences.edit();
		String lockCell = sharedPreferences.getString(Constants.LOCK_CELL_KEY, "");
		
		String key;
		int value = location.getLockMode();
		int alarmMode = location.getAlarmMode();
		
		if (location.isGsm()) {
			key = location.getLac() + "-" + location.getCid();
		} else {
			key = location.getBaseStationId() + "-" + location.getSystemId() + "-" + location.getNetworkId();
		}
		
		if (Constants.IS_DEBUG) Log.d(TAG, "[saveLockMode] key=" + key + "; lockMode=" + value + "; alarmMode=" + alarmMode);
		
		if (location.getLockMode() == 0) {
			edit.remove(key);
			Utils.removePreference(this, Constants.LOCK_ALARM_MODE_FILE, key);
			
			if (lockCell.contains(key)) {
				lockCell.replace(key, "");
				
				edit.putString(Constants.LOCK_CELL_KEY, lockCell);
			}
		} else {
			edit.putInt(key, value);
			
			if (!lockCell.contains(key)) {
				if (lockCell.length() > 0) {
					lockCell = lockCell + "," + key;
				} else {
					lockCell = key;
				}
				edit.putString(Constants.LOCK_CELL_KEY, lockCell);
			}
			
			Utils.savePreference(this, Constants.LOCK_ALARM_MODE_FILE, key, alarmMode);
		}
		
		edit.apply();
	}
	
	private class MyLocationAdapter extends LocationAdapter {

		public MyLocationAdapter(Context context) {
			super(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.station_lock_list_item, null);
			}
			
			View gsmView = convertView.findViewById(R.id.station_gsm);
			View cdmaView = convertView.findViewById(R.id.station_cdma);
			//TextView cellView = (TextView) convertView.findViewById(android.R.id.text1);
			TextView lacView = (TextView)convertView.findViewById(R.id.station_lac);
			TextView cidView = (TextView)convertView.findViewById(R.id.station_cid);
			
			TextView bsdView = (TextView)convertView.findViewById(R.id.station_bsd);
			TextView sidView = (TextView)convertView.findViewById(R.id.station_sid);
			TextView nidView = (TextView)convertView.findViewById(R.id.station_nid);
			
			Switch lockInView = (Switch) convertView.findViewById(R.id.station_lock_in);
			Switch lockOutView = (Switch) convertView.findViewById(R.id.station_lock_out);
			Switch alarmVibrateView = (Switch) convertView.findViewById(R.id.station_lock_alarm_vibrate);
			Switch alarmRingView = (Switch) convertView.findViewById(R.id.station_lock_alarm_ring);
			
			String cellLocation = "";
			CellLocationWrapper location = (CellLocationWrapper) getItem(position);
			if (location.isGsm()) {
				cdmaView.setVisibility(View.GONE);
				gsmView.setVisibility(View.VISIBLE);
				
				lacView.setText(String.valueOf(location.getLac()));
				cidView.setText(String.valueOf(location.getCid()));
				//cellLocation = mContext.getResources().getString(R.string.cell_location, location.getLac(), location.getCid(), location.getPsc());
			} else {
				cdmaView.setVisibility(View.VISIBLE);
				gsmView.setVisibility(View.GONE);
				
				bsdView.setText(String.valueOf(location.getBaseStationId()));
				sidView.setText(String.valueOf(location.getSystemId()));
				nidView.setText(String.valueOf(location.getNetworkId()));
				//cellLocation = mContext.getResources().getString(R.string.cell_location_cdma, location.getBaseStationId(), location.getSystemId(), location.getNetworkId());
			}
			
			lockInView.setChecked((location.getLockMode()&Constants.LOCK_MODE_IN) == Constants.LOCK_MODE_IN);
			lockOutView.setChecked((location.getLockMode()&Constants.LOCK_MODE_OUT) == Constants.LOCK_MODE_OUT);
			alarmVibrateView.setChecked((location.getAlarmMode()&Constants.LOCK_ALARM_MODE_VIB) == Constants.LOCK_ALARM_MODE_VIB);
			alarmRingView.setChecked((location.getAlarmMode()&Constants.LOCK_ALARM_MODE_RING) == Constants.LOCK_ALARM_MODE_RING);
			
			lockInView.setTag(location);
			lockOutView.setTag(location);
			alarmVibrateView.setTag(location);
			alarmRingView.setTag(location);
			lockInView.setOnCheckedChangeListener(mListener);
			lockOutView.setOnCheckedChangeListener(mListener);
			alarmVibrateView.setOnCheckedChangeListener(mListener);
			alarmRingView.setOnCheckedChangeListener(mListener);
			
			if (position % 2 == 1) {
				convertView.setBackgroundResource(R.drawable.rectangle_background_grey);
			} else {
				convertView.setBackgroundResource(R.drawable.rectangle_background_white);
			}
			
			return convertView;
		}
		
		public void sort() {
			Collections.sort(mLocation, mComparator);
		}
		
		private Comparator<CellLocation> mComparator = new Comparator<CellLocation>() {

			@Override
			public int compare(CellLocation paramT1,
					CellLocation paramT2) {
				CellLocationWrapper obj1 = (CellLocationWrapper)paramT1;
				CellLocationWrapper obj2 = (CellLocationWrapper)paramT2;
				
				if (obj1.getTime() < obj2.getTime()) {
					return 1;
				} else if(obj1.getTime() == obj2.getTime()) {
					return 0;
				} else {
					return -1;
				}
				
			}
			
		};
		
		private OnClickListener mOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				((CheckedTextView)view).toggle();
				
				CellLocationWrapper location = (CellLocationWrapper) view.getTag();
				
				int checkMode = location.getLockMode();
				boolean checked = ((CheckedTextView)view).isChecked();
				
				switch(view.getId()) {
				case R.id.station_lock_in:
					if (checked) {
						checkMode |= Constants.LOCK_MODE_IN;
					} else {
						checkMode &= ~Constants.LOCK_MODE_IN;
					}
					break;
				case R.id.station_lock_out:
					if (checked) {
						checkMode |= Constants.LOCK_MODE_OUT;
					} else {
						checkMode &= ~Constants.LOCK_MODE_OUT;
					}
					break;
				}
				
				//FIXME: if the checkMode is ZERO, the item should be removed from the list.
				
				// Save the location.
				location.setLockMode(checkMode);
				saveLockMode(location);
			}
			
		};
		
	}
	
	private void AlertStartService() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.bs_lock_and_alarm_title));
		builder.setMessage(getString(R.string.station_lock_start_service));
		builder.setPositiveButton(getString(R.string.start), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent service = new Intent(StationLock.this, StationLockService.class);
				startService(service);
			}
			
		});
		
		builder.create().show();
	}
	
	private void AlertStopService() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.bs_lock_and_alarm_title));
		builder.setMessage(getString(R.string.station_lock_stop_service));
		builder.setPositiveButton(getString(R.string.stop), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent service = new Intent(StationLock.this, StationLockService.class);
				stopService(service);
			}
			
		});
		
		builder.create().show();
	}
	
	private void resolveInputListener() {
		if (BASE_DECIMAL == mBase) {
			stationLacView.setKeyListener(decimalListener);
			stationCidView.setKeyListener(decimalListener);
			stationBsdView.setKeyListener(decimalListener);
			stationSidView.setKeyListener(decimalListener);
			stationNidView.setKeyListener(decimalListener);
		} else {
			stationLacView.setKeyListener(hexaDecimalListener);
			stationCidView.setKeyListener(hexaDecimalListener);
			stationBsdView.setKeyListener(hexaDecimalListener);
			stationSidView.setKeyListener(hexaDecimalListener);
			stationNidView.setKeyListener(hexaDecimalListener);
		}
	}
	
	class PageAdapter extends PagerAdapter {
		private View mView1;
		private View mView2;
		
		public PageAdapter(ViewPager parent) {
			mView1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.station_lock_add, null);
			mView2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.station_lock_list, null);
			
			initView1(mView1);
			initView2(mView2);
		}
		
		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
		@Override
        public Object instantiateItem(View container, int position) {
			final View page = position == 0 ? mView1 : mView2;
			((ViewGroup)container).addView(page);
			
			return page;
		}
		
		@Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewGroup) container).removeView((View) object);
        }
		
		private void initView1(View view) {
			stationLacView = (EditText) view.findViewById(R.id.station_lac);
			stationCidView = (EditText) view.findViewById(R.id.station_cid);
			stationBsdView = (EditText) view.findViewById(R.id.station_bsd);
			stationSidView = (EditText) view.findViewById(R.id.station_sid);
			stationNidView = (EditText) view.findViewById(R.id.station_nid);
					
			stationGsmContainer = view.findViewById(R.id.station_gsm);
			stationCdmaContainer = view.findViewById(R.id.station_cdma);
			mBaseContainer = (RadioGroup) view.findViewById(R.id.base_container);
			mDecimal = (RadioButton) view.findViewById(R.id.decimal);
			mHexaDecimal = (RadioButton) view.findViewById(R.id.hexadecimal);
			mBaseContainer.setOnCheckedChangeListener(mBaseListener);
			if (BASE_DECIMAL == mBase) {
				mDecimal.setChecked(true);
			} else {
				mHexaDecimal.setChecked(true);
			}
			
			
			lockInView = (Switch) view.findViewById(R.id.station_lock_in);
			lockOutView = (Switch) view.findViewById(R.id.station_lock_out);
			alarmVibrateView = (Switch) view.findViewById(R.id.station_lock_alarm_vibrate);
			alarmRingView = (Switch) view.findViewById(R.id.station_lock_alarm_ring);
			
			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
			if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
				stationCdmaContainer.setVisibility(View.VISIBLE);
				stationGsmContainer.setVisibility(View.GONE);
			}
			
		}
		
		private void initView2(View view) {
			mListView = (ListView) view.findViewById(android.R.id.list);
			mAdapter = new MyLocationAdapter(StationLock.this);
			mListView.setAdapter(mAdapter);
		}
		
	}
}