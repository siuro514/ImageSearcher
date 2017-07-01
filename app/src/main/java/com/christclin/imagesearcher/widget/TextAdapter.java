package com.christclin.imagesearcher.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.christclin.imagesearcher.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 2017/7/1.
 */

public class TextAdapter extends BaseAdapter {

    private List<String> mData = new ArrayList<>();

    private Context mContext;
    private Integer mTextColor = null;

    public TextAdapter(Context context) {
        mContext = context;
    }

    public void addText(String text) {
        mData.add(text);
    }

    public String getText(int position) {
        return mData.get(position);
    }

    public void setTextColor(int color) {
        mTextColor = color;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_spinner, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(getText(position));

        return convertView;
    }

    private class ViewHolder {
        private TextView text;

        private ViewHolder(View view) {
            text = (TextView) view.findViewById(android.R.id.text1);
            if (mTextColor != null) {
                text.setTextColor(mTextColor);
            }
        }
    }
}
