package com.pipi.workhouse.telephony.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.pipi.workhouse.telephony.R;
import com.pipi.workhouse.telephony.common.Constants;
import com.pipi.workhouse.telephony.common.MyApplication;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


public class MapActivity extends Activity {
	private static final String TAG = "MapActivity";
	
	private BMapManager mBMapMan = null;
	private MKSearch mSearch = null;
	private MapView mMapView = null;
	private double longtitude;
	private double latitude;
	private GeoPoint mDstGeoPoint = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mBMapMan = ((MyApplication)getApplication()).getMapManager();
		
		setContentView(R.layout.map_layout);
		
		getDataFromIntent();
		
		initViews();
	}
	
	private void initViews() {
		// Init map.
		mMapView=(MapView)findViewById(R.id.bmapsView);  
		mMapView.setBuiltInZoomControls(true);  
		//设置启用内置的缩放控件  
		MapController mMapController=mMapView.getController();  
		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放  
		GeoPoint point =new GeoPoint((int)(39.915* 1E6),(int)(116.404* 1E6));  
		//用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
		mMapController.setCenter(mDstGeoPoint);//设置地图中心点  
		mMapController.setZoom(17);//设置地图zoom级别  
		
		mSearch = new MKSearch();
        mSearch.init(mBMapMan, new MKSearchListener() {

			@Override
			public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetPoiDetailSearchResult(int arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetPoiResult(MKPoiResult res, int type, int error) {
				if (mDstGeoPoint != null) {
			        MyLocationOverlay myLocationOverlay = new MyLocationOverlay(mMapView);
					LocationData locData = new LocationData();
					locData.latitude = latitude;
					locData.longitude = longtitude;
					locData.direction = 2.0f;
					myLocationOverlay.setData(locData);
					mMapView.getOverlays().add(myLocationOverlay);
					
					mMapView.getController().animateTo(mDstGeoPoint);
					mMapView.refresh();
		        }
			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
					int arg2) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
        mSearch.poiSearchNearBy("基站", mDstGeoPoint, 1000);
	}
	
	private void getDataFromIntent() {
		longtitude = getIntent().getDoubleExtra(Constants.KEY_LONGTITUDE, -1);
		latitude = getIntent().getDoubleExtra(Constants.KEY_LATITUDE, -1);
		if (Constants.IS_DEBUG) Log.d(TAG, "longtitude=" + longtitude + " ; latitude=" + latitude);
		
		if (longtitude != -1 && latitude != -1) {
			mDstGeoPoint = new GeoPoint((int)(latitude*1e6), (int)(longtitude*1e6));
		}
		
	}
	
	public void onBack(View view) {
		finish();
	}
	
	@Override  
	protected void onDestroy(){  
        mMapView.destroy();  
  
        super.onDestroy();  
	}
	
	@Override  
	protected void onPause(){  
        mMapView.onPause();  
        if(mBMapMan!=null){
        	mBMapMan.stop();
        }  
        super.onPause();  
	}
	
	@Override  
	protected void onResume(){  
		mMapView.onResume();  
        if(mBMapMan!=null){  
        	mBMapMan.start();
        }  
        super.onResume();
	}  
}
