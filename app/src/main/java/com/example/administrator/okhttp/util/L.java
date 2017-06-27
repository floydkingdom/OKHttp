package com.example.administrator.okhttp.util;

/**
 * Created by Administrator on 2017/6/22.
 */

public class L {

    private static final String TAG = "okHttp";
    private static boolean debug = true;

    public static void e(String s){
        if (debug)
        android.util.Log.e(TAG, s);
    }
}
