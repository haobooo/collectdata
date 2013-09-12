package com.pipi.workhouse.telephony.widget;

import java.util.ArrayList;

import com.pipi.workhouse.telephony.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ViewPageIndicator extends LinearLayout{
	private static final String TAG = "ViewPageIndicator";
	
	ArrayList<ImageView> pointList;
	ImageView point;
	LayoutParams lp;
	Context context;
	private int mResId;
	private int mActiveIndex = 0;
	
	public ViewPageIndicator(Context context) {
		super(context);
		this.context=context;
		init();
		setOrientation(0);
		setWillNotDraw(false);
	}
	
	public ViewPageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		init();
		setOrientation(0);
		setWillNotDraw(false);
	}
	
	private void init(){
		lp=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		pointList=new ArrayList<ImageView>();
		
		mResId = R.drawable.viewpager_indicator_drawable_selector;
	}
	
	public void setIndicatorDrwable(int resId) {
		mResId = resId;
	}
	
	public void setPointCount(int PointCount){
		for(int i=0;i<PointCount;i++){
			point=new ImageView(context);
			point.setImageResource(mResId);
			point.setPadding(5,0,5,0);
			point.setSelected(false);
			pointList.add(point);
			addView(point);
		}
		
		setPoint(0);
	}
	
	public void setPoint(int i){
		if (i < pointList.size()) {
			pointList.get(mActiveIndex).setSelected(false);
			
			mActiveIndex = i;
			pointList.get(mActiveIndex).setSelected(true);
		}
	}
}
