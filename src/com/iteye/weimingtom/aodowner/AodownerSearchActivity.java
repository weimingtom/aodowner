package com.iteye.weimingtom.aodowner;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AodownerSearchActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "AodownerSearchActivity";
	
	private final static String ASSET_DATABASE_NAME = "aozoralist.ogg";
	private final static String DATABASE_NAME = "aozoralist.sqlite";
	
	private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_SEARCH_TYPE = "searchType";
	
	public static final String EXTRA_SEARCHSTR = "EXTRA_SEARCHSTR";
	
	private ListView viewBookList;
	private TextView textViewTitle;
	private MenuItemAdapter adapter;
	private List<MenuItemModel> models;
	
	private EditText editTextSearch;
	private Button buttonSearch;
	
	private String dataBaseFileName;
	private TextView textViewLoading;
	private LinearLayout linearLayoutTitle;
	
	private final static int SEARCH_TYPE_FULLTEXT = 0;
	private final static int SEARCH_TYPE_TITLE = 1;
	private final static int SEARCH_TYPE_AUTHOR = 2;
	
	private SearchTask searchTask;

	private Spinner spinnerSearchType;
	private ArrayAdapter<String> spinnerSearchTypeAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.search);
		
		viewBookList = (ListView) findViewById(R.id.viewBookList);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		editTextSearch = (EditText) findViewById(R.id.editTextSearch);
		buttonSearch = (Button) findViewById(R.id.buttonSearch);
		textViewLoading = (TextView) findViewById(R.id.textViewLoading);
		linearLayoutTitle = (LinearLayout) this.findViewById(R.id.linearLayoutTitle);
        spinnerSearchType = (Spinner) this.findViewById(R.id.spinnerSearchType);

        
		textViewTitle.setText("作品搜索");
		textViewTitle.setTextColor(0xff000000);
		linearLayoutTitle.setBackgroundColor(0xfffcabbd);
		
		models = new ArrayList<MenuItemModel>();

		adapter = new MenuItemAdapter(this, models);
		viewBookList.setAdapter(adapter);
		viewBookList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position >= 0 && position < models.size()) {
					MenuItemModel model = models.get(position);
					if (model != null) {
						startActivity(new Intent(AodownerSearchActivity.this, AodownerDownloadActivity.class)
							.putExtra(AodownerDownloadActivity.EXTRA_BOOKNAME, model.bookname)
							.putExtra(AodownerDownloadActivity.EXTRA_SUBNAME, model.subname)
							.putExtra(AodownerDownloadActivity.EXTRA_AUTHOR, model.author)
							.putExtra(AodownerDownloadActivity.EXTRA_ZIPURL, model.zipurl)
							.putExtra(AodownerDownloadActivity.EXTRA_CARDURL, model.cardurl)
						);
					}
				}
			}
		});
		buttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSearch();
			}
		});

        spinnerSearchTypeAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
        spinnerSearchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSearchTypeAdapter.add("全文"); // SEARCH_TYPE_FULLTEXT
        spinnerSearchTypeAdapter.add("标题"); // SEARCH_TYPE_TITLE
        spinnerSearchTypeAdapter.add("作者"); // SEARCH_TYPE_AUTHOR
        spinnerSearchType.setAdapter(spinnerSearchTypeAdapter);
        spinnerSearchType.setSelection(getLastSearchType());
        spinnerSearchType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int postion, long id) {
				setLastSearchType(postion);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
        });
        
        Intent intent = this.getIntent();
        if (intent != null) {
        	String searchStr = intent.getStringExtra(EXTRA_SEARCHSTR);
        	if (searchStr != null && searchStr.length() > 0) {
        		this.editTextSearch.setText("");
        		this.editTextSearch.append(searchStr);
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
	
    private void startSearch() {
		String keyword = editTextSearch.getText().toString();
		if (searchTask == null && keyword != null && keyword.length() > 0) {
			searchTask = new SearchTask();
			searchTask.execute(keyword, Integer.toString(this.spinnerSearchType.getSelectedItemPosition()));
		} else {
			showSearchKeywordEmpty();
		}
    }
	
    public static String createExternalDatabase(Context context) {
        File path = new File(Environment.getExternalStorageDirectory() +
        	"/Android/data/" + context.getPackageName());
        File file = new File(path, DATABASE_NAME);
        
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
	
    public static String copyDatabase(Context context) {
    	if (D) {
    		Log.d(TAG, "Copying words...");
    	}
    	String dbname = createExternalDatabase(context);
    	if (dbname != null) {
	    	final AssetManager assetManager = context.getAssets();
	    	InputStream inputStream = null;
	        BufferedInputStream istr = null; 
	        OutputStream outputStream = null;
	        BufferedOutputStream ostr = null;
	        try {
	        	inputStream = assetManager.open(ASSET_DATABASE_NAME);
	        	istr = new BufferedInputStream(inputStream);
	        	outputStream = new FileOutputStream(dbname);
	        	ostr = new BufferedOutputStream(outputStream);
	        	byte[] bytes = new byte[2048];
	            int size = 0;
	            while (true) {
	                size = istr.read(bytes);
	                if (size >= 0) {
	                	ostr.write(bytes, 0, size);
	                	ostr.flush();
	                } else {
	                	break;
	                }
	            }
	            return dbname;
	        } catch (IOException e) {
		        if (D) {
		        	Log.e(TAG, "copying words error!!!");
		        }
	        	e.printStackTrace();
	        } finally {
	        	if (ostr != null) {
	        		try {
	        			ostr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
	        	if (outputStream != null) {
	        		try {
	        			outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
	        	if (istr != null) {
		        	try {
						istr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
	        	if (inputStream != null) {
		        	try {
		        		inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
		        if (D) {
		        	Log.d(TAG, "DONE copying words.");
		        }
	        }
    	} else {
	        if (D) {
	        	Log.e(TAG, "copying words error!!!");
	        }	
    	}
    	return null;
    }
	
    private void showSearchLoadingSuccess() {
    	textViewLoading.setVisibility(TextView.INVISIBLE);
    	viewBookList.setVisibility(ListView.VISIBLE);
    	buttonSearch.setEnabled(true);
    	//Toast.makeText(this, "数据库复制成功", Toast.LENGTH_SHORT).show();
    }
    
    private void showSearchLoadingFailed() {
    	textViewLoading.setVisibility(TextView.VISIBLE);
    	viewBookList.setVisibility(ListView.INVISIBLE);
    	buttonSearch.setEnabled(false);
    	textViewLoading.setText("数据库复制失败，请检查sd卡是否可写入");
    }
    
    private void showSearchStart() {
    	textViewLoading.setVisibility(TextView.VISIBLE);
    	viewBookList.setVisibility(ListView.INVISIBLE);
    	textViewLoading.setText("搜索中...");
    }
    
    private void showSearchFailed() {
    	//Toast.makeText(this, "搜索失败", Toast.LENGTH_SHORT).show();
    	textViewLoading.setVisibility(TextView.VISIBLE);
    	viewBookList.setVisibility(ListView.INVISIBLE);
    	textViewLoading.setText("搜索失败");
    }
    
    private void showSearchKeywordEmpty() {
    	//Toast.makeText(this, "关键词为空", Toast.LENGTH_SHORT).show();
    	textViewLoading.setVisibility(TextView.VISIBLE);
    	viewBookList.setVisibility(ListView.INVISIBLE);
    	textViewLoading.setText("关键词为空");
    }
    
    private void showSearchEnd(String keyword, int count) {
    	if (D) {
    		Log.d(TAG, "showSearchEnd");
    	}
    	if (count > 0) {
        	textViewLoading.setVisibility(TextView.INVISIBLE);
        	viewBookList.setVisibility(ListView.VISIBLE);
    	} else {
        	textViewLoading.setVisibility(TextView.VISIBLE);
        	viewBookList.setVisibility(ListView.INVISIBLE);
        	textViewLoading.setText("搜索结果：0\n请选择全文搜索或缩短关键词");
    	}
    }
    
    private void setLastSearchType(int searchType) {
		Editor e = getSharedPreferences(SHARE_PREF_NAME, MODE_PRIVATE).edit();
		e.putInt(SHARE_PREF_SEARCH_TYPE, searchType);
		e.commit();
    }
    
    private int getLastSearchType() {
    	SharedPreferences sp = getSharedPreferences(SHARE_PREF_NAME, MODE_PRIVATE);
		return sp.getInt(SHARE_PREF_SEARCH_TYPE, 0);
    }
    
	private class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
		private boolean loadResult = false;
		private String dbname;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
	    	buttonSearch.setEnabled(false);
	    	textViewLoading.setVisibility(TextView.VISIBLE);
	    	viewBookList.setVisibility(ListView.INVISIBLE);
	    	textViewLoading.setText("数据加载中...");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				dbname = copyDatabase(AodownerSearchActivity.this); 
				if (dbname != null) {
					loadResult = true;
				} else {
					loadResult = false;
				}
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true && !AodownerSearchActivity.this.isFinishing()) {
				if (loadResult) {
					dataBaseFileName = dbname;
					showSearchLoadingSuccess();
					String keyword = editTextSearch.getText().toString();
					if (keyword != null && keyword.length() > 0) {
						startSearch();
					}
				} else {
					dataBaseFileName = null;
					showSearchLoadingFailed();
				}
			} else if (result == false) {
				dataBaseFileName = null;
				finish();
			}
		}
    }
	
	private class SearchTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		private int resultNum = 0;
		private String keyword = "";
		private int type = SEARCH_TYPE_FULLTEXT;
		private List<MenuItemModel> tempmodels;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			buttonSearch.setEnabled(false);
			models.clear();
			adapter.notifyDataSetChanged();
			showSearchStart();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (params[0] != null) {
				keyword = params[0].replace(" ", "%");
			} else {
				keyword = "%";
			}
			try {
				type = Integer.parseInt(params[1]);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			tempmodels = new ArrayList<MenuItemModel>();
			try {
				SQLiteDatabase db = null;
				boolean result = true;
				String dbPath = dataBaseFileName;
				try {
					db = SQLiteDatabase.openDatabase(dbPath, 
						null, 
						SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
					Cursor cursor = null;
					String selection = null;
					String[] selectionArgs = null;
					String[] columns = null;
					switch(type) {
					case SEARCH_TYPE_FULLTEXT:
						selection = "bookname1 LIKE ? OR " + 
								"bookname2 LIKE ? OR " + 
								"subname1 LIKE ? OR " + 
								"subname2 LIKE ? OR " + 
								"author1 LIKE ? OR " + 
								"author2 LIKE ? ";
						selectionArgs = new String[]{
							"%" + keyword + "%", 
							"%" + keyword + "%", 
							"%" + keyword + "%",
							"%" + keyword + "%",
							"%" + keyword + "%",
							"%" + keyword + "%",
							};
						break;
						
					case SEARCH_TYPE_TITLE:
						selection = "bookname1 LIKE ? OR " + 
								"bookname2 LIKE ? OR " + 
								"subname1 LIKE ? OR " + 
								"subname2 LIKE ? ";
						selectionArgs = new String[]{
							"%" + keyword + "%", 
							"%" + keyword + "%", 
							"%" + keyword + "%",
							"%" + keyword + "%",
							};
						break;
						
					case SEARCH_TYPE_AUTHOR:
						selection = "author1 LIKE ? OR " + 
								"author2 LIKE ? ";
						selectionArgs = new String[]{
							"%" + keyword + "%", 
							"%" + keyword + "%",
							};
						break;
					}
					columns = new String[]{
						"bookname1", 
						"subname1",
						"author1",
						"zipurl",
						"cardurl",
					};
					cursor = db.query(false, //destinct
							"aozoralist", //table
							columns, //columns
							selection, //"word LIKE ?", //selection
							selectionArgs, //new String[]{"%明日%"}, //selectionArgs
							null, //groupBy
							null, //having
							"bookname1" + " ASC", //orderBy
							null); //"0, 10"); //limit
					try {
						resultNum = 0;
						while (cursor.moveToNext()) {
							String bookname = cursor.getString(0);
							String subname = cursor.getString(1);
							String author = cursor.getString(2);
							String zipurl = cursor.getString(3);
							String cardurl = cursor.getString(4);
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
							MenuItemModel model = new MenuItemModel(title.toString(), detail.toString(), null, null);
							model.bookname = bookname;
							model.subname = subname;
							model.author = author;
							model.zipurl = zipurl;
							model.cardurl = cardurl;
							tempmodels.add(model);
							resultNum++;
						}
					} catch (Throwable e) {
						e.printStackTrace();
						result = false;
					} finally {
						if (cursor != null) {
							cursor.close();
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
					result = false;
				} finally {
					if (db != null) {
						db.close();
					} 
				}
				loadResult = result;
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true && !AodownerSearchActivity.this.isFinishing()) {
				models.clear();
				if (tempmodels != null) {
					for (MenuItemModel model : tempmodels) {
						models.add(model);
					}
					tempmodels.clear();
				}
				adapter.notifyDataSetChanged();
				buttonSearch.setEnabled(true);
				viewBookList.requestFocus();
				viewBookList.requestFocusFromTouch();
				if (loadResult) {
					showSearchEnd(keyword, resultNum);
					if (resultNum > 0) {
						InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						inputMethodManager.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
					}
				} else {
					models.clear();
					adapter.notifyDataSetChanged();
					showSearchFailed();
				}
			} else if (result == false) {
				models.clear();
				adapter.notifyDataSetChanged();
				dataBaseFileName = null;
				finish();
			}
			searchTask = null;
			adapter.notifyDataSetChanged();
		}
    }
}
