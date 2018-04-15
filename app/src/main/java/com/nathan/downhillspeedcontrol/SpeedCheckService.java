package com.nathan.downhillspeedcontrol;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SpeedCheckService extends IntentService {

    private ScheduledExecutorService scheduleTaskExecutor;
    private int counter;

    public SpeedCheckService() {
        super("SpeedCheckService");
//        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
//
//        // This schedule a runnable task every 2 minutes
//        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
//            public void run() {
//                System.out.println("Schedules task | " + Thread.currentThread().getId());
//            }
//        }, 0, 2, TimeUnit.SECONDS);
        counter = 0;
        System.out.println("new service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            System.out.println("Doing intent  stuff | " + Thread.currentThread().getId());
            Intent mServiceIntent = new Intent(this, SpeedCheckService.class);
            //startService(mServiceIntent);
        }
    }
}
