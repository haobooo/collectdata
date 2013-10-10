package com.pipi.workhouse.telephony.activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.pipi.workhouse.telephony.R;
import com.pipi.workhouse.telephony.adapter.LocationAdapter;
import com.pipi.workhouse.telephony.common.Constants;
import com.pipi.workhouse.telephony.common.MyApplication;
import com.pipi.workhouse.telephony.utils.CellLocationWrapper;
import com.pipi.workhouse.telephony.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class CollectionData extends Activity {
	private static final String TAG = "CollectionData";
	private TelephonyManager mTelephonyManager;
	
	private TextView mOperatorNameView;
	private TextView mPhoneTypeView;
	private TextView mLocationView;
	private ListView mListView;
	
	private SignalStrength mLastSigalStrength;
	private CellLocation mLastLocation;
	
	private LocationAdapter mAdapter;
	
	private double longitude;
	private double latitude;
	public LocationClient mLocationClient = null;
	
	private ArrayList<CellLocation> mCollectCell = new ArrayList<CellLocation>();
	private String cellKey = "";
	
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

        @Override
        public void onCellLocationChanged(CellLocation location) {
            updateLocation(location);
        }
        
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        	mLastSigalStrength = signalStrength;
        	
        	updateLocationView(mLastLocation);
        }

    };
    
    private BroadcastReceiver mPlmnSpnReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "mPlmnSpnReceiver action=" + intent.getAction());
			
			CharSequence plmn = Utils.getTelephonyPlmnFrom(intent);
			CharSequence spn = Utils.getTelephonySpnFrom(intent);
			mOperatorNameView.setText(Utils.makeCarierString(plmn, spn));
			mPhoneTypeView.setText(Utils.getNetworkClass(mTelephonyManager.getNetworkType()));
		}
    	
    };
    
    public BDLocationListener myListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			
			if (Constants.IS_DEBUG) Log.d(TAG, "longitude=" + longitude + "; latitude=" + latitude);
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.collectiondata_layout);
		
		mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
	    mLocationClient.registerLocationListener( myListener );    //注册监听函数
	    LocationClientOption locationClientOption = new LocationClientOption();
	    locationClientOption.setOpenGps(true);
	    locationClientOption.setCoorType("bd09ll");
	    locationClientOption.setScanSpan(5000);
	    mLocationClient.setLocOption(locationClientOption);
	    
		mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		
		CellLocation.requestLocationUpdate();
		
		initViews();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION
												/*| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS*/);
		//mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED");
		registerReceiver(mPlmnSpnReceiver,filter);
		
		mLocationClient.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		unregisterReceiver(mPlmnSpnReceiver);
		
		mLocationClient.stop();
	}
	
	private void initViews() {
		mOperatorNameView = (TextView) findViewById(R.id.phone_operator);
		mPhoneTypeView = (TextView) findViewById(R.id.phone_type);
		
		mOperatorNameView.setText(mTelephonyManager.getSimOperatorName());
		mPhoneTypeView.setText(Utils.getNetworkClass(mTelephonyManager.getNetworkType()));
		
		mLocationView = (TextView) findViewById(R.id.cell_info);
		mListView = (ListView) findViewById(android.R.id.list);
		
		mAdapter = new MyLocationAdapter(this);
		mListView.setAdapter(mAdapter);
	}
	
	private void updateLocation(CellLocation location) {
		Log.d(TAG, "location=" + location);
		updateLocationView(location);
		
		CellLocationWrapper locWrap = new CellLocationWrapper(location);
		locWrap.setSignalStrength(getSignalStrength());
		
		mCollectCell.add(locWrap);
		
		if (!cellKey.contains(locWrap.toString())) {
			mAdapter.addItem(locWrap);
			cellKey = cellKey + ";" + locWrap.toString();
		} else {
			((MyLocationAdapter)mAdapter).findAndReplace(locWrap);
		}
		
		mLastLocation = location;
	}
	
	private void updateLocationView(CellLocation location) {
		if (location == null) return;
		
		String locationStr = "";
		if (location instanceof GsmCellLocation) {
			GsmCellLocation loc = (GsmCellLocation)location;
			locationStr = this.getResources().getString(R.string.cell_location, loc.getLac(), loc.getCid()&0xFFFF, getSignalStrength());
		} else if (location instanceof CdmaCellLocation) {
			CdmaCellLocation loc = (CdmaCellLocation)location;
			locationStr = this.getResources().getString(R.string.cell_location_cdma, loc.getBaseStationId(), loc.getSystemId(), loc.getNetworkId(), getSignalStrength());
		}
		
		mLocationView.setText(locationStr);
	}
	
	private int getSignalStrength() {
		int signal = -1;
		
		mLastSigalStrength = ((MyApplication)getApplication()).getSignalStrength();
		
		if (mLastSigalStrength != null) {
			if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
				signal = mLastSigalStrength.getCdmaDbm();
	        } else if  (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
	        	signal = mLastSigalStrength.getGsmSignalStrength();
	        }
		}
		
		return signal;
	}
	
	public void onBack(View v) {
		finish();
	}
	
	public void onSaveFile(View v) {
		View view = LayoutInflater.from(this).inflate(R.layout.save_data_layout, null);
		final EditText fileEdit = (EditText) view.findViewById(R.id.file_name_edit);
		Button saveBtn = (Button) view.findViewById(R.id.save);
		Button cancelBtn = (Button) view.findViewById(R.id.cancel);
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//builder.setView(view);
		
		final AlertDialog dialog = builder.create();
		dialog.show();
		
		
		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setContentView(view);
		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		
		saveBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String fileName = fileEdit.getText().toString();
				
				if (fileName.length() == 0) {
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
					Date date= new Date(System.currentTimeMillis());
					fileName = formatter.format(date);
				}
				//fileName += "_CELL";
				//saveCellToFile(fileName);
				saveCellToFileEx(fileName);
				
				dialog.dismiss();
			}
			
		});
		
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
			
		});
		
		
	}
	
	private void saveCellToFile(String fileName) {
		//Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();
		
		SharedPreferences sharedPreferences = this.getSharedPreferences(fileName, MODE_PRIVATE);
		Editor edit = sharedPreferences.edit();
		
		String key, detailCell;
		StringBuilder detailBuilder = new StringBuilder();
		StringBuilder briefBuilder = new StringBuilder();
		
		int size = mCollectCell.size();
		for (int i = 0; i < size; i++) {
			CellLocationWrapper location = (CellLocationWrapper) mCollectCell.get(i);
			
			if (location.isGsm()) {
				key = location.getLac() + Constants.DATA_SEPERATOR + location.getCid();
			} else {
				key = location.getBaseStationId() + Constants.DATA_SEPERATOR + location.getSystemId() + Constants.DATA_SEPERATOR + location.getNetworkId();
			}
			
			// detail cell.
			detailCell = key + Constants.DATA_SEPERATOR + location.getSignalStrength() + Constants.DATA_SEPERATOR + location.getTime();
			detailBuilder.append(detailCell);
			detailBuilder.append("; ");
			
			// brief cell.
			if (!briefBuilder.toString().contains(key)) {
				briefBuilder.append(key);
				briefBuilder.append("; ");
			}
		}
		
		if (Constants.IS_DEBUG) Log.d(TAG, "detailBuilder=" + detailBuilder);
		if (Constants.IS_DEBUG) Log.d(TAG, "briefBuilder=" + briefBuilder);
		
		edit.putString(Constants.DETAIL_CELL_KEY, detailBuilder.toString());
		edit.putString(Constants.BRIEF_CELL_KEY, briefBuilder.toString());
		edit.apply();
		
		Toast.makeText(this, "Save OK!", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * 把采集的数据存储到外部存储卡，存储格式如下：
	 * 	#本数据文件包括'简表'和'详表'，简表只包含'小区信息'，详表除简表信息外还包括'信号强度'和'采集时间'
	 *  #运营商：中国联通
	 *  #简表---------------------------
	 *  #(2143,1234)(GSM制式)，或(11,22,33)(CDMA制式)
	 *  #......
	 *	#详表---------------------------
	 *	#(2143,1234,15,201333332223)(GSM制式)或(11,22,33,15,2013333333323)(CDMA制式)
	 * @param fileName
	 */
	private void saveCellToFileEx(String fileName) {
		String fullFileName = getFileDir() + File.separator + fileName;
		if (Constants.IS_DEBUG) Log.d(TAG, "[saveCellToFileEx] fullFileName=" + fullFileName);
		
		try {
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(new File(fullFileName)));
			bWriter.write("#本数据文件包括'简表'和'详表'，简表只包含'小区信息'，详表除简表信息外还包括'信号强度'和'采集时间'");
			bWriter.newLine();
			bWriter.write("#简表---------------------------");
			bWriter.newLine();
			bWriter.write("#(2143,1234)(GSM制式)，或(11,22,33)(CDMA制式)");
			bWriter.newLine();
			bWriter.write("#......");
			bWriter.newLine();
			bWriter.write("#详表---------------------------");
			bWriter.newLine();
			bWriter.write("#(2143,1234,15,201333332223)(GSM制式)或(11,22,33,15,2013333333323)(CDMA制式)");
			bWriter.newLine();
			bWriter.newLine();
			bWriter.newLine();
			
			String key, detailCell;
			StringBuilder briefBuilder = new StringBuilder();
			StringBuilder detailBuilder = new StringBuilder();
			int size = mCollectCell.size();
			
			bWriter.write("运营商：" + mOperatorNameView.getText().toString());
			bWriter.newLine();
			bWriter.newLine();
			
			bWriter.write("简表---------------------------");
			bWriter.newLine();
			for (int i = 0; i < size; i++) {
				CellLocationWrapper location = (CellLocationWrapper) mCollectCell.get(i);
				if (location.isGsm()) {
					key = "(" + location.getLac() + "," + location.getCid() + ")";
				} else {
					key = "(" + location.getBaseStationId() + "," + location.getSystemId() + "," + location.getNetworkId() + ")";
				}
				
				if (!briefBuilder.toString().contains(key)) {
					briefBuilder.append(key);
					briefBuilder.append("; ");
					
					bWriter.write(key);
					bWriter.newLine();
				}
			}
			
			bWriter.newLine();
			bWriter.write("详表---------------------------");
			bWriter.newLine();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			for (int i = 0; i < size; i++) {
				CellLocationWrapper location = (CellLocationWrapper) mCollectCell.get(i);
				
				Date date= new Date(location.getTime());
				String time = formatter.format(date);
				
				if (location.isGsm()) {
					key = "(" + location.getLac() + "," + location.getCid() + "," + location.getSignalStrength() + ","+ time + ")";
				} else {
					key = "(" + location.getBaseStationId() + "," + location.getSystemId() + "," + location.getNetworkId() + "," + location.getSignalStrength() + ","+ time + ")";
				}
				
				bWriter.write(key);
				bWriter.newLine();
			}
			
			bWriter.flush();
			bWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private String getPhoneType(int type) {
		String phone = "";
		switch(mTelephonyManager.getPhoneType()) {
		case TelephonyManager.PHONE_TYPE_NONE:
			phone = getResources().getString(R.string.phone_type_none);
			break;
		case TelephonyManager.PHONE_TYPE_GSM:
			phone = getResources().getString(R.string.phone_type_gsm);
			break;
		case TelephonyManager.PHONE_TYPE_CDMA:
			phone = getResources().getString(R.string.phone_type_cdma);
			break;
		case TelephonyManager.PHONE_TYPE_SIP:
			phone = getResources().getString(R.string.phone_type_sip);
			break;
		}
		
		return phone;
	}
	
	private class MyLocationAdapter extends LocationAdapter {

		public MyLocationAdapter(Context context) {
			super(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
			}
			
			TextView cellView = (TextView) convertView.findViewById(android.R.id.text1);
			
			String cellLocation = "";
			CellLocationWrapper location = (CellLocationWrapper) getItem(position);
			
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");//yyyy-MM-dd HH:mm:ss
			Date date= new Date(location.getTime());
			cellLocation = "[" + formatter.format(date) + "] ";
			
			if (location.isGsm()) {
				//cellLocation = mContext.getResources().getString(R.string.cell_location, location.getLac(), location.getCid(), location.getAsu());
				cellLocation += location.getLac() + ", " + (location.getCid()&0xFFFF) + ", " + location.getSignalStrength();
			} else {
				//cellLocation = mContext.getResources().getString(R.string.cell_location_cdma, location.getBaseStationId(), location.getSystemId(), location.getNetworkId());
				cellLocation += location.getBaseStationId() + ", " + location.getSystemId() + ", " + location.getNetworkId() + ", " + location.getSignalStrength();
			}
			
			cellView.setText(cellLocation);
			
			if (position % 2 == 1) {
				convertView.setBackgroundColor(mContext.getResources().getColor(R.color.grey));
			} else {
				convertView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
			}
			
			// The first always highlighted.
			if (position == 0) {
				convertView.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
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
		
		public void findAndReplace(CellLocationWrapper newLocation) {
			int count = this.getCount();
			
			String key = newLocation.toString();
			int index = -1;
			for (int i = 0; i < count; i++) {
				CellLocationWrapper location = (CellLocationWrapper) getItem(i);
				
				if (location.toString().equals(key)) {
					index = i;
					break;
				}
			}
			
			if (index != -1) {
				mLocation.remove(index);
				addItem(newLocation);
			}
		}
	}
	
	public void showMap(View view) {
		Intent intent = new Intent(this, MapActivity.class);
		intent.putExtra(Constants.KEY_LONGTITUDE, longitude);
		intent.putExtra(Constants.KEY_LATITUDE, latitude);
		
		if (mLastLocation instanceof GsmCellLocation) {
			intent.putExtra(Constants.KEY_CELL_LOCATION_TYPE, "GSM");
			intent.putExtra(Constants.KEY_CELL_LOCATION_LAC, ((GsmCellLocation) mLastLocation).getLac());
			intent.putExtra(Constants.KEY_CELL_LOCATION_CID, ((GsmCellLocation) mLastLocation).getCid());
		} else if (mLastLocation instanceof CdmaCellLocation) {
			intent.putExtra(Constants.KEY_CELL_LOCATION_TYPE, "CDMA");
			intent.putExtra(Constants.KEY_CELL_LOCATION_BSD, ((CdmaCellLocation) mLastLocation).getBaseStationId());
			intent.putExtra(Constants.KEY_CELL_LOCATION_SID, ((CdmaCellLocation) mLastLocation).getSystemId());
			intent.putExtra(Constants.KEY_CELL_LOCATION_NID, ((CdmaCellLocation) mLastLocation).getNetworkId());
		}
		
		startActivity(intent);
	}
	
	private String getFileDir() {
		//存储路径：mnt/sdcard/collectdata/基站采集
		String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "collectdata" + File.separator + "基站采集";
		File file = new File(dirPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		return dirPath;
	}
}