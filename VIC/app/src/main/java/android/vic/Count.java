package com.example.administrator.carmonitor;

import android.app.Application;

/**
 * Created by Administrator on 2017/7/8 0008.
 */

public class Count extends Application {
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int _count) {
        count = _count;
    }

    @Override
    public void onCreate() {
        count = 0;
        super.onCreate();
    }
}
