package com.example.atlantic8.projectspartan;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

public class SubmitParticipationActivity extends AppCompatActivity {

    public int start_time, end_time;
    public String tag, username, SessionId, TAG="SubmitParticipationActivity";
    private String serverPublicKey, serverAESKey;
    private EditText tag_content;
    private TextView TextStart, TextEnd, TextTag;
    private Button btnQuery;
    public String destinationAddress;
    boolean keyReady = false;
    GetServiceKeyThread getServiceKeyThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        keyReady = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_participation);
        Intent intent = this.getIntent();
        start_time = intent.getIntExtra("start_time", -1);
        end_time   = intent.getIntExtra("end_time",   -1);
        tag = intent.getStringExtra("tag");

        final MyApp myApp = (MyApp)getApplication();
        this.destinationAddress = myApp.destServerAddress;
        this.SessionId = myApp.SessionId;
        this.username = myApp.username;
        init();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1: // success
                    Toast.makeText(getApplicationContext(), "upload success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SubmitParticipationActivity.this, ParticipateActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    break;
                case 2: // fail
                    Toast.makeText(getApplicationContext(), "upload failed", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(SubmitParticipationActivity.this, ParticipateActivity.class);
                    intent1.putExtra("username", username);
                    startActivity(intent1);
                    break;
                case 3: // key ready
                    keyReady = true;
                    Toast.makeText(getApplicationContext(), "key loaded", Toast.LENGTH_SHORT).show();
                    break;
                case 4: // key not ready
                    keyReady = false;
                    break;
                default:
                    break;
            }
        }
    };

    private void init() {
        TextStart = (TextView)findViewById(R.id.submit_start_time);
        TextEnd = (TextView)findViewById(R.id.submit_end_time);
        TextTag = (TextView)findViewById(R.id.submit_tag_text);
        TextStart.setText(String.valueOf(start_time));
        TextEnd.setText(String.valueOf(end_time));
        TextTag.setText(tag);
        tag_content = (EditText)findViewById(R.id.tag_content);
        btnQuery = (Button)findViewById(R.id.button_submit);

        final MyApp myApp = (MyApp)getApplication();
        String RAAdress = myApp.destRAAddress;
        // Log.e(TAG, RAAdress);
        // getting service keys
        getServiceKeyThread = new GetServiceKeyThread(tag, start_time,end_time,
                "participant", SessionId ,RAAdress, handler);
        getServiceKeyThread.start();

        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (keyReady == false) {
                    Toast.makeText(getApplicationContext(), "cannot load cipher keys", Toast.LENGTH_SHORT).show();
                    return;
                }
                String time = Long.toString(System.currentTimeMillis());
                JSONObject tmpJson = getServiceKeyThread.json;
                String publicKey="", secretKey="";
                try {
                    publicKey = tmpJson.getString("RSAPublicKey");
                    secretKey = tmpJson.getString("SecretKey");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final MyApp myApp1 = (MyApp)getApplication();
                CipherPack cipherPack = new CipherPack();
                // tagInput and contentInput remains to be encrypted.
                String tagInput = secretKey;
                // 二次加密 和 Base64编码
                String tagInput1 = cipherPack.RSAEncrypt(tagInput, myApp1.serverPublicKey);
                // tagInput = Base64.encodeToString(tagInput.getBytes(), Base64.DEFAULT);
                tagInput1 = new BASE64Encoder().encodeBuffer(tagInput1.getBytes());
                // Log.e(TAG, tagInput);
                // encrypt content
                String contentInput = cipherPack.RSAEncrypt(tag_content.getText().toString(),
                        cipherPack.StringToPublicKey2(publicKey));
                contentInput = Base64.encodeToString(contentInput.getBytes(), Base64.DEFAULT);
                //Log.e(TAG, contentInput+"  --over");
                // 二次加密 和 Base64编码
                String contentInput1 = cipherPack.RSAEncrypt(contentInput, myApp1.serverPublicKey);
                //Log.e(TAG, contentInput+"  --over");
                // contentInput = Base64.encodeToString(contentInput.getBytes(), Base64.DEFAULT);
                contentInput1 = new BASE64Encoder().encodeBuffer(contentInput1.getBytes());
                //Log.e(TAG, contentInput+"  --over");
                // Log.e(TAG+" content input", Integer.toString(contentInput.length())+tag_content.getText().toString());
                JSONObject json = new JSONObject();
                String time1 = Long.toString(System.currentTimeMillis());
                Log.d(TAG, "处理参与者的数据花费"+time1+"-"+time+"/ms时间.");
                try {
                    //json.put("start_time", start_time);
                    //json.put("end_time", end_time);
                    json.put("tag", tagInput);
                    // Log.e(TAG, tagInput);
                    json.put("content", contentInput);
                    // json.put("username", username);
                    // json.put("type", "1"); // data type
                    new SubmitParticipationAsyncTask().execute(json.toString());
                } catch (JSONException e) {
                    Log.e(TAG, "exception during generating json data. "+e.getMessage());
                    // e.printStackTrace();
                }
            }
        });
    }

    public void upload (String str) {
        try {
            //Log.d(TAG, str);
            URL url = new URL(destinationAddress+"/ServerEntry?operation=participate");
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestProperty("Cookie", SessionId);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setReadTimeout(10000);
            OutputStream os = httpURLConnection.getOutputStream();
            os.write(("data=" + str).getBytes());
            // get feedback from server.
            BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String Str;
            while ((Str = br.readLine()) != null) {
                sb.append(Str);
            }
            //Log.d(TAG, "feedback from server after upload :"+sb.toString());
            JSONObject jsonObject = new JSONObject(sb.toString());
            String state = jsonObject.getString("state");
            // send message to update UI
            Message msg = new Message();
            if (state.equals("SUCCESS") || state.equals("success"))
                msg.what = 1;
            else
                msg.what = 2;
            handler.sendMessage(msg);
            return;
        } catch (Exception e) {
            Log.e(TAG, "exception in upload. "+e.getMessage());
            // e.printStackTrace();
        }
        return;
    }

    class SubmitParticipationAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            upload(params[0]);
            return null;
        }
    }
}
