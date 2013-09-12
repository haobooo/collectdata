package com.pipi.workhouse.telephony.widget;

import com.pipi.workhouse.telephony.common.Constants;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;


public class MyViewPager extends ViewPager {
	private static final String TAG = "MyViewPager";
	
	private float xDistance, yDistance, xLast, yLast;
	
	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override  
    public boolean onInterceptTouchEvent(MotionEvent ev) {  
        switch (ev.getAction()) {  
            case MotionEvent.ACTION_DOWN:  
                xDistance = yDistance = 0f;  
                xLast = ev.getX();  
                yLast = ev.getY();  
                break;  
            case MotionEvent.ACTION_MOVE:  
                final float curX = ev.getX();  
                final float curY = ev.getY();  
                  
                xDistance += Math.abs(curX - xLast);  
                yDistance += Math.abs(curY - yLast);  
                xLast = curX;  
                yLast = curY;  
                
                if (Constants.IS_DEBUG) Log.d(TAG, "xDistance=" + xDistance + "; yDistance=" + yDistance);
                if(xDistance > yDistance){  
                    return true;  
                }    
        }  
  
        return super.onInterceptTouchEvent(ev);  
    }  
}