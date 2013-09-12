package com.pipi.workhouse.telephony.common;


import com.baidu.mapapi.BMapManager;

import android.app.Application;


public class MyApplication extends Application {
	private BMapManager mBMapMan = null;
	
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
}