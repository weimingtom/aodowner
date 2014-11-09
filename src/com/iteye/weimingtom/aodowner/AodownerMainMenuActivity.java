package com.iteye.weimingtom.aodowner;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AodownerMainMenuActivity extends Activity {
	private ListView viewBookList;
	private TextView textViewTitle;
	private MenuItemAdapter adapter;
	
	private List<MenuItemModel> models;
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.main_menu);
		viewBookList = (ListView) findViewById(R.id.viewBookList);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		
		textViewTitle.setText(this.getString(R.string.app_name));
		models = new ArrayList<MenuItemModel>();
		
		models.add(new MenuItemModel("作品搜索", "搜索作品数据库", null, null));
		models.add(new MenuItemModel("排行榜", "2012年文本版排行", null, null));
		models.add(new MenuItemModel("古典文学", "古典文学", null, null));
		models.add(new MenuItemModel("近代現代文学", "近代現代文学", null, null));
		models.add(new MenuItemModel("青空文庫", "http://www.aozora.gr.jp/", null, null));
		models.add(new MenuItemModel("关于", "应用信息与联系方式", null, null));
		
		adapter = new MenuItemAdapter(this, models);
		viewBookList.setAdapter(adapter);
		viewBookList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch(position) {
				case 0:
					startActivity(new Intent(AodownerMainMenuActivity.this, 
							AodownerSearchActivity.class));
					break;
					
				case 1:
					startActivity(new Intent(AodownerMainMenuActivity.this, 
							AodownerListActivity.class)
						.putExtra(AodownerListActivity.EXTRA_FILENAME, "rank2012.csv")
						.putExtra(AodownerListActivity.EXTRA_TITLE, "排行榜")
					);
					break;
					
				case 2:
					startActivity(new Intent(AodownerMainMenuActivity.this, 
							AodownerListActivity.class)
						.putExtra(AodownerListActivity.EXTRA_FILENAME, "denshitext1.csv")
						.putExtra(AodownerListActivity.EXTRA_TITLE, "古典文学")
					);
					break;
					
				case 3:
					startActivity(new Intent(AodownerMainMenuActivity.this, 
							AodownerListActivity.class)
						.putExtra(AodownerListActivity.EXTRA_FILENAME, "denshitext2.csv")
						.putExtra(AodownerListActivity.EXTRA_TITLE, "近代現代文学")
					);
					break;
					
				case 4:
		    		Intent intent = new Intent();
		    		intent.setAction(Intent.ACTION_VIEW);
		    		intent.setDataAndType(Uri.parse("http://www.aozora.gr.jp/"), "*/*");
		    		try {
		    			startActivity(intent);
		    		} catch (Throwable e) {
		    			e.printStackTrace();
		    			Toast.makeText(AodownerMainMenuActivity.this, 
		    				"打开网页出错", Toast.LENGTH_SHORT)
		    				.show();
		    		}
					break;
					
				case 5:
					startActivity(new Intent(AodownerMainMenuActivity.this, 
							AodownerAboutActivity.class));
					break;
				}
			}
		});
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
}
