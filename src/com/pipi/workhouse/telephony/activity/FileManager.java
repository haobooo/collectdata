package com.pipi.workhouse.telephony.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellLocation;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pipi.workhouse.telephony.R;
import com.pipi.workhouse.telephony.adapter.LocationAdapter;
import com.pipi.workhouse.telephony.common.Constants;
import com.pipi.workhouse.telephony.utils.CellLocationWrapper;


public class FileManager extends Activity {
	private static final String TAG = "FileManager";
	private static final int REQUEST_SELECT_FILE = 1;
	private static final int BREIF_SECTION = 1;
	private static final int DETAIL_SECTION = 2;
	
	private TextView fileView;
	private ListView mListView;
	private LocationAdapter mBriefAdapter;
	private LocationAdapter mDetailAdapter;
	
	private FilenameFilter mFileNameFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			String regularExpression = ".*";
			if (Constants.IS_DEBUG) Log.d(TAG, "regularExpression= " + regularExpression);
			if (Constants.IS_DEBUG) Log.d(TAG, "filename= " + filename);
			if (filename.matches(regularExpression)) {
				return true;
			}
			return false;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.file_manager_layout);
		
		initViews();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == RESULT_OK) {
			String file = data.getStringExtra("PATH");
			fileView.setText(file);
			mBriefAdapter.clear();
			mDetailAdapter.clear();
			
		}
	}
	
	private void initViews() {
		fileView = (TextView) findViewById(R.id.file_name);
		mListView = (ListView) findViewById(android.R.id.list);
		
		mBriefAdapter = new BriefLocationAdapter(this);
		mDetailAdapter = new DetailLocationAdapter(this);
	}
	
	public void btnHandle(View view) {
		switch(view.getId()) {
		case R.id.select:
			Intent selectFile = new Intent(this, SelectFileActivity.class);
			startActivityForResult(selectFile, REQUEST_SELECT_FILE);
			break;
		case R.id.open:
			openBriefCellInfo();
			break;
		case R.id.detail:
			openDetailCellInfo();
			break;
		case R.id.delete:
			deleteDataFile(fileView.getText().toString());
			break;
		case R.id.delete_all:
			deleteAllDataFile();
			break;
		}
	}
	
	public void onBack(View view) {
		finish();
	}
	
	private void openBriefCellInfo() {
//		mBriefAdapter.clear();
//		
//		String fileName = getFileName(fileView.getText().toString());
//		SharedPreferences sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);
//		
//		String briefCell = sharedPreferences.getString(Constants.BRIEF_CELL_KEY, "");
//		briefCell = briefCell.trim();
//		if (Constants.IS_DEBUG) Log.d(TAG, "briefCell=" + briefCell);
//		if (briefCell.length() > 0) {
//			String[] cells = briefCell.split(";");
//			
//			if (Constants.IS_DEBUG) Log.d(TAG, "cells=" + Arrays.toString(cells));
//			int size = cells.length;
//			for (int i = 0; i < size; i++) {
//				CellLocation loc = null;
//				String cell = cells[i].trim();
//				String[] tmp = cell.split(Constants.DATA_SEPERATOR);
//				
//				if (Constants.IS_DEBUG) Log.d(TAG, "cell=" + cell);
//				if (Constants.IS_DEBUG) Log.d(TAG, "len=" + tmp.length);
//				int len = tmp.length;
//				if (len == 2) {
//					int lac = Integer.parseInt(tmp[0]);
//					int cid = Integer.parseInt(tmp[1]);
//					
//					loc = new GsmCellLocation();
//					((GsmCellLocation)loc).setLacAndCid(lac, cid);
//				} else if (len == 3) {
//					int bsd = Integer.parseInt(tmp[0]);
//					int sid = Integer.parseInt(tmp[1]);
//					int nid = Integer.parseInt(tmp[2]);
//					
//					loc = new CdmaCellLocation();
//					((CdmaCellLocation)loc).setCellLocationData(bsd, Integer.MAX_VALUE, Integer.MAX_VALUE, sid, nid);
//				}
//				
//				CellLocationWrapper location = new CellLocationWrapper(loc);
//				mBriefAdapter.addItem(location);
//			}
//			
//			mListView.setAdapter(mBriefAdapter);
//		}
		getCellInfoFromFile(fileView.getText().toString());
		mListView.setAdapter(mBriefAdapter);
	}
	
	private void openDetailCellInfo() {
//		mDetailAdapter.clear();
//		
//		String fileName = getFileName(fileView.getText().toString());
//		SharedPreferences sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);
//		
//		String detailCell = sharedPreferences.getString(Constants.DETAIL_CELL_KEY, "");
//		detailCell = detailCell.trim();
//		if (detailCell.length() > 0) {
//			String[] cells = detailCell.split(";");
//			int size = cells.length;
//			for (int i = 0; i < size; i++) {
//				CellLocation loc = null;
//				String cell = cells[i].trim();
//				String[] tmp = cell.split(Constants.DATA_SEPERATOR);
//				
//				int len = tmp.length;
//				int asu = -1;
//				long time = 0;
//				if (len == 4) {
//					int lac = Integer.parseInt(tmp[0]);
//					int cid = Integer.parseInt(tmp[1]);
//					asu = Integer.parseInt(tmp[2]);
//					time = Long.parseLong(tmp[3]);
//					
//					loc = new GsmCellLocation();
//					((GsmCellLocation)loc).setLacAndCid(lac, cid);
//				} else if (len == 5) {
//					
//					int bsd = Integer.parseInt(tmp[0]);
//					int sid = Integer.parseInt(tmp[1]);
//					int nid = Integer.parseInt(tmp[2]);
//					asu = Integer.parseInt(tmp[3]);
//					time = Long.parseLong(tmp[4]);
//					
//					loc = new CdmaCellLocation();
//					((CdmaCellLocation)loc).setCellLocationData(bsd, Integer.MAX_VALUE, Integer.MAX_VALUE, sid, nid);
//				}
//				
//				CellLocationWrapper location = new CellLocationWrapper(loc);
//				location.setSignalStrength(asu);
//				location.setTime(time);
//				mDetailAdapter.addItem(location);
//			}
//			
//			mListView.setAdapter(mDetailAdapter);
//		}
		
		getCellInfoFromFile(fileView.getText().toString());
		mListView.setAdapter(mDetailAdapter);
	}
	
	private void deleteDataFile(String fileName) {
		if (fileName == null && fileName.length() == 0) {
			return;
		}
		
		String fileFullPath = getFileDir() + File.separator + fileName;
		File file = new File(fileFullPath);
		boolean success = file.delete();
		
		if (success) {
			Toast.makeText(this, "File is deleted!", Toast.LENGTH_SHORT).show();
			mBriefAdapter.clear();
			mDetailAdapter.clear();
			fileView.setText("");
		}
	}
	
	private void deleteAllDataFile() {
		File filesDir = new File(getFileDir());
		if (filesDir.exists() && filesDir.isDirectory()) {
			File[] filelist = filesDir.listFiles(mFileNameFilter);
			if (filelist != null) {
				for (File file : filelist) {
					file.delete();
				}
			}
		}
		
		Toast.makeText(this, "All files are deleted!", Toast.LENGTH_SHORT).show();
		mBriefAdapter.clear();
		mDetailAdapter.clear();
		fileView.setText("");
	}
	
	private String getDirFilePath() {
		String tmp = getFilesDir().getAbsolutePath();
		if (Constants.IS_DEBUG) Log.d(TAG, "tmp=" + tmp);
		int pos = tmp.lastIndexOf(File.separator);
		String dir = tmp.substring(0, pos) + File.separator + "shared_prefs";
		if (Constants.IS_DEBUG) Log.d(TAG, "dir=" + dir);
		
		return dir;
	}
	
	private String getFileName(String fullName) {
		String fileName = "";
		if (fullName != null && fullName.length() > 0) {
			int lastIndex = fullName.lastIndexOf(".");
			fileName = fullName.substring(0, lastIndex);
		}
		
		return fileName;
	}
	
	private void getCellInfoFromFile(String fileName) {
		mBriefAdapter.clear();
		mDetailAdapter.clear();
		
		String fullName = getFileDir() + File.separator + fileName;
		
		try {
			String line = null;
			int section = 0;
			
			BufferedReader bReader = new BufferedReader(new FileReader(fullName));
			while((line = bReader.readLine()) != null){
				if (line.equals("简表---------------------------")) {
					section = BREIF_SECTION;
					continue;
				}
				
				if (line.equals("详表---------------------------")) {
					section = DETAIL_SECTION;
					continue;
				}
				
				if (section == BREIF_SECTION) {
					line = line.replace("(", "");
					line = line.replace(")", "");
					line = line.trim();
					
					CellLocation loc = null;
					String[] tmp = line.split(",");
					int len = tmp.length;
					if (len == 2) {
						int lac = Integer.parseInt(tmp[0]);
						int cid = Integer.parseInt(tmp[1]);
						
						loc = new GsmCellLocation();
						((GsmCellLocation)loc).setLacAndCid(lac, cid);
					} else if (len == 3) {
						int bsd = Integer.parseInt(tmp[0]);
						int sid = Integer.parseInt(tmp[1]);
						int nid = Integer.parseInt(tmp[2]);
						
						loc = new CdmaCellLocation();
						((CdmaCellLocation)loc).setCellLocationData(bsd, Integer.MAX_VALUE, Integer.MAX_VALUE, sid, nid);
					}
					
					CellLocationWrapper location = new CellLocationWrapper(loc);
					mBriefAdapter.addItem(location);
				} else if (section == DETAIL_SECTION) {
					line = line.replace("(", "");
					line = line.replace(")", "");
					line = line.trim();
					
					CellLocation loc = null;
					String[] tmp = line.split(",");
					
					int len = tmp.length;
					int asu = -1;
					long time = 0;
					if (len == 4) {
						int lac = Integer.parseInt(tmp[0]);
						int cid = Integer.parseInt(tmp[1]);
						asu = Integer.parseInt(tmp[2]);
						time = Long.parseLong(tmp[3]);
						
						loc = new GsmCellLocation();
						((GsmCellLocation)loc).setLacAndCid(lac, cid);
					} else if (len == 5) {
						
						int bsd = Integer.parseInt(tmp[0]);
						int sid = Integer.parseInt(tmp[1]);
						int nid = Integer.parseInt(tmp[2]);
						asu = Integer.parseInt(tmp[3]);
						time = Long.parseLong(tmp[4]);
						
						loc = new CdmaCellLocation();
						((CdmaCellLocation)loc).setCellLocationData(bsd, Integer.MAX_VALUE, Integer.MAX_VALUE, sid, nid);
					}
					
					CellLocationWrapper location = new CellLocationWrapper(loc);
					location.setSignalStrength(asu);
					location.setTime(time);
					mDetailAdapter.addItem(location);
				}
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getFileDir() {
		//存储路径：mnt/sdcard/collectdata/基站采集
		String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "collectdata" + File.separator + "基站采集";
		File file = new File(dirPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		return dirPath;
	}
	
	private class BriefLocationAdapter extends LocationAdapter {

		public BriefLocationAdapter(Context context) {
			super(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
			}
			
			TextView cellView = (TextView) convertView.findViewById(android.R.id.text1);
			
			String cellLocation = "";
			CellLocationWrapper location = (CellLocationWrapper) getItem(position);
			if (location.isGsm()) {
				cellLocation = mContext.getResources().getString(R.string.cell_location_brief, location.getLac(), location.getCid());
			} else {
				cellLocation = mContext.getResources().getString(R.string.cell_location_cdma_brief, location.getBaseStationId(), location.getSystemId(), location.getNetworkId());
			}
			if (Constants.IS_DEBUG) Log.d(TAG, "cellLocation=" + cellLocation);
			
			cellView.setText(cellLocation);
			
			return convertView;
		}
	}
	
	private class DetailLocationAdapter extends LocationAdapter {

		public DetailLocationAdapter(Context context) {
			super(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_2, null);
			}
			
			TextView cellView = (TextView) convertView.findViewById(android.R.id.text1);
			TextView timeView = (TextView) convertView.findViewById(R.id.time);
			
			String cellLocation = "";
			CellLocationWrapper location = (CellLocationWrapper) getItem(position);
			if (location.isGsm()) {
				cellLocation = mContext.getResources().getString(R.string.cell_location, location.getLac(), location.getCid(), location.getSignalStrength());
			} else {
				cellLocation = mContext.getResources().getString(R.string.cell_location_cdma, location.getBaseStationId(), location.getSystemId(), location.getNetworkId());
			}
			
			cellView.setText(cellLocation);
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date= new Date(location.getTime());  
			timeView.setText(formatter.format(date));
			return convertView;
		}
	}
}