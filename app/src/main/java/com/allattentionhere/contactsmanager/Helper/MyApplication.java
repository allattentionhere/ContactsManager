package com.allattentionhere.contactsmanager.Helper;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.allattentionhere.contactsmanager.Model.DBHandler;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.File;

public class MyApplication extends Application {
    public static RequestQueue Remotecalls;
    public static DisplayMetrics metrics;
    public static Context context;
    public static DBHandler dbHandler;


    @Override
    public void onCreate() {
        super.onCreate();
        MakeRequestQueue();
        metrics = this.getResources().getDisplayMetrics();
        context = getApplicationContext();
        dbHandler = new DBHandler(getApplicationContext());

    }


    public void MakeRequestQueue() {
        Remotecalls = Volley.newRequestQueue(getApplicationContext());
    }





}