package com.pipi.workhouse.telephony.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pipi.workhouse.telephony.R;
import com.pipi.workhouse.telephony.common.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SelectFileActivity extends Activity {
	private static final String TAG = "SelectFileActivity";
	
	private ListView mListView;
	private View emptyView;
	
	private List<Map<String, Object>> mListData = new ArrayList<Map<String, Object>>();
	private SimpleAdapter mAdapter = null;
	
	private FilenameFilter mFileNameFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			String regularExpression = ".*_CELL[.]xml";
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
		
		setContentView(R.layout.select_file_layout);
		
		initViews();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		initList();
		
		if (mListData.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
			mAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
		}
		
	}
	
	public void onBack(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	private void initViews() {
		mListView = (ListView) findViewById(android.R.id.list);
		emptyView = findViewById(R.id.empty_view);
		
		mAdapter = new SimpleAdapter(this, mListData,
                android.R.layout.simple_list_item_1,
                new String[] { "title" },
                new int[] { android.R.id.text1 }) {
			@Override
			public View getView(int position, View convertView,
					ViewGroup parent) {
				TextView view = (TextView) super.getView(position, convertView, parent);
				
				view.setTextColor(getResources().getColor(R.color.text_color_black));
				view.setTextSize(18);
				return view;
			}
		};
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Map<String, Object> item = mListData.get(position);
				
				String filePath = (String) item.get("path");
				Intent data = new Intent();
				data.putExtra("PATH", filePath);
				setResult(RESULT_OK, data);
				finish();
			}
			
		});
	}
	
	private void initList() {
		List<Map<String, Object>> retListData = mListData;
		retListData.clear();
		
		String tmp = getFilesDir().getAbsolutePath();
		if (Constants.IS_DEBUG) Log.d(TAG, "tmp=" + tmp);
		int pos = tmp.lastIndexOf("/");
		String dir = tmp.substring(0, pos);
		if (Constants.IS_DEBUG) Log.d(TAG, "dir=" + dir);
		
		File filesDir = new File(dir, "shared_prefs");
		if (filesDir.exists() && filesDir.isDirectory()) {
			String[] filelist = filesDir.list(mFileNameFilter);
			if (filelist != null) {
				for (String fileName : filelist) {
					Map<String, Object> item = new HashMap<String, Object>();
					
					item.put("title", fileName);
					item.put("path",/*dir + File.separator +*/ fileName);
					retListData.add(item);
				}
			}
		}
	}

	
	private void listFiles() {
		String[] files;
		
		File dirFile = getFilesDir();
		files = dirFile.list();
		
		if (files == null) {
			emptyView.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		} else {
			
		}
	}
}