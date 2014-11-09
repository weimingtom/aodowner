package com.iteye.weimingtom.aodowner;

import android.content.Intent;
import android.os.Environment;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * IntentService会自动开启线程，不会阻塞用户界面线程
 * 
 * @see https://github.com/jonasevcik/Japanese-dictionary-Android/blob/master/src/cz/muni/fi/japanesedictionary/parser/ParserService.java
 * @author Administrator
 *
 */
public class AodownerDownloadService extends IntentService {
	public final static String SAVE_PATH = "/aodowner";
	
	public final static String EXTRA_URL = "EXTRA_URL";
	public final static String EXTRA_FILENAME = "EXTRA_FILENAME";
	
	public final static String SERVICE_NAME = "com.iteye.weimingtom.aodowner.AodownerDownloadService";
	public static final String DICTIONARY_PREFERENCES = "cz.muni.fi.japanesedictionary";

	public final static String SERVICE_DONE = "serviceDone";
	public final static String SERVICE_PROGRESS = "serviceProgress";
	
	public final static String SERVICE_EXTRA_ISPARSING = "parsing";
	public final static String SERVICE_EXTRA_CURRENT = "current";
	public final static String SERVICE_EXTRA_PARSERESULT = "parseResult";
	
	private InputStream input = null;
	private OutputStream output = null;
//	private NotificationManager mNotifyManager = null;
//	NotificationCompat.Builder mBuilder = null;
	private boolean canceled = false;
	private boolean complete = false;
	
	public AodownerDownloadService() {
		super("AodownerDownloadService");
	}

	@Override
	public void onCreate(){
		super.onCreate();
//		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		mBuilder = new NotificationCompat.Builder(this);
//		mBuilder.setContentTitle("下载进度")
//			.setContentText("进度：" + " 0 %")
//			.setSmallIcon(R.drawable.ic_launcher);
//		mBuilder.setAutoCancel(true);
//		Intent resultIntent = new Intent(getApplicationContext(), AodownerDownloadActivity.class);
//		PendingIntent resultPendingIntent =
//				PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//		mBuilder.setContentIntent(resultPendingIntent);
//		mNotifyManager.notify(0, mBuilder.build());
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null && intent.hasExtra(EXTRA_URL)) {
			String urlStr = intent.getStringExtra(EXTRA_URL);
			String fileName = intent.getStringExtra(EXTRA_FILENAME);
			
			URL url = null;
			URLConnection connection = null;
			long fileLength;
			String path;
			try{
				//"http://www.aozora.gr.jp/cards/000148/files/789_ruby_5639.zip"
				url = new URL(urlStr);
				connection = url.openConnection();
				//connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.3.3; zh-cn; HTC_WildfireS_A510e Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
				connection.connect();
				fileLength = connection.getContentLength();
				input = new BufferedInputStream(url.openStream(), 1024);
				File karta = null;
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					karta = getExternalCacheDir();
				}
				if (karta == null) {
					karta = getCacheDir();
				}
				path = karta.getPath() + File.separator + "data.zip";	
				output = new FileOutputStream(path);
	            byte[] data = new byte[1024];
	            int total = 0;
	            int count;
	            int perc = 0;
	            while ((count = input.read(data)) != -1){
	                total += count;
	                output.write(data, 0, count);
	                int persPub = Math.round((((float)total / fileLength) * 100));
	                if (perc < persPub) {
//	                	mBuilder.setContentText("进度：" + persPub + " %");
//		                mNotifyManager.notify(0, mBuilder.build());
		                publishProgress(persPub);
		                perc = persPub;
	                }
	            	if (canceled) {
	            		closeIOStreams();
	            		return;
	            	}
	            }
	            output.flush(); 
//	            mBuilder.setContentText("下载完成");
//	            mNotifyManager.notify(0, mBuilder.build());
	            parseFile(path, fileName, urlStr);
	        } catch (IOException e) {
				e.printStackTrace();
			} finally {
				closeIOStreams();
			}
			stopSelf();
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		closeIOStreams();
		if (!complete) {
//            mBuilder.setContentText("下载未完成");
//            mNotifyManager.notify(0, mBuilder.build());
		}
	}
	
	private void closeIOStreams(){
		canceled = true;
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			input = null;
		}
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			output = null;
		}
	}
	
	private void publishProgress(int current){
		Intent intent = new Intent(SERVICE_PROGRESS);
		if (current == -1){
			intent.putExtra(SERVICE_EXTRA_ISPARSING, true);
		}
		intent.putExtra(SERVICE_EXTRA_CURRENT, current);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);	
	}
	
	private void parseFile(String path, String fileName, String url) {
		publishProgress(-1);
//        mBuilder.setContentText("正在读取中");
//        mNotifyManager.notify(0, mBuilder.build());
//        
//		mBuilder.setContentText("下载完成");
//		Intent intent = new Intent(getApplicationContext(), AodownerDownloadActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		intent.putExtra(DICTIONARY_PREFERENCES, true);
//		PendingIntent resultPendingIntent =
//				PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//		mBuilder.setContentIntent(resultPendingIntent);
//        mNotifyManager.notify(0, mBuilder.build());
		
		String targetPath = createExternalFile(this, fileName);
		if (targetPath != null) {
			boolean result = false;
			if (url != null && url.toLowerCase().endsWith(".zip")) {
				result = unzip(path, targetPath);
			} else {
				if (url != null) {
					int index = url.lastIndexOf(".");
					if (index >= 0) {
						String endStr = url.substring(index);
						targetPath.replace(".txt", endStr);
					}
				}
				result = copyto(path, targetPath);
			}
			if (result) {
				complete = true;
				serviceSuccessfullyDone();
			} else {
				complete = true;
				serviceFailDone();
			}
		} else {
			complete = true;
			serviceFailDone();
		}
	}
	
	private boolean unzip(String path, String targetPath) {
		boolean result = false;
    	InputStream in = null;
    	OutputStream out = null;
    	ZipFile zip = null;
    	try {
            zip = new ZipFile(path);
            if (zip.entries().hasMoreElements()) {
	            ZipEntry entry = zip.entries().nextElement();
	            if (entry != null) {
	            	in = zip.getInputStream(entry);
	            	out = new FileOutputStream(targetPath);
		            byte[] data = new byte[1024];
		            int count;
		            while ((count = in.read(data)) != -1){
		                out.write(data, 0, count);
		            }
		            out.flush();
		            result = true;
	            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	if (out != null) {
        		try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
         	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	if (zip != null) {
        		try {
					zip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
		return result;
	}
	
	private boolean copyto(String path, String targetPath) {
		boolean result = false;
    	InputStream in = null;
    	OutputStream out = null;
    	try {
        	in = new FileInputStream(path);
        	out = new FileOutputStream(targetPath);
            byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data)) != -1){
                out.write(data, 0, count);
            }
            out.flush();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	if (out != null) {
        		try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
         	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
		return result;
	}
	
	private void serviceSuccessfullyDone() {
        Intent intent = new Intent(SERVICE_DONE);
        intent.putExtra(SERVICE_EXTRA_PARSERESULT, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);	
	}
	
	private void serviceFailDone() {
        Intent intent = new Intent(SERVICE_DONE);
        intent.putExtra(SERVICE_EXTRA_PARSERESULT, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);	
	}
	
    public static String createExternalFile(Context context, String filename) {
        File path = new File(Environment.getExternalStorageDirectory() +
        		SAVE_PATH);
        File file = new File(path, filename);
        
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        if (mExternalStorageAvailable && mExternalStorageWriteable) {
        	path.mkdirs();
        	return file.getAbsolutePath();
        } else {
        	//throw new IllegalArgumentException("无法创建目录");
        	return null;
        }
    }
}
