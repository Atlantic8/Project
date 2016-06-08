package com.example.atlantic8.projectspartan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    private Button btnRegister;
    private String username, password, SessionId;
    private EditText usernameEditText, passwordEditText;
    private RadioButton participant, querier;
    private String TAG="RegisterActivity", destinationAddress="http://10.108.15.98:8081/WebServer";
    boolean state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btnRegister = (Button)findViewById(R.id.confirmButtonRegister);
        usernameEditText = (EditText)findViewById(R.id.usernameTextRegister);
        passwordEditText = (EditText)findViewById(R.id.passwordTextRegister);
        participant = (RadioButton)findViewById(R.id.radioButtonParticipantRegister);
        querier = (RadioButton)findViewById(R.id.radioButtonQuerierRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                if (participant.isChecked() == true) {
                    RegisterThread registerThread = new RegisterThread(username,password,"participant");
                    registerThread.start();
                } else if (querier.isChecked() == true) {
                    RegisterThread registerThread = new RegisterThread(username,password,"querier");
                    registerThread.start();
                } else {
                    Toast.makeText(getApplicationContext(), "choose user type first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        final MyApp myApp = (MyApp)getApplication();
        this.destinationAddress = myApp.destServerAddress;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: // register success
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("state", state);
                    intent.putExtra("SessionId", SessionId);
                    startActivity(intent);
                    break;
                case 2: // register failed
                    Toast.makeText(getApplicationContext(), "register failed", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

        }
    };

    class RegisterThread extends Thread {
        String username, password, usertype;
        public RegisterThread(String u, String p, String userType) {
            this.username = u;
            this.password = p;
            this.usertype = userType;
        }

        @Override
        public void run() {
            super.run();
            String content = "";

            content += ("username=" + username);
            content += ("&password=" + password);
            content += ("&usertype=" + usertype);
            content += ("&operation=register");

            try {
                // send login information to server.
                URL urlt = new URL(destinationAddress+"/test");
                URL url = new URL(destinationAddress+"/ServerEntry");
                HttpURLConnection con = (HttpURLConnection) urlt.openConnection();

                CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
                // get cookie or set cookie
                if (SessionId!=null && SessionId.length()>0) {
                    con.setRequestProperty("Cookie", SessionId);
                } else {
                    String cookieValue = con.getHeaderField("Set-Cookie");
                    if (cookieValue != null) {
                        SessionId = cookieValue.substring(0, cookieValue.indexOf(";"));
                    }
                }
                con.disconnect();

                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setReadTimeout(10000);
                OutputStream os = con.getOutputStream();
                os.write(content.getBytes());
                // feedback from server.
                // if: ok, then login success, else wrong;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String str = "";
                StringBuffer sb = new StringBuffer();
                while ((str = bufferedReader.readLine()) != null) {
                    sb.append(str);
                }
                JSONObject json = new JSONObject(sb.toString());
                //System.out.println("Feedback from server: " + sb.toString());
                Message msg = new Message();
                if (json.get("state").equals("success") || json.get("state").equals("SUCCESS"))
                    msg.what = 1;
                else
                    msg.what = 2;
                handler.sendMessage(msg);
            } catch (Exception e) {
                Log.e("Login Result:", e.getMessage().toString());
                e.printStackTrace();
            }
        }
    }

}
