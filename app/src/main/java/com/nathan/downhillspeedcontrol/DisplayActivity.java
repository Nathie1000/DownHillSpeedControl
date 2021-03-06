package com.nathan.downhillspeedcontrol;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import static android.os.SystemClock.uptimeMillis;

public class DisplayActivity extends AppCompatActivity implements LocationListener {
    private static final int animationTimeMs = 600;
    private static final int gpsUpdateTimeMs = 500;
    private static final int numberOfSpeedSamples = 10;
    private static final int countDownIntervalMs = 100;
    public static final int maxThresholdExceededCount = 3;

    private static final int defaultTimePreferenceS = 45;
    private static final int defaultSpeedPreferenceKmh = 80;
    private static final int defaultSoundVolume = 100;
    //private static final int defaultSoundDurationMs = 150;
    public static final boolean defaultNightMode = false;

    private TextView speedView;
    private TextView timeView;
    private ValueAnimator timeViewAnimator;

    private LocationManager locationManager;
    private InfiniteCountDownTimer countDownTimer;

    private Location lastKnowLocation;
    private long lastKnowTimeMs;
    private ArrayList<Float> speeds;
    private boolean isSpeedThresholdExceeded;
    private int thresholdExceededCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isNightMode = sharedPref.getBoolean(SettingsActivity.KEY_NIGHT_MODE, defaultNightMode);
        if (isNightMode) {
            setTheme(R.style.DarkTheme);
        }
        // Note: that setTheme() needs to be called before setContentView().
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        speedView = findViewById(R.id.speedView);
        timeView = findViewById(R.id.timeView);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        lastKnowLocation = null;
        lastKnowTimeMs = 0;
        speeds = new ArrayList<>();
        isSpeedThresholdExceeded = false;
        thresholdExceededCount = 0;

        float largeTextSize = getResources().getDimension(R.dimen.text_large);

        timeViewAnimator = ValueAnimator.ofFloat(0, largeTextSize);
        timeViewAnimator.setDuration(animationTimeMs);
        timeViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                timeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue);
            }
        });


        // The countdown time form settings and covert it to milliseconds.
        int countDownTimeMs = sharedPref.getInt(SettingsActivity.KEY_TIME, defaultTimePreferenceS) * 1000;
        countDownTimer = new InfiniteCountDownTimer(countDownTimeMs, countDownIntervalMs) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeView.setText(getResources().getString(R.string.time_unit, millisUntilFinished / 1000.0f));
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        // Night mode icon.
        menu.getItem(0).getIcon().mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        // Settings icon.
        menu.getItem(1).getIcon().mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.night_mode) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            // Get the current night mode and inverse it.
            boolean isNightMode = !sharedPref.getBoolean(SettingsActivity.KEY_NIGHT_MODE, defaultNightMode);
            // Rewrite the inverted mode back to settings.
            sharedPref.edit().putBoolean(SettingsActivity.KEY_NIGHT_MODE, isNightMode).apply();
            // Recreate windows in order to display new theme.
            recreate();

            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsUpdateTimeMs, 0, this);
            lastKnowTimeMs = uptimeMillis();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsUpdateTimeMs, 0, this);
            lastKnowTimeMs = uptimeMillis();
        }
        else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Don't update if location is not valid.
        if (location == null) return;

        // To avoid nullptr take current location as last known location if last known location is unknown.
        if (lastKnowLocation == null) {
            lastKnowLocation = location;
        }

        // Get the current distance and time for this update.
        float distanceM = location.distanceTo(lastKnowLocation);
        long nowMs = uptimeMillis();

        // Get the time between this updte and the last  update in seconds.
        float elapsedTimeSec = (nowMs - lastKnowTimeMs) / 1000.0f;

        // Use a moving average to stabilize the speed.
        if (speeds.size() >= numberOfSpeedSamples) {
            speeds.remove(0);
        }
        // Calc and add steed. Note: speed is in m/s.
        speeds.add(distanceM / elapsedTimeSec);

        // Get the average speed and convert is from ms/s to km/h
        int speedKmh = Math.round(calcAvgFromList(speeds) * 3.6f);

        speedView.setText(getResources().getString(R.string.speed_unit, speedKmh));
        speedView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_medium));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the speed threshold from settings. This should already be in km/h
        int speedThresholdKmh = sharedPref.getInt(SettingsActivity.KEY_SPEED, defaultSpeedPreferenceKmh);

        if (speedKmh > speedThresholdKmh) {
            thresholdExceededCount++;
            thresholdExceededCount = Math.min(thresholdExceededCount, maxThresholdExceededCount);
        }
        else {
            thresholdExceededCount--;
            thresholdExceededCount = Math.max(thresholdExceededCount, 0);
        }

        // Check if we need to start the countdown timer.
        if (speedKmh > speedThresholdKmh && !isSpeedThresholdExceeded && thresholdExceededCount >= maxThresholdExceededCount) {
            isSpeedThresholdExceeded = true;
            timeView.setVisibility(View.VISIBLE);
            timeViewAnimator.start();
            countDownTimer.start();
            // Get the volume of the notification sound.
            int soundVolume = sharedPref.getInt(SettingsActivity.KEY_SOUND_VOLUME, defaultSoundVolume);
            // Play notification sound.
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, soundVolume);
            toneGen.startTone(ToneGenerator.TONE_CDMA_PIP);
        }
        // Check if we need to stop the countdown timer.
        else if (speedKmh <= speedThresholdKmh && isSpeedThresholdExceeded && thresholdExceededCount == 0) {
            isSpeedThresholdExceeded = false;
            timeViewAnimator.reverse();
            timeViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    timeView.setVisibility(View.INVISIBLE);
                    timeViewAnimator.removeListener(this);
                }
            });
            countDownTimer.cancel();
        }

        // Update last know position and time.
        lastKnowLocation = location;
        lastKnowTimeMs = nowMs;
    }

    @Override
    public void onStatusChanged(String s, int status, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    private float calcAvgFromList(ArrayList<Float> list) {
        if (list.isEmpty()) return 0;
        float sum = 0;
        for (float listItem : list) {
            sum += listItem;
        }
        return sum / list.size();
    }
}
