package com.example.atlantic8.projectspartan;

import android.app.Application;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

/**
 * Created by Atlantic8 on 2016/3/24 0024.
 */
public class MyApp extends Application {
    public String destServerAddress="http://10.108.15.98:8081/WebServer";
    public String destRAAddress="http://10.108.15.98:8081/RAuthority";
    public String SessionId,username;
    public PublicKey serverPublicKey;
}
