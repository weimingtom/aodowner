package com.iteye.weimingtom.aodowner;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AodownerAboutActivity extends Activity {
	private TextView textViewTitle;
	private Button sendmail;
	private LinearLayout linearLayoutTitle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.about);
		
		textViewTitle = (TextView) this.findViewById(R.id.textViewTitle);
		sendmail = (Button)this.findViewById(R.id.sendmail);
		linearLayoutTitle = (LinearLayout) this.findViewById(R.id.linearLayoutTitle);
		
		textViewTitle.setText("关于");
		textViewTitle.setTextColor(0xff000000);
		linearLayoutTitle.setBackgroundColor(0xfffcabbd);
		
		sendmail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SENDTO);
				intent.setData(Uri.parse("mailto:" + getString(R.string.mail_address)));
				intent.putExtra(Intent.EXTRA_SUBJECT, "关于 " + getString(R.string.app_name) + " v" + getString(R.string.version_number));
				//intent.putExtra(Intent.EXTRA_TEXT, "我想说：\n");
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(AodownerAboutActivity.this, "找不到发送邮件的应用程序。\n我的邮箱地址在帮助信息中。", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});
		Button back = (Button)this.findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
