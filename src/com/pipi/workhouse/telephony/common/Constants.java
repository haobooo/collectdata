package com.pipi.workhouse.telephony.common;

import android.os.Environment;


public class Constants {
	public static final boolean IS_DEBUG = true;
	
	public static final String LOCK_MODE_FILE = "lock_mode";
	public static final String LOCK_ALARM_MODE_FILE = "alarm_mode";
	public static final String LOCK_CELL_KEY = "lock_cell_key";
	public static final String NOTICIFATION_KEY = "notification_key";
	public static final String DETAIL_CELL_KEY = "detail_cell";
	public static final String BRIEF_CELL_KEY = "brief_cell";
	public static final String DATA_SEPERATOR = "/";
	
	public static int LOCK_MODE_IN = 1;
	public static int LOCK_MODE_OUT = 2;
	
	public static int LOCK_ALARM_MODE_VIB = 1;
	public static int LOCK_ALARM_MODE_RING = 2;
	
	// Map related.
	public static String KEY_LONGTITUDE = "LONGTITUDE";
	public static final String KEY_LATITUDE = "LATITUDE";
	public static final String KEY_CELL_LOCATION_TYPE = "CELL_LOCATION_TYPE";
	public static final String KEY_CELL_LOCATION_LAC = "CELL_LOCATION_LAC";
	public static final String KEY_CELL_LOCATION_CID = "CELL_LOCATION_CID";
	public static final String KEY_CELL_LOCATION_BSD = "CELL_LOCATION_BSD";
	public static final String KEY_CELL_LOCATION_SID = "CELL_LOCATION_SID";
	public static final String KEY_CELL_LOCATION_NID = "CELL_LOCATION_NID";
	
	// Map key.
	public static final String MAP_KEY = "98596aaceb832c608063a4e8f865be61"; // release used.
	//public static final String MAP_KEY = "A8d46f967ba4c17f12ca53e7302813d5"; // debug used home. 
	//public static final String MAP_KEY = "9c567d6d41414c537e035c2b818ea545"; // debug used at work.
	
	
	public static boolean isExternalStorageEnabled() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		}
		
		return false;
	}
}