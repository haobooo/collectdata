package com.pipi.workhouse.telephony.adapter;

import java.util.ArrayList;

import com.pipi.workhouse.telephony.R;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class LocationAdapter extends BaseAdapter {
	private static final String TAG = "LocationAdapter";
	
	protected Context mContext;
	protected ArrayList<CellLocation> mLocation = new ArrayList<CellLocation>();
	
	public LocationAdapter(Context context) {
		mContext = context;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mLocation.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= mLocation.size()) {
			return null;
		}
		return mLocation.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
		}
		
		TextView cellView = (TextView) convertView.findViewById(android.R.id.text1);
		
		String cellLocation = "";
		CellLocation location = (CellLocation) getItem(position);
		if (location instanceof GsmCellLocation) {
			GsmCellLocation loc = (GsmCellLocation)location;
			cellLocation = mContext.getResources().getString(R.string.cell_location, loc.getLac(), loc.getCid(), loc.getPsc());
		} else if (location instanceof CdmaCellLocation) {
			CdmaCellLocation loc = (CdmaCellLocation)location;
			cellLocation = mContext.getResources().getString(R.string.cell_location_cdma, loc.getBaseStationId(), loc.getSystemId(), loc.getNetworkId());
		}
		
		cellView.setText(cellLocation);
		return convertView;
	}
	
	public void addItem(CellLocation location) {
		mLocation.add(location);
		sort();
		notifyDataSetChanged();
	}
	
	public void sort() {
		
	}
	
	public void clear() {
		mLocation.clear();
		notifyDataSetChanged();
	}
	
}