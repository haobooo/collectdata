package com.pipi.workhouse.telephony.common;


import com.baidu.mapapi.BMapManager;

import android.app.Application;
import android.telephony.SignalStrength;


public class MyApplication extends Application {
	private BMapManager mBMapMan = null;
	private SignalStrength mLastSigalStrength;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Initiate the Map Manager.
		mBMapMan = new BMapManager(this);
        mBMapMan.init(Constants.MAP_KEY, null);
	}
	
	public BMapManager getMapManager() {
		return mBMapMan;
	}
	
	public void setSigalStrength(SignalStrength signal) {
		mLastSigalStrength = signal;
	}
	
	public SignalStrength getSignalStrength() {
		return mLastSigalStrength;
	}
}