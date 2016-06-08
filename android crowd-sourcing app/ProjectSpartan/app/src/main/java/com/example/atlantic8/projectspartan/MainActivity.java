package com.example.atlantic8.projectspartan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class MainActivity extends AppCompatActivity{
    private EditText username, password;
    private RadioGroup rgroup;
    private RadioButton rquerier, rparticipant;
    private Button btnLogin, btnReg;
    private String SessionId="";
    String userName, passWord, destinationAddress;
    String serverPuk = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3Zqi5RdC2Qon7vvTILqtno/0+4OLD1gF/FYyStkvVNBqSMbB6dq6oUgAvUdeQp5OBuRo83H5S5JpV5IqrrPUr4+8QoLJ/n90YTqRZlYmsixGypn0liyQyrsgOYascMcYpBzqBNOcbDEuhSBdqqf3idLo/fi3sOrp1hJskexcCOQIDAQAB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        username = (EditText)findViewById(R.id.usernameText);
        password = (EditText)findViewById(R.id.passwordText);
        rgroup = (RadioGroup)findViewById(R.id.radioGroup);
        rquerier = (RadioButton)findViewById(R.id.radioButtonQuerier);
        rparticipant = (RadioButton)findViewById(R.id.radioButtonParticipant);
        btnLogin = (Button)findViewById(R.id.loginButton);
        btnReg = (Button)findViewById(R.id.registerButton);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = username.getText().toString();
                passWord = password.getText().toString();
                final MyApp myApp = (MyApp)getApplication();
                destinationAddress = myApp.destServerAddress;
                myApp.username = userName;
                if (rparticipant.isChecked() == true) {
                    LoginThread loginThread = new LoginThread(userName, passWord, "participant");
                    loginThread.start();
                } else if (rquerier.isChecked() == true) {
                    LoginThread loginThread = new LoginThread(userName, passWord, "querier");
                    loginThread.start();
                } else {
                    Toast.makeText(getApplicationContext(), "choose user type before.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Toast.makeText(getApplicationContext(), "login successfully", Toast.LENGTH_SHORT).show();
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        final MyApp myApp = (MyApp)getApplication();
        myApp.serverPublicKey = new CipherPack().StringToPublicKey2(serverPuk);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1: // participation login success
                    Intent intent = new Intent(MainActivity.this, ParticipateActivity.class);
                    startActivity(intent);
                    break;
                case 2: // participation login failed
                    Toast.makeText(getApplicationContext(), "login failed", Toast.LENGTH_SHORT).show();
                    break;
                case 3: // query login success
                    Intent intent2 = new Intent(MainActivity.this, QueryActivity.class);
                    startActivity(intent2);
                    break;
                case 4: // query login failed
                    Toast.makeText(getApplicationContext(), "login failed", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    class LoginThread extends Thread {
        String username, password, usertype;
        public LoginThread(String u, String p, String userType) {
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
            content += ("&operation=login");

            try {
                // send login information to server.
                URL url = new URL(destinationAddress+"/ServerEntry");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
                final MyApp myApp = (MyApp)getApplication();
                myApp.SessionId = SessionId;
                con.disconnect();

                // get connection
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
                // System.out.println("Feedback from server: " + sb.toString());
                Message msg = new Message();
                if (json.get("state").equals("success") || json.get("state").equals("SUCCESS"))
                    if (usertype.equals("participant"))
                        msg.what = 1;
                    else if (usertype.equals("querier"))
                        msg.what = 3;

                else
                    if (usertype.equals("participant"))
                        msg.what = 2;
                    else if (usertype.equals("querier"))
                        msg.what = 4;
                handler.sendMessage(msg);
            } catch (Exception e) {
                // Log.e("Login Result:", e.getMessage().toString());
                e.printStackTrace();
            }
        }
    }

}
