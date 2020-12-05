package com.miss.myplugin;

import android.util.Log;

public class MyPrint {

    public static final void printString() {

        Log.e("tpc", "插件  普通类 方法调用~");
        Log.e("tpc", Util.getTime());
    }

}
