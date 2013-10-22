package com.pipi.workhouse.telephony.service;

import com.pipi.workhouse.telephony.R;
import com.pipi.workhouse.telephony.common.Constants;
import com.pipi.workhouse.telephony.utils.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;


public class StationLockService extends Service {
	private static final String TAG = "StationLockService";
	
	private TelephonyManager mTelephonyManager;
	private CellLocation mCellLocation;
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

        @Override
        public void onCellLocationChanged(CellLocation location) {
        	checkCellLock(location);
        }

    };
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (Constants.IS_DEBUG) Log.d(TAG, "onCreate");
		mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
	}
	
	
	@Override
	public void onDestroy() {
		if (Constants.IS_DEBUG) Log.d(TAG, "onDestroy");
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		
		super.onDestroy();
	}
	
	private void checkCellLock(CellLocation location) {
		if (Constants.IS_DEBUG) Log.d(TAG, "[checkCellLock] location=" + location);
		if (Constants.IS_DEBUG) Log.d(TAG, "[mCellLocation] location=" + mCellLocation);
		
		// Out check.
		String key = "";
		if (mCellLocation instanceof GsmCellLocation) {
			GsmCellLocation loc = (GsmCellLocation)mCellLocation;
			key = loc.getLac() + "-" + loc.getCid();
		} else if (mCellLocation instanceof CdmaCellLocation) {
			CdmaCellLocation loc = (CdmaCellLocation)mCellLocation;
			key = loc.getBaseStationId() + "-" + loc.getSystemId() + "-" + loc.getNetworkId();
		}
		
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOCK_MODE_FILE, MODE_PRIVATE);
		int lockMode = sharedPreferences.getInt(key, 0);
		
		if (Constants.IS_DEBUG) Log.d(TAG, "[checkCellLock] 1. key=" + key + "; lockMode=" + lockMode);
		if ((lockMode & Constants.LOCK_MODE_OUT) == Constants.LOCK_MODE_OUT) {
			int alarmMode = Utils.getPreference(this, Constants.LOCK_ALARM_MODE_FILE, key);
			if (Constants.IS_DEBUG) Log.d(TAG, "[checkCellLock] 1. key=" + key + "; alarmMode=" + alarmMode);
			Alarm(alarmMode);
			
			AlarmByNotification(getResources().getString(R.string.station_lock_out_alarm, key));
		}
		
		// In check.
		if (location instanceof GsmCellLocation) {
			GsmCellLocation loc = (GsmCellLocation)location;
			key = loc.getLac() + "-" + loc.getCid();
		} else if (location instanceof CdmaCellLocation) {
			CdmaCellLocation loc = (CdmaCellLocation)location;
			key = loc.getBaseStationId() + "-" + loc.getSystemId() + "-" + loc.getNetworkId();
		} else {
			key = "";
		}
		lockMode = sharedPreferences.getInt(key, 0);
		if (Constants.IS_DEBUG) Log.d(TAG, "[checkCellLock] 2. key=" + key + "; lockMode=" + lockMode);
		if ((lockMode & Constants.LOCK_MODE_IN) == Constants.LOCK_MODE_IN) {
			int alarmMode = Utils.getPreference(this, Constants.LOCK_ALARM_MODE_FILE, key);
			if (Constants.IS_DEBUG) Log.d(TAG, "[checkCellLock] 2. key=" + key + "; alarmMode=" + alarmMode);
			Alarm(alarmMode);
			
			AlarmByNotification(getResources().getString(R.string.station_lock_in_alarm, key));
		}
		
		mCellLocation = location;
	}
	
	private void AlarmByVibrator() {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator == null /*|| !vibrator.hasVibrator()*/) {
            return ;
        }
		
		vibrator.vibrate(1000);
	}
	
	private void AlarmByRing() {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.message_female);
		mp.start();
	}
	
	private void Alarm(int mode) {
		if ((mode & Constants.LOCK_ALARM_MODE_VIB) == Constants.LOCK_ALARM_MODE_VIB) {
			AlarmByVibrator();
		}
		
		if ((mode & Constants.LOCK_ALARM_MODE_RING) == Constants.LOCK_ALARM_MODE_RING) {
			AlarmByRing();
		}
	}
	private void AlarmByNotification(String message) {
		Notification.Builder notification = new Notification.Builder(this)
			.setAutoCancel(true)
			.setSmallIcon(R.drawable.ic_stat_notify_lock)
			.setContentTitle(message)
			.setTicker(message);
		
		NotificationManager notificationManager =
            (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(getNextNotificationId(), notification.getNotification());
	}
	
	private int getNextNotificationId() {
		int id = -1;
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOCK_MODE_FILE, MODE_PRIVATE);
		id = sharedPreferences.getInt(Constants.NOTICIFATION_KEY, 0);
		++id;
		if (id > 37265) {
			id = 1;
		}
		
		sharedPreferences.edit().putInt(Constants.NOTICIFATION_KEY, id).apply();
		
		return id;
	}
}