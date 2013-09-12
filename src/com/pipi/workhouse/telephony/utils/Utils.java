package com.pipi.workhouse.telephony.utils;

import com.pipi.workhouse.telephony.common.Constants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


public class Utils {
	public static CharSequence makeCarierString(CharSequence plmn, CharSequence spn) {
        final boolean plmnValid = !TextUtils.isEmpty(plmn);
        final boolean spnValid = !TextUtils.isEmpty(spn);
        if (plmnValid && spnValid) {
            return plmn + "|" + spn;
        } else if (plmnValid) {
            return plmn;
        } else if (spnValid) {
            return spn;
        } else {
            return "";
        }
    }
	
	public static CharSequence getTelephonyPlmnFrom(Intent intent) {
        if (intent.getBooleanExtra("showPlmn", false)) {
            final String plmn = intent.getStringExtra("plmn");
            if (plmn != null) {
                return plmn;
            } else {
                return "";
            }
        }
        return null;
    }
	
	public static CharSequence getTelephonySpnFrom(Intent intent) {
        if (intent.getBooleanExtra("showSpn", false)) {
            final String spn = intent.getStringExtra("spn");
            if (spn != null) {
                return spn;
            }
        }
        return null;
    }
	
	public static String getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

	public static void savePreference(Context context, String fileName, String key, int value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		Editor edit = sharedPreferences.edit();
		
		if (value == 0) {
			edit.remove(key);
		} else {
			edit.putInt(key, value);
		}
		
		edit.apply();
	}
	
	public static void removePreference(Context context, String fileName, String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		Editor edit = sharedPreferences.edit();
		edit.remove(key);
		edit.apply();
	}
	
	public static int getPreference(Context context, String fileName, String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		
		return sharedPreferences.getInt(key, 0);
	}
}