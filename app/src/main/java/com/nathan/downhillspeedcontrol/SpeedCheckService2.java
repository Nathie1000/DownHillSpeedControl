package com.nathan.downhillspeedcontrol;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.ActivityCompat;

public class SpeedCheckService2 extends Service implements LocationListener {

    private Handler handler;

    private LocationManager locationManager;

    @Override
    public void onCreate() {
        System.out.println("Ser Created");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            System.out.println("Presmission denided");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("Running on thread" + Thread.currentThread().getId());

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // Restore interrupt status.
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });

        //thread.start();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Ser started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Ser bind");
        return null;
    }

    @Override
    public void onDestroy() {
        System.out.println("Ser Destroyed");
    }


    @Override
    public void onLocationChanged(Location location) {
        System.out.println("loc: " + location.getLatitude() + " " + location.getLongitude());
        System.out.println("speed: " + location.getSpeed());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        System.out.println("no gps");
    }
}
