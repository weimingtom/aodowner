package com.iteye.weimingtom.aodowner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MenuItemModel {
	public String title;
	public String detail;
	public String imageSrc;
	public String progress;
	public Bitmap bitmap;
	
	public String bookname;
	public String subname;
	public String author;
	public String zipurl;
	public String cardurl;
	
	public MenuItemModel(String title, String detail, String imageSrc, String progress) {
		this.title = title;
		this.detail = detail;
		this.imageSrc = imageSrc;
		this.progress = progress;
		if (this.imageSrc != null) {
			this.bitmap = BitmapFactory.decodeFile(this.imageSrc);
		}
	}
	
	public void recycle() {
		if (this.bitmap != null) {
			this.bitmap.recycle();
			this.bitmap = null;
		}
	}
}
