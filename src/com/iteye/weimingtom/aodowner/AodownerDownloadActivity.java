package com.iteye.weimingtom.aodowner;

import java.io.File;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AodownerDownloadActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "AodownerActivity";
	
	public final static String EXTRA_BOOKNAME = "bookname"; 
	public final static String EXTRA_SUBNAME = "subname";
	public final static String EXTRA_AUTHOR = "author";
	public final static String EXTRA_ZIPURL = "zipurl";
	public final static String EXTRA_CARDURL = "cardurl";
	
	private Button buttonBeginDownload;
	private TextView textViewPercent;
	private ProgressBar progressBarDownload;
	private TextView textViewLog;
	private Button buttonClearLog;
	private TextView textViewTitle;
	private TextView textViewInfo;
	private Button buttonOpenWeb;
	
	private final static String TEST_URL = "http://www.aozora.gr.jp/cards/000148/files/789_ruby_5639.zip";
	
	private String bookname, subname, author, zipurl, cardurl;
	
	private BroadcastReceiver mReceiverDone = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	boolean parseResult = intent.getBooleanExtra(AodownerDownloadService.SERVICE_EXTRA_PARSERESULT, true);
	    	finishDownload(parseResult);
	    }
	};

	private BroadcastReceiver mReceiverProgress = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        int current = intent.getIntExtra(AodownerDownloadService.SERVICE_EXTRA_CURRENT, 0);
	    	boolean parsing = intent.getBooleanExtra(AodownerDownloadService.SERVICE_EXTRA_ISPARSING, false);
	    	if (parsing) {
	    		progressBarDownload.setIndeterminate(true);
	    		progressBarDownload.setProgress(0);
    		   	log("解压处理中...");
	    	} else {
	    		textViewPercent.setText(current + "%");
	    		progressBarDownload.setProgress(current);
	    	}
	    }
	};
	
	@Override
	public void onResume(){
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverDone, new IntentFilter(AodownerDownloadService.SERVICE_DONE));
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverProgress,new IntentFilter(AodownerDownloadService.SERVICE_PROGRESS));
	}

	@Override
	public void onPause(){
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverDone);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverProgress);
	}

	@Override
	public void onDestroy(){
		Intent intent = new Intent(this, AodownerDownloadService.class);
    	stopService(intent);
    	super.onDestroy();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download);
        
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        buttonBeginDownload = (Button) this.findViewById(R.id.buttonBeginDownload);
        textViewPercent = (TextView) this.findViewById(R.id.textViewPercent);
        progressBarDownload = (ProgressBar) this.findViewById(R.id.progressBarDownload);
        textViewLog = (TextView) this.findViewById(R.id.textViewLog);
        buttonClearLog = (Button) this.findViewById(R.id.buttonClearLog);
        textViewInfo = (TextView) this.findViewById(R.id.textViewInfo);
        buttonOpenWeb = (Button) this.findViewById(R.id.buttonOpenWeb);
        
        textViewTitle.setText("文件下载");
        
        buttonBeginDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadDictionary();
			}
        });
        buttonOpenWeb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openWeb();
			}
        });
        buttonClearLog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearLog();
			}
        });
        
        Intent intent = this.getIntent();
        if (intent != null) {
        	bookname = intent.getStringExtra(EXTRA_BOOKNAME); 
        	subname = intent.getStringExtra(EXTRA_SUBNAME);
        	author = intent.getStringExtra(EXTRA_AUTHOR);
        	zipurl = intent.getStringExtra(EXTRA_ZIPURL);
        	cardurl = intent.getStringExtra(EXTRA_CARDURL);
        }
		StringBuffer title = new StringBuffer();
		StringBuffer detail = new StringBuffer();
		
		if (bookname != null && bookname.length() > 0) {
			title.append("「" + bookname + "」");
		}
		if (subname != null && subname.length() > 0) {
			title.append("\n");
			title.append(subname);
		}
		if (author != null && author.length() > 0) {
			detail.append("作者：" + author);
		}
		if (zipurl != null && zipurl.length() > 0) {
			detail.append("\n");
			detail.append("下载地址：" + zipurl);
		}
		if (cardurl != null && cardurl.length() > 0) {
			detail.append("\n");
			detail.append("网页地址：" + cardurl);
		}
		textViewInfo.setText("下载信息：\n" + title.toString() + "\n" + detail.toString());
    }
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (AodownerDownloadService.SERVICE_NAME.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public void downloadDictionary(){
		ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    if (networkInfo != null && networkInfo.isConnected()) {
	    	if (zipurl != null && zipurl.length() > 0) {
		    	log("开始下载：" + zipurl);
				progressBarDownload.setIndeterminate(false);
				progressBarDownload.setMax(100);
				StringBuffer fileNameBuf = new StringBuffer();
				if (author != null && author.length() > 0) {
					fileNameBuf.append("[" + author + "]");
				}
				if (bookname != null && bookname.length() > 0) {
					fileNameBuf.append("[" + bookname + "]");
				}
				if (subname != null && subname.length() > 0) {
					fileNameBuf.append("[" + subname + "]");
				}
				fileNameBuf.append(".txt");
				String fileName = fileNameBuf.toString();
				fileName = fileName.replace("&", "_");
				fileName = fileName.replace("/", "_");
				fileName = fileName.replace("\\", "_");
				File fileSD = Environment.getExternalStorageDirectory();
				String fileSDPath = "";
				if (fileSD != null) {
					fileSDPath = fileSD.getAbsolutePath() + AodownerDownloadService.SAVE_PATH + "/";
				}
				log("保存文件名：" + fileSDPath + fileName);
				startService(new Intent(this, AodownerDownloadService.class)
					.putExtra(AodownerDownloadService.EXTRA_URL, zipurl)
					.putExtra(AodownerDownloadService.EXTRA_FILENAME, fileName)
				);
	    	} else {
	    		log("下载文件的链接为空，无法下载");
	    	}
	    } else {
	    	log("网络未连接");
	    }
	}
	
	private void finishDownload(boolean parseResult){
		progressBarDownload.setIndeterminate(false);
		progressBarDownload.setProgress(0);
		progressBarDownload.invalidate();
		if (parseResult) {
			log("下载完成，解压成功");
		} else {
			log("下载完成，解压失败");
		}
	}
	
	private void clearLog() {
		textViewLog.setText("");
	}
	
	private void log(String text) {
		if (text != null) {
			textViewLog.append(text + "\n");
		}
	}
	
	private void openWeb() {
		if (this.cardurl != null && this.cardurl.length() > 0) {
    		Intent intent = new Intent();
    		intent.setAction(Intent.ACTION_VIEW);
    		intent.setDataAndType(Uri.parse(this.cardurl), "*/*");
    		try {
    			startActivity(intent);
    		} catch (Throwable e) {
    			e.printStackTrace();
    			Toast.makeText(this, 
    				"打开网页出错", Toast.LENGTH_SHORT)
    				.show();
    		}
		} else {
			Toast.makeText(this, 
					"网址为空", Toast.LENGTH_SHORT)
					.show();
		}
	}
}
