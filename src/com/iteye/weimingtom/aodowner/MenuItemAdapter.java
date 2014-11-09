package com.iteye.weimingtom.aodowner;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuItemAdapter extends BaseAdapter{
	private LayoutInflater inflater;
	private List<MenuItemModel> models;

	public MenuItemAdapter(Context context, List<MenuItemModel> models){
		this.inflater = LayoutInflater.from(context);
		this.models = models;
	}

	@Override
	public int getCount() {
		if (models == null) {
			return 0;
		}
		return models.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= getCount()){
			return null;
		}
		return models.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null){
			convertView = inflater.inflate(R.layout.menu_item_adapter, null);
			holder = new ViewHolder();
			holder.sItemTitle = (TextView)convertView.findViewById(R.id.sItemTitle);
			holder.sItemInfo = (TextView)convertView.findViewById(R.id.sItemInfo);
			holder.sItemImage = (ImageView)convertView.findViewById(R.id.sItemImage);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		MenuItemModel model = models.get(position);
		if (model != null) {
			holder.sItemTitle.setText(model.title);
			if (model.progress != null) {
				holder.sItemTitle.append(model.progress);
			}
			holder.sItemInfo.setText(model.detail);
			if (model.imageSrc != null) {
				//holder.sItemImage.setImageURI(Uri.fromFile(new File(model.imageSrc)));
				holder.sItemImage.setImageBitmap(model.bitmap);
			}
		}
		return convertView;
	}
	
    private static final class ViewHolder {
    	TextView sItemTitle;
    	TextView sItemInfo;
    	ImageView sItemImage;
    }
}
