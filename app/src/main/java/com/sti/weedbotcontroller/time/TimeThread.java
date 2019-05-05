package com.sti.weedbotcontroller.time;

import android.os.Message;
import android.util.Log;

import com.sti.weedbotcontroller.MainActivity;

public class TimeThread extends Thread{

    public boolean stop=false;

    public void run() {
        while (!stop) {
            android.os.SystemClock.sleep(1000);
            Message msgt = new Message();
            msgt.setTarget(MainActivity.handler);
            msgt.obj = "updateTime";
            msgt.sendToTarget();
        }
    }
}
