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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import Decoder.BASE64Encoder;

public class SubmitQueryActivity extends AppCompatActivity {

    public EditText textTag;
    public Button btnQuery;
    public int start_time, end_time;
    public ListView listViewContent;
    String username, SessionId, tag, TAG="SubmitQueryActivity", data;
    public String destinationAddress;
    boolean keyReady = false;
    GetServiceKeyThread getServiceKeyThread;
    SecretKey AESKey;
    CipherPack cipherPack;
    PrivateKey RAPrivateKey;
    // PublicKey RAPublicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_query);
        final MyApp myApp = (MyApp)getApplication();
        this.destinationAddress = myApp.destServerAddress;
        this.SessionId = myApp.SessionId;
        this.username = myApp.username;

        Intent intent = this.getIntent();
        start_time = intent.getIntExtra("start_time", -1);
        end_time = intent.getIntExtra("end_time", -1);
        tag = intent.getStringExtra("tag");
        keyReady = false;
        init();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 3: // key ready
                    keyReady = true;
                    Toast.makeText(getApplicationContext(), "key loaded", Toast.LENGTH_SHORT).show();
                    query();
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
        textTag = (EditText)findViewById(R.id.text_tag);
        btnQuery = (Button)findViewById(R.id.button_query);
        listViewContent = (ListView)findViewById(R.id.listView_content);
        listViewContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ServiceContentItem item = (ServiceContentItem)listViewContent.getItemAtPosition(position);
                Intent intent = new Intent(SubmitQueryActivity.this, ServiceContentDetailActivity.class);
                intent.putExtra("start_time", item.start_time);
                intent.putExtra("end_time", item.end_time);
                intent.putExtra("tag",item.tag);
                intent.putExtra("content",item.content);
                intent.putExtra("create_time", item.create_time);
                startActivity(intent);
            }
        });
        textTag.setText(tag); //自动填充已经选择的tag
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query();
            }
        });

        final MyApp myApp = (MyApp)getApplication();
        String RAAdress = myApp.destRAAddress;
        // getting service keys
        getServiceKeyThread = new GetServiceKeyThread(tag, start_time,end_time,
                "querier", SessionId ,RAAdress, handler);
        getServiceKeyThread.start();
    }

    private void query() {
        if (keyReady == false) {
            Toast.makeText(getApplicationContext(), "cannot load cipher keys", Toast.LENGTH_SHORT).show();
            return;
        }
        String time = Long.toString(System.currentTimeMillis());
        JSONObject tmpJson = getServiceKeyThread.json;
        String privateKey="";
        try {
            // publicKey = tmpJson.getString("RSAPublicKey");
            privateKey = tmpJson.getString("RSAPrivateKey");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        cipherPack = new CipherPack();
        // RAPublicKey = cipherPack.StringToPublicKey2(publicKey);
        RAPrivateKey = cipherPack.StringToPrivateKey(privateKey);

        String tmpAesKey = cipherPack.getMD5(privateKey);
        tag = String.format("%d %d %s", start_time, end_time, tag);
        AESKey = cipherPack.OriginalStringToSecretKey(tmpAesKey);
        String tmpTag = cipherPack.AESEncryption(tag, AESKey);

        final MyApp myApp = (MyApp)getApplication();
        String tmpTag1 = cipherPack.RSAEncrypt(tmpTag,myApp.serverPublicKey);
        tmpTag1 = Base64.encodeToString(tmpTag1.getBytes(), Base64.DEFAULT);
        tmpTag1 = new BASE64Encoder().encodeBuffer(tmpTag1.getBytes());
        // 生成json数据
        JSONObject json = new JSONObject();
        tag = textTag.getText().toString();
        try {
            json.put("tag", tmpTag);
            //json.put("username", username);
        } catch (Exception e) {
            Log.e(TAG, "exception when generate json data.");
        }
        String time1 = Long.toString(System.currentTimeMillis());
        Log.d(TAG, "处理查询者的数据花费"+time1+"-"+time+"/ms时间.");
        // 发送json数据，接收服务内容
        new SubmitQueryAsyncTask().execute("data=" + json.toString());
        // Log.e(TAG + "private key", privateKey);
    }

    /**
     * send query tag, and retrieve JSONArray string
     * @param content
     */
    public void queryAndObtain(String content) {
        try {
            URL url = new URL(destinationAddress+"/ServerEntry?operation=query");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Cookie", SessionId);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(10000);
            OutputStream os = conn.getOutputStream();
            os.write(content.getBytes());

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str;
            StringBuffer sb = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
            data = sb.toString();
        }  catch (Exception e) {
            Log.e(TAG, "exception in getJsonData.");
        }
    }

    /**
     * parse JSONArray to get List<ServiceContentItem>
     * @return
     */
    public List<ServiceContentItem> getJsonData() {
        List<ServiceContentItem> list = new ArrayList<>();
        try {
            JSONArray json = new JSONArray(data);
            if (json.length() == 0)
                return list;
            String CONTENT, Tag, CREATE_TIME;
            int START_TIME, END_TIME;
            for (int i = 0; i < json.length(); i++) {
                JSONObject jb = json.getJSONObject(i);
                CONTENT = new String(Base64.decode(jb.getString("data"), Base64.DEFAULT));
                // if (CONTENT.equals(cipherPack.RSAEncrypt("playing.", RAPublicKey)))
                    // textTag.setText("asshole detected.");
                Tag = jb.getString("tag");
                CREATE_TIME = jb.getString("time");
                String[] tag_tmp = cipherPack.AESDecryption(Tag, AESKey).split(" ");
                START_TIME = Integer.valueOf(tag_tmp[0]);
                END_TIME = Integer.valueOf(tag_tmp[1]);
                Tag = tag_tmp[2];
                // Log.e(TAG+" content", Integer.toString(CONTENT.length()));
                CONTENT = cipherPack.RSADecrypt(CONTENT, RAPrivateKey);
                ServiceContentItem item = new ServiceContentItem(CONTENT, Tag, START_TIME, END_TIME, CREATE_TIME);
                list.add(item);
            }
        } catch (Exception e) {
            Log.e(TAG, "exception when get json data."+e.getMessage());
        }
        return list;
    }

    /**
     * use AsyncTask to get service content from server.
     */
    class SubmitQueryAsyncTask extends AsyncTask<String,Void,List<ServiceContentItem>> {

        @Override
        protected List<ServiceContentItem> doInBackground(String... params) {
            queryAndObtain(params[0]);
            return getJsonData();
        }

        @Override
        protected void onPostExecute(List<ServiceContentItem> serviceContentItems) {
            super.onPostExecute(serviceContentItems);
            ServiceContentItemAdapter scia = new ServiceContentItemAdapter(getBaseContext(), serviceContentItems, listViewContent);
            listViewContent.setAdapter(scia);
        }
    }
}
