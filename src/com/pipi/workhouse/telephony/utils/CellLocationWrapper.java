package com.pipi.workhouse.telephony.utils;

import android.telephony.CellLocation;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;


public class CellLocationWrapper extends CellLocation {
	
	private CellLocation mCellLocation;
	private long updateTime = -1;
	private int lockMode = 0;
	private int alarmMode = 0;
	private int mSignalStrength = -1;
	private double mLongitude;
	private double mLatitude;
	
	public CellLocationWrapper(CellLocation location) {
		mCellLocation = location;
		updateTime = System.currentTimeMillis();
	}
	
	public int getLac() {
		if (mCellLocation instanceof GsmCellLocation) {
			return ((GsmCellLocation)mCellLocation).getLac();
		}
		
		return -1;
	}
	
	public int getCid() {
		if (mCellLocation instanceof GsmCellLocation) {
			return ((GsmCellLocation)mCellLocation).getCid();
		}
		
		return -1;
	}
	
	public int getPsc() {
		if (mCellLocation instanceof GsmCellLocation) {
			return ((GsmCellLocation)mCellLocation).getPsc();
		}
		
		return -1;
	}
	
	public int getBaseStationId() {
		if (mCellLocation instanceof CdmaCellLocation) {
			return ((CdmaCellLocation)mCellLocation).getBaseStationId();
		}
		
		return -1;
	}
	
	public int getSystemId() {
		if (mCellLocation instanceof CdmaCellLocation) {
			return ((CdmaCellLocation)mCellLocation).getSystemId();
		}
		
		return -1;
	}
	
	public int getNetworkId() {
		if (mCellLocation instanceof CdmaCellLocation) {
			return ((CdmaCellLocation)mCellLocation).getNetworkId();
		}
		
		return -1;
	}
	
	public boolean isGsm() {
		return (mCellLocation instanceof GsmCellLocation);
	}
	
	public void setTime(long time) {
		updateTime = time;
	}
	
	public long getTime() {
		return updateTime;
	}
	
	public void setLockMode(int lock) {
		lockMode = lock;
	}
	
	public int getLockMode() {
		return lockMode;
	}
	
	public void setAlarmMode(int mode) {
		alarmMode = mode;
	}
	
	public int getAlarmMode() {
		return alarmMode;
	}
	
	public void setSignalStrength(int strength) {
		mSignalStrength = strength;
	}
	
	public int getSignalStrength() {
		return mSignalStrength;
	}
	
	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}
	
	public double getLongitude() {
		return mLongitude;
	}
	
	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}
	
	public double getLatitude() {
		return mLatitude;
	}
}