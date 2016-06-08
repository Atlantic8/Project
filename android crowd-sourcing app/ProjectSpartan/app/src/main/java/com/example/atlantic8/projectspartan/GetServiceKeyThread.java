package com.example.atlantic8.projectspartan;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Atlantic8 on 2016/3/12 0012.
 */
public class GetServiceKeyThread extends Thread{
    private String tag, userType, SessionId;
    private int start_time, end_time;
    public String destinationAddress;  // TA address
    String TAG = "Get Service Key Thread";
    Handler handler;
    // all the cipher keys are in this json object
    JSONObject json;
    public GetServiceKeyThread(String tag, int start_time, int end_time, String user_type, String SessionId, String addr, Handler h) {
        this.tag = tag;
        this.userType = user_type;
        this.start_time = start_time;
        this.end_time = end_time;
        this.SessionId = SessionId;
        this.destinationAddress = addr;
        this.handler = h;
    }

    // get cipher keys from RA.
    @Override
    public void run() {
        Message msg = new Message();
        try {
            URL url = new URL(destinationAddress+"/ServerEntry");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Cookie", SessionId);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setReadTimeout(10000);
            OutputStream os = conn.getOutputStream();
            JSONObject json1 = new JSONObject();
            json1.put("start_time", start_time);
            json1.put("end_time", end_time);
            json1.put("tag", tag);
            json1.put("user_type", userType);
            os.write(("data="+json1.toString()).getBytes());
            // get feedback from RA
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str;
            StringBuffer sb = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
            json = new JSONObject(sb.toString());
            // Log.e(TAG, sb.toString());
            msg.what = 3; // success
        } catch (Exception e) {
            // Log.e(TAG, e.getMessage());
            msg.what = 4;
        }
        handler.sendMessage(msg);
    }

}
