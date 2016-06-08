package com.example.atlantic8.projectspartan;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PoseQuestion extends AppCompatActivity {
    Button btnPoseQuestionSubmit;
    EditText editTextQuestion, editTextStartTime, editTextEndTime;
    String TAG="PoseQuestion", destinationAddress="http://10.108.15.98:8081/RAuthority";
    String SessionId, username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pose_question);
        btnPoseQuestionSubmit = (Button)findViewById(R.id.btn_pose_question_submit);
        editTextQuestion = (EditText)findViewById(R.id.edit_text_question);
        editTextStartTime = (EditText)findViewById(R.id.edit_text_start_time);
        editTextEndTime = (EditText)findViewById(R.id.edit_text_end_time);
        btnPoseQuestionSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startTime, endTime, question;
                startTime = editTextStartTime.getText().toString();
                endTime = editTextEndTime.getText().toString();
                question = editTextQuestion.getText().toString();
                JSONObject json = new JSONObject();
                try {
                    json.put("start_time",startTime);
                    json.put("end_time",endTime);
                    json.put("question",question);
                } catch (JSONException e) {
                    Log.e(TAG,e.getMessage());
                }
                // execute
                new PoseQuestionAsyncTask().execute("data="+json.toString());
            }
        });
        Intent intent = this.getIntent();
        SessionId = intent.getStringExtra("SessionId");
        username = intent.getStringExtra("username");
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: // success
                    Toast.makeText(getApplicationContext(), "upload succeed", Toast.LENGTH_SHORT).show();
                    break;
                case 2: // failed
                    Toast.makeText(getApplicationContext(), "upload failed", Toast.LENGTH_SHORT).show();
                    break;
                case 3: // 问题已经存在
                    Toast.makeText(getApplicationContext(), "content already exists", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            Intent intent = new Intent(PoseQuestion.this, QueryActivity.class);
            startActivity(intent);
        }
    };

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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(PoseQuestion.this);
                builder1.setTitle("about");
                builder1.setMessage("about item clicked");
                builder1.show();
                break;
            case R.id.action_settings:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(PoseQuestion.this);
                builder2.setTitle("settings");
                builder2.setMessage("settings item clicked");
                builder2.show();
                break;
            case R.id.action_quit: //go back to the login activity
                Intent intent = new Intent(PoseQuestion.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.refresh_service_list_button:
                //new ServiceAsyncTask().execute();
                break;
            default:
                break;
        }
        return true;
    }

    public String uploadQuestion(String content) {
        try {
            URL url = new URL(destinationAddress+"/GetQuestion");
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
            return sb.toString();
        }  catch (Exception e) {
            Log.e(TAG, "exception in uploadQuestion.");
        }
        return null;
    }

    class PoseQuestionAsyncTask extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... params) {
            String feedback = uploadQuestion(params[0]);
            Message msg = new Message();
            if (feedback.equals("1")) {
                msg.what = 1;
                handler.sendMessage(msg);
            } else if (feedback.equals("2")) {
                msg.what = 2;
                handler.sendMessage(msg);
            } else if (feedback.equals("3")) {
                msg.what = 3;
                handler.sendMessage(msg);
            } else {
                msg.what = 0;
                handler.sendMessage(msg);
            }
            return null;
        }
    }
}
