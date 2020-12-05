package com.miss.plugin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jump(View view) {

        Log.e("tpc", "宿主 点击 jump ");

        try {
            Class<?> aClass = Class.forName("com.miss.myplugin.MyPrint");
            Method method = aClass.getMethod("printString");
            method.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.miss.myplugin", "com.miss.myplugin.MainActivity"));
        startActivity(intent);
    }
}