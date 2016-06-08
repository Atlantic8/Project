package com.example.atlantic8.projectspartan;

/**
 * Created by Atlantic8 on 2016/2/20 0020.
 */

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SpartanAdapter extends BaseAdapter implements AbsListView.OnScrollListener{
    private List<PictureInfo> mList;
    private LayoutInflater mInflater;
    private ImageLoader mimageLoader;
    private int mStart,mEnd;
    public static String[] URLS;
    private boolean mFirstIn;
    public SpartanAdapter(Context context,List<PictureInfo> data,ListView listView){
        mList=data;
        mInflater=LayoutInflater.from(context);
        mimageLoader=new ImageLoader(listView);
        URLS=new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            URLS[i]=data.get(i).picture_path;
        }
        //注册OnScrollListener
        listView.setOnScrollListener(this);
    }
    public int getCount() {
        return mList.size();
    }
    public Object getItem(int position) {
        return mList.get(position);
    }
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View v, ViewGroup arg2) {
        //用ViewHolder,主要是进行一些性能优化,减少一些不必要的重复操作,将v的tag设置为ViewHolder,不为空时即可重复使用
        viewHolder vHolder=null;
        if (v==null) {
            vHolder=new viewHolder();
            v=mInflater.inflate(R.layout.listview_layout, null);//laout文件转化成convertview
            vHolder.Icon=(ImageView) v.findViewById(R.id.image_compressed);
            vHolder.title=(TextView) v.findViewById(R.id.image_title);
            vHolder.time=(TextView) v.findViewById(R.id.image_create_time);
            vHolder.username=(TextView) v.findViewById(R.id.username);
            vHolder.value=(TextView) v.findViewById(R.id.image_value);
            v.setTag(vHolder);
        }else {
            vHolder=(viewHolder) v.getTag();
        }
        vHolder.Icon.setImageResource(R.drawable.cover);
        String url=mList.get(position).picture_path;
        vHolder.Icon.setTag(url);
        //通过showimageThread得到newsIconURl加载新图片
        //new imageLoader().showimageThread(vHolder.Icon, url);
        //不做图片缓存时
        //new imageLoader().showImageByAsyncTask(vHolder.Icon, url);
        mimageLoader.showImageByAsyncTask(vHolder.Icon, url);//做图片缓存时
        vHolder.title.setText(mList.get(position).title);
        vHolder.time.setText(mList.get(position).time);
        vHolder.username.setText(mList.get(position).user_name);
        vHolder.value.setText(mList.get(position).value);
        return v;
    }
    class viewHolder{
        public TextView title,time,username,value;
        public ImageView Icon;
    }
    @Override
    public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int endVisibleItem) {
        mStart=firstVisibleItem;
        mEnd=firstVisibleItem+visibleItemCount;
        //第一次显示的时候调用
        if (mFirstIn && visibleItemCount>0) {
            mimageLoader.loadImages(mStart,mEnd);
            mFirstIn=false;
        }
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState==SCROLL_STATE_IDLE) {//闲置时
            //加载可见项
            mimageLoader.loadImages(mStart,mEnd);
        }else {
            mimageLoader.cancelAllTasks();
        }
    }
}
