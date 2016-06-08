package com.example.atlantic8.projectspartan;

/**
 * Created by Atlantic8 on 2016/2/27 0027.
 */

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;


public class ImageLoader {
    private ImageView mimageView;
    private String murl;
    private ListView mlistView;
    private Set<SpartanAsyncTack> mtask;
    //创建lruCache
    private LruCache<String, Bitmap> lruCache;

    public ImageLoader(ListView listView) {
        mlistView = listView;
        mtask = new HashSet<SpartanAsyncTack>();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 4;
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            //内部方法sizeOf设置每一张图片的缓存大小
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存时调用，告诉系统这张缓存图片有多大
                return value.getByteCount();
            }
        };
    }

    public void addBitmapToCache(String url, Bitmap bitmap) {
        if (getBitmapfromCache(url) == null) {//判断当前缓存是否存在
            lruCache.put(url, bitmap);
        }
    }

    public Bitmap getBitmapfromCache(String url) {
        return lruCache.get(url);//可将lruCache看成map
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mimageView.getTag().equals(murl)) {
                mimageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    public void showimageThread(ImageView imageView, final String url) {
        mimageView = imageView;
        murl = url;
        new Thread() {
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromUrl(url);
                //此时的bitmap不能直接赋给ImageView，要创建Message将bitmap用handler发送出去给Handler，使之在主线程ui中更新
                Message message = Message.obtain();
                message.obj = bitmap;
                handler.sendMessage(message);
            }
        }.start();
    }

    //从url中获取一个Bitmap
    public Bitmap getBitmapFromUrl(String urlString) {
        Bitmap bitmap;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            inputStream = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(inputStream);
            connection.disconnect();
            return bitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;   //为什么要返回null
    }

    public void showImageByAsyncTask(ImageView imageView, String url) {  //将默认图片imageView换成要加载的图片的url
        //从缓存中取出对应的图片
        Bitmap bitmap = getBitmapfromCache(url);
        //如果没有就从网上下载
        if (bitmap == null) {
//			new SpartanAsyncTack(url).execute(url);
            imageView.setImageResource(R.drawable.cover);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /*
     * cancelAllTask()和loadImages方法是用来处理滚动时的优化
     */
    public void cancelAllTasks() {
        if (mtask != null) {
            for (SpartanAsyncTack task : mtask) {
                task.cancel(false);
            }
        }
    }

    public void loadImages(int start, int end) {
        for (int i = start; i < end; i++) {
            String url = SpartanAdapter.URLS[i];
            //从缓存中取出对应的图片
            Bitmap bitmap = getBitmapfromCache(url);
            //如果没有就从网上下载
            if (bitmap == null) {
                SpartanAsyncTack task = new SpartanAsyncTack(url);
                task.execute(url);
                mtask.add(task);
//				new SpartanAsyncTack(imageView, url).execute(url);
            } else {
//				imageView.setImageBitmap(bitmap);
                ImageView imageView = (ImageView) mlistView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private class SpartanAsyncTack extends AsyncTask<String, Void, Bitmap> {
        //写一个构造方法传递imageview
        //private ImageView mimageView;
        private String murl;

        public SpartanAsyncTack(String url) {
            //mimageView=imageView;
            murl = url;
        }

        protected Bitmap doInBackground(String... arg0) {
            // return getBitmapFormUrl(arg0[0]);
            // 不需要listview滚动优化缓存时
            String url = arg0[0];
            //从网络上获取图片
            Bitmap bitmap = getBitmapFromUrl(url);
            if (bitmap != null) {
                //将不在缓存的图片加入缓存
                addBitmapToCache(url, bitmap);
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {//将bitmap设置给imageview
            super.onPostExecute(bitmap);
            // if (mimageView.getTag().equals(murl)) {
            //     mimageView.setImageBitmap(bitmap);
            // }
            // mimageView.setImageBitmap(bitmap);
            ImageView imageView = (ImageView) mlistView.findViewWithTag(murl);
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            mtask.remove(this);  //此时AsyncTack已经结束，调用mtask结束AsyncTack自身
        }
    }
}
