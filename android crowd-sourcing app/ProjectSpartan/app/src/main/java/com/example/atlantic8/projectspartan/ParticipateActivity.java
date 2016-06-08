package com.example.atlantic8.projectspartan;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ParticipateActivity extends AppCompatActivity {
    public ListView listView;
    List<ServiceItem> serviceList;
    ServiceItemAdapter serviceItemAdapter;
    private String username, SessionId, TAG="Participant Activity";
    public String destinationAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participate);
        this.setTitle("Service List");

        final MyApp myApp = (MyApp)getApplication();
        this.destinationAddress = myApp.destServerAddress;
        this.SessionId = myApp.SessionId;
        this.username = myApp.username;
        init();
    }

    private void init() {
        listView = (ListView)findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ServiceItem serviceItem = serviceList.get(position);
                Intent intent = new Intent(ParticipateActivity.this, SubmitParticipationActivity.class);
                intent.putExtra("start_time", serviceItem.startTime);
                intent.putExtra("end_time", serviceItem.endTime);
                intent.putExtra("tag",serviceItem.tag);
                startActivity(intent);
            }
        });
        new ServiceAsyncTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 资源文件添加菜单
        // new MenuInflater(this).inflate(R.menu.menu_main, menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ParticipateActivity.this);
                builder1.setTitle("about");
                builder1.setMessage("about item clicked");
                builder1.show();
                break;
            case R.id.action_settings:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(ParticipateActivity.this);
                builder2.setTitle("settings");
                builder2.setMessage("settings item clicked");
                builder2.show();
                break;
            case R.id.action_quit: //go back to the login activity
                Intent intent = new Intent(ParticipateActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.refresh_service_list_button:
                new ServiceAsyncTask().execute();
                break;
            default:
                break;
        }

        return true;
    }

    public List<ServiceItem> getJsonData() {
        StringBuffer sb = new StringBuffer();
        serviceList = new ArrayList<>();
        try {
            URL url = new URL(destinationAddress+"/GetService");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Cookie", SessionId);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(10000);
            OutputStream os = conn.getOutputStream();
            os.write("operation=get_service".getBytes());

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
            JSONArray json = new JSONArray(sb.toString());
            for (int i=0; i<json.length(); i++) {
                JSONObject job = json.getJSONObject(i);
                // Log.d(TAG, job.toString());
                ServiceItem serviceItem = new ServiceItem(job.getInt("start_time"),job.getInt("end_time"),job.getString("tag"));
                serviceList.add(serviceItem);
            }
            return serviceList;
        }  catch (Exception e) {
            Log.e(TAG, "exception in getJsonData.");
        }
        return null;
    }

    // 异步对网络的访问
    class ServiceAsyncTask extends AsyncTask<Void, Void, List<ServiceItem>> {

        @Override
        protected List<ServiceItem> doInBackground(Void... params) {
             return getJsonData();
        }

        @Override
        protected void onPostExecute(List<ServiceItem> serviceItems) {
            if (serviceItems == null)
                Log.e(TAG, "serviceItems is null");
            super.onPostExecute(serviceItems);
            serviceItemAdapter = new ServiceItemAdapter(getBaseContext(), serviceItems, listView);
            listView.setAdapter(serviceItemAdapter);
        }
    }

}
