package com.example.atlantic8.projectspartan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Atlantic8 on 2016/3/8 0008.
 */
public class ServiceContentItemAdapter extends BaseAdapter implements AbsListView.OnScrollListener {
    private List<ServiceContentItem> mList;
    private LayoutInflater mInflater;
    private int mStart,mEnd;
    public ServiceContentItemAdapter(Context context,List<ServiceContentItem> data,ListView listView) {
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
            v=mInflater.inflate(R.layout.service_content_item, null);//laout文件转化成convertview
            vHolder.start_time=(TextView) v.findViewById(R.id.start_time2);
            vHolder.end_time=(TextView) v.findViewById(R.id.end_time2);
            vHolder.service_tag=(TextView) v.findViewById(R.id.service_tag2);
            vHolder.contentView=(TextView) v.findViewById(R.id.service_item_content);
            vHolder.createTime=(TextView) v.findViewById(R.id.content_create_time);
            v.setTag(vHolder);
        }else {
            vHolder=(viewHolder) v.getTag();
        }
        vHolder.start_time.setText(String.valueOf(mList.get(position).start_time));
        vHolder.end_time.setText(String.valueOf(mList.get(position).end_time));
        vHolder.service_tag.setText(mList.get(position).tag);
        vHolder.contentView.setText(mList.get(position).content);
        vHolder.createTime.setText(timeStamp2Date(mList.get(position).create_time));
        return v; // this is important!!!!!!!!!
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart=firstVisibleItem;
        mEnd=firstVisibleItem+visibleItemCount;
    }

    /**
     * transfer unix timeStamp to common date
     * @param timeStamp
     * @return
     */
    private String timeStamp2Date(String timeStamp) {
        Long timestamp = Long.parseLong(timeStamp)*1000;
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
        return date;
    }

    class viewHolder{
        public TextView start_time,end_time,service_tag;
        public TextView contentView, createTime;
    }
}
