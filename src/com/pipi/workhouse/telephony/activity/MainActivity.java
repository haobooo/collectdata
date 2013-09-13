package com.pipi.workhouse.telephony.activity;

import com.pipi.workhouse.telephony.R;
import com.pipi.workhouse.telephony.common.MyApplication;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	private MyApplication mApp;
	private TelephonyManager mTelephonyManager;
	
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        	mApp.setSigalStrength(signalStrength);
        }

    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		mApp = (MyApplication) getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		
	}
	
	public void goNext(View view) {
		switch(view.getId()) {
		case R.id.bs_collectdata:
			Intent collectData = new Intent(this, CollectionData.class);
			startActivity(collectData);
			break;
		case R.id.bs_lock_and_alarm:
			Intent stationLock = new Intent(this, StationLock.class);
			startActivity(stationLock);
			break;
		case R.id.bs_looptest:
			Intent loopTest = new Intent(this, LoopTest.class);
			startActivity(loopTest);
			break;
		case R.id.bs_file_manager:
			Intent fileManager = new Intent(this, FileManager.class);
			startActivity(fileManager);
			break;
		case R.id.bs_search:
			break;
		case R.id.bs_navigation:
			break;
		case R.id.bs_location_search:
			break;
		case R.id.bs_help:
			break;
		}
	}
}
