package com.pipi.workhouse.telephony.activity;

import com.pipi.workhouse.telephony.R;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.help_layout);
		
		initViews();
	}
	
	private void initViews() {
		TextView versionView = (TextView) findViewById(R.id.version);
		
		PackageInfo info;
		try {
			info = getPackageManager().getPackageInfo(this.getPackageName(), 0);
			versionView.setText("当前版本：" + info.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}