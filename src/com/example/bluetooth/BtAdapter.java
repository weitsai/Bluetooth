package com.example.bluetooth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class BtAdapter extends BaseAdapter {
	private LayoutInflater mLayoutInflater = null;
	private View mInflater = null;
	private List<String> mData = null;
	private Context context = null;

	BtAdapter(Context context, HashSet<String> data) {
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mData = new ArrayList<String>(data);
		this.context = context;
	}

	public int getCount() {
		return mData.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (mData != null && mData.size() > 0) {
			mInflater = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, null);
			((TextView) mInflater.findViewById(android.R.id.text1)).setText(mData.get(position));
		}
		return mInflater;
	}

}
