package com.pipi.workhouse.telephony.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.jraf.android.backport.switchwidget.Switch;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.pipi.workhouse.telephony.R;
import com.pipi.workhouse.telephony.adapter.LocationAdapter;
import com.pipi.workhouse.telephony.common.Constants;
import com.pipi.workhouse.telephony.utils.CellLocationWrapper;
import com.pipi.workhouse.telephony.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class LoopTest extends Activity {
	private static final String TAG = "LoopTest";
	
	private double longitude;
	private double latitude;
	private TextView mOperatorNameView;
	private TextView mPhoneTypeView;
	private TextView mSavePathPromptView;
	private ListView mListView;
	private LocationAdapter mAdapter;
	private TelephonyManager mTelephonyManager;
	
	public LocationClient mLocationClient = null;
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

        @Override
        public void onCellLocationChanged(CellLocation location) {
        	if (Constants.IS_DEBUG) Log.d(TAG, "onCellLocationChanged");
            updateLocation(location);
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
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.looptest_layout);
		
		initViews();
		
		mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
	    mLocationClient.registerLocationListener( myListener );    //注册监听函数
	    LocationClientOption locationClientOption = new LocationClientOption();
	    locationClientOption.setOpenGps(true);
	    locationClientOption.setCoorType("bd09ll");
	    locationClientOption.setScanSpan(5000);
	    mLocationClient.setLocOption(locationClientOption);
	    
	    mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		CellLocation.requestLocationUpdate();
	}
	
	protected void onResume() {
		super.onResume();
		
		mLocationClient.start();
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED");
		registerReceiver(mPlmnSpnReceiver,filter);
	}
	
	protected void onPause() {
		super.onPause();
		
		mLocationClient.stop();
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		
		unregisterReceiver(mPlmnSpnReceiver);
	}
	
	private void updateLocation(CellLocation location) {
		Log.d(TAG, "location=" + location);
		String locationStr = "";
		if (location instanceof GsmCellLocation) {
			GsmCellLocation loc = (GsmCellLocation)location;
			locationStr = this.getResources().getString(R.string.cell_location, loc.getLac(), loc.getCid(), loc.getPsc());
		} else if (location instanceof CdmaCellLocation) {
			CdmaCellLocation loc = (CdmaCellLocation)location;
			locationStr = this.getResources().getString(R.string.cell_location_cdma, loc.getBaseStationId(), loc.getSystemId(), loc.getNetworkId());
		}
		
		CellLocationWrapper locWrapper = new CellLocationWrapper(location);
		locWrapper.setLongitude(longitude);
		locWrapper.setLatitude(latitude);
		
		mAdapter.addItem(locWrapper);
	}
	
	private void initViews() {
		mOperatorNameView = (TextView) findViewById(R.id.phone_operator);
		mPhoneTypeView = (TextView) findViewById(R.id.phone_type);
		
		mSavePathPromptView = (TextView) findViewById(R.id.save_path_prompt);
		mListView = (ListView) findViewById(android.R.id.list);
		
		mAdapter = new MyLocationAdapter(this);
		mListView.setAdapter(mAdapter);
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
				saveDataToFile(fileName);
				
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
	
	private void saveDataToFile(String fileName) {
		FileOutputStream os = null;
		OutputStreamWriter osWriter = null;
		try {
			String filePath = getFileDir() + File.separator + fileName;
			if (Constants.IS_DEBUG) Log.d(TAG, "filePath=" + filePath);
			
			os = new FileOutputStream(filePath);
			osWriter = new OutputStreamWriter(os);
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			int size = mAdapter.getCount();
			String seperator = ", ";
			for (int i = 0; i < size; i++) {
				String key;
				CellLocationWrapper location = (CellLocationWrapper) mAdapter.getItem(i);
				
				Date date= new Date(location.getTime());
				String time = formatter.format(date);
				
				if (location.isGsm()) {
					key = location.getLac() + seperator + location.getCid() + seperator + time + seperator + location.getLongitude() + seperator + location.getLatitude();
				} else {
					key = location.getBaseStationId() + seperator + location.getSystemId() + seperator + location.getNetworkId()+ seperator + time + seperator + location.getLongitude() + seperator + location.getLatitude();
				}
				
				key += "\r\n";
				
				try {
					osWriter.write(key, 0, key.length());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				osWriter.close();
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Toast.makeText(this, "Save OK", Toast.LENGTH_SHORT).show();
		mSavePathPromptView.setText(getFileDir() + File.separator + fileName);
		mSavePathPromptView.setVisibility(View.VISIBLE);
	}
	
	private class MyLocationAdapter extends LocationAdapter {

		public MyLocationAdapter(Context context) {
			super(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.looptest_list_item, null);
			}
			
			View gsmView = convertView.findViewById(R.id.station_gsm);
			View cdmaView = convertView.findViewById(R.id.station_cdma);
			TextView lacView = (TextView)convertView.findViewById(R.id.station_lac);
			TextView cidView = (TextView)convertView.findViewById(R.id.station_cid);
			TextView bsdView = (TextView)convertView.findViewById(R.id.station_bsd);
			TextView sidView = (TextView)convertView.findViewById(R.id.station_sid);
			TextView nidView = (TextView)convertView.findViewById(R.id.station_nid);
			TextView longtitudeView = (TextView)convertView.findViewById(R.id.looptest_longtitude);
			TextView lantitudeView = (TextView)convertView.findViewById(R.id.looptest_lantitude);
			
			
			TextView timeView = (TextView) convertView.findViewById(R.id.time);
			
			String cellLocation = "";
			CellLocationWrapper location = (CellLocationWrapper) getItem(position);
			if (location.isGsm()) {
				cdmaView.setVisibility(View.GONE);
				gsmView.setVisibility(View.VISIBLE);
				
				lacView.setText(String.valueOf(location.getLac()));
				cidView.setText(String.valueOf(location.getCid()&0xFFFF));
			} else {
				cdmaView.setVisibility(View.VISIBLE);
				gsmView.setVisibility(View.GONE);
				
				bsdView.setText(String.valueOf(location.getBaseStationId()));
				sidView.setText(String.valueOf(location.getSystemId()));
				nidView.setText(String.valueOf(location.getNetworkId()));
				
			}
			
			longtitudeView.setText(String.valueOf(location.getLongitude()));
			lantitudeView.setText(String.valueOf(location.getLatitude()));
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date= new Date(location.getTime());  
			timeView.setText(formatter.format(date));
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
	}
	
	private String getFileName() {
		String fileName = "log";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Date date= new Date(System.currentTimeMillis());
		fileName = formatter.format(date);
		
		return fileName;
	}
	
	private String getFileDir() {
		//存储路径：mnt/sdcard/collectdata/基站路测
		String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "collectdata" + File.separator + "基站路测";
		File file = new File(dirPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		return dirPath;
	}
}