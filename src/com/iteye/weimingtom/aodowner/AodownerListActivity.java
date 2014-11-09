package com.iteye.weimingtom.aodowner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class AodownerListActivity extends Activity {
	public final static String EXTRA_FILENAME = "EXTRA_FILENAME";
	public final static String EXTRA_TITLE = "EXTRA_TITLE";
	
	private final static int CONTEXT_MENU_TITLE = ContextMenu.FIRST + 1;
	private final static int CONTEXT_MENU_AUTHOR = ContextMenu.FIRST + 2;
	
	private ListView viewBookList;
	private TextView textViewTitle;
	private MenuItemAdapter adapter;
	private TextView textViewLoading;
	
	private List<MenuItemModel> models;
 	
	private String filename = null;
	private String title = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.list);
		viewBookList = (ListView) findViewById(R.id.viewBookList);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		textViewLoading = (TextView) findViewById(R.id.textViewLoading);
		
		textViewTitle.setText("列表");
		models = new ArrayList<MenuItemModel>();

		adapter = new MenuItemAdapter(this, models);
		viewBookList.setAdapter(adapter);
		viewBookList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				openContextMenu(view);
			}
		});
		registerForContextMenu(viewBookList);
		
		Intent intent = this.getIntent();
		if (intent != null) {
			filename = intent.getStringExtra(EXTRA_FILENAME);
			title = intent.getStringExtra(EXTRA_TITLE);
			if (title != null && title.length() > 0) {
				textViewTitle.setText(title);
			}
		}
		
		new LoadDataTask().execute();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		for (MenuItemModel model : models) {
			if (model != null) {
				model.recycle();
			}
		}
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
    	super.onCreateContextMenu(menu, v, info);
    	menu.add(0, CONTEXT_MENU_TITLE, 0, "搜索标题");
    	menu.add(0, CONTEXT_MENU_AUTHOR, 0, "搜索作者");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	switch (item.getItemId()) {
    	case CONTEXT_MENU_TITLE:
    		search(info.position, true);
    		return true;
    		
    	case CONTEXT_MENU_AUTHOR:
    		search(info.position, false);
    		return true;
    	}
    	return super.onContextItemSelected(item);
	}

	private void search(int pos, boolean isTitle) {
		if (pos >= 0 && pos < models.size()) {
			MenuItemModel model = models.get(pos);
			if (model != null) {
				String searchStr = null;
				if (isTitle) {
					searchStr = model.title;
				} else {
					searchStr = model.detail;
				}
				startActivity(new Intent(this, 
						AodownerSearchActivity.class)
					.putExtra(AodownerSearchActivity.EXTRA_SEARCHSTR, searchStr)
				);
			}
		}
	}
	
	private class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
		private boolean loadResult = false;
		private List<MenuItemModel> tempmodels;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			textViewLoading.setVisibility(TextView.VISIBLE);
	    	viewBookList.setVisibility(ListView.INVISIBLE);
	    	textViewLoading.setText("数据加载中...");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			InputStream instr = null;
			InputStreamReader reader = null;
			BufferedReader rbuf = null;
			loadResult = false;
			tempmodels = new ArrayList<MenuItemModel>();
			try {
				AssetManager am = getAssets();
				//"rank2012.csv"
				instr = am.open(filename);
				reader = new InputStreamReader(instr, "utf8");
				rbuf = new BufferedReader(reader);
				String line;
				while (null != (line = rbuf.readLine())) {
					String[] strs = line.split(",");
					if (strs != null && strs.length >= 2) {
						tempmodels.add(new MenuItemModel(strs[0], strs[1], null, null));
					}
				}
				loadResult = true;
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			} finally {
				if (rbuf != null) {
					try {
						rbuf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (instr != null) {
					try {
						instr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true && !AodownerListActivity.this.isFinishing()) {
				if (loadResult) {
					models.clear();
					if (tempmodels != null) {
						for (MenuItemModel model : tempmodels) {
							models.add(model);
						}
						tempmodels.clear();
					}
			    	textViewLoading.setVisibility(TextView.INVISIBLE);
			    	viewBookList.setVisibility(ListView.VISIBLE);
				} else {
					models.clear();
			    	textViewLoading.setVisibility(TextView.VISIBLE);
			    	viewBookList.setVisibility(ListView.INVISIBLE);
			    	textViewLoading.setText("数据加载失败");
				}
			} else if (result == false) {
				finish();
			}
			adapter.notifyDataSetChanged();
		}
    }
}
