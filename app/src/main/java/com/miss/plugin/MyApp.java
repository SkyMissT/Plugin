package com.miss.plugin;

import android.app.Application;

import com.miss.plugin.util.HookUtil;
import com.miss.plugin.util.LoadUtil;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        LoadUtil.loadUtil(this);

        HookUtil.hookAMS();

    }
}
