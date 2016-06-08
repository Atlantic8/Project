package com.example.atlantic8.projectspartan;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Atlantic8 on 2016/3/3 0003.
 */
public class ServiceItemAdapter extends BaseAdapter implements AbsListView.OnScrollListener {
    private List<ServiceItem> mList;
    private LayoutInflater mInflater;
    private int mStart,mEnd;
    private String TAG="ServiceItemAdapter";

    public ServiceItemAdapter(Context context,List<ServiceItem> data,ListView listView) {
        mList=data;
        mInflater=LayoutInflater.from(context);
        //注册OnScrollListener
        listView.setOnScrollListener(this);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        viewHolder vHolder;
        if (v==null) {
            vHolder=new viewHolder();
            v=mInflater.inflate(R.layout.service_item, null);//layout文件转化成convertView
            vHolder.start_time=(TextView) v.findViewById(R.id.start_time);
            vHolder.end_time=(TextView) v.findViewById(R.id.end_time);
            vHolder.service_tag=(TextView) v.findViewById(R.id.service_tag);
            v.setTag(vHolder);
            // Log.d(TAG,"this is a debug point");
        } else {
            vHolder=(viewHolder) v.getTag();
        }
        vHolder.start_time.setText(String.valueOf(mList.get(position).startTime));
        vHolder.end_time.setText(String.valueOf(mList.get(position).endTime+""));
        vHolder.service_tag.setText(mList.get(position).tag);
        return v;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart=firstVisibleItem;
        mEnd=firstVisibleItem+visibleItemCount;
    }

    class viewHolder{
        public TextView start_time,end_time,service_tag;
        public viewHolder(){}
    }
}
