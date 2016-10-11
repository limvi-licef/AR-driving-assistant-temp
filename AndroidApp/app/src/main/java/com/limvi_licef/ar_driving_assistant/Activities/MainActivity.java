package com.limvi_licef.ar_driving_assistant.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aware.Aware;
import com.aware.Aware_Preferences;

import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.database.*;
import com.limvi_licef.ar_driving_assistant.R;

import com.limvi_licef.ar_driving_assistant.fragments.UserDialogFragment;
import com.limvi_licef.ar_driving_assistant.receivers.AccelerometerReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.GyroscopeReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.LinearAccelerometerReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.LocationReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.TemperatureReceiver;
import com.limvi_licef.ar_driving_assistant.services.ExportTask;

public class MainActivity extends Activity {

    private DatabaseHelper dbHelper;
    private Intent aware;
    private HandlerThread sensorThread;
    private Handler sensorHandler;

    private AccelerometerReceiver accelerometerReceiver;
    private TemperatureReceiver temperatureReceiver;
    private LocationReceiver locationReceiver;
    private LinearAccelerometerReceiver linearAccelerometerReceiver;
    private GyroscopeReceiver gyroscopeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUIElements();
        dbHelper = DatabaseHelper.getHelper(this);
        aware = new Intent(this, Aware.class);
        setupSensors();
    }

    @Override
    protected void onDestroy() {
        unregisterListeners();
        super.onDestroy();
    }

    private void setupUIElements() {

        ToggleButton monitoringToggle = (ToggleButton) findViewById(R.id.monitoring_button);
        monitoringToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean toggled) {
                if (toggled) {
                    startMonitoring();

                } else {
                    stopMonitoring();
                }
            }
        });
        Button setupUser = (Button) findViewById(R.id.setup_user_button);
        setupUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDialogFragment userFragment = UserDialogFragment.newInstance();
                userFragment.show(getFragmentManager(), "usersetupdialog");
            }
        });
        Button exportDatabase = (Button) findViewById(R.id.check_database_button);
        exportDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ExportTask(MainActivity.this).execute();
            }
        });
    }

    private void setupSensors(){
        startService(aware);

        Aware.setSetting(this, Aware_Preferences.STATUS_GYROSCOPE, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_GYROSCOPE, 200000);

        Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_ACCELEROMETER, 200000);

        Aware.setSetting(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 200000);

        //TODO BUG
//        Aware.setSetting(this, Settings.STATUS_FUSED_LOCATION, true, Settings.FUSED_LOCATION_PACKAGE);
//        Aware.setSetting(this, "fallback_location_timeout", 0, Settings.FUSED_LOCATION_PACKAGE);
//        Aware.setSetting(this, Settings.FREQUENCY_FUSED_LOCATION, 0.5, Settings.FUSED_LOCATION_PACKAGE);
//        Aware.setSetting(this, Settings.MAX_FREQUENCY_FUSED_LOCATION, 0, Settings.FUSED_LOCATION_PACKAGE);
//        Aware.setSetting(this, Settings.ACCURACY_FUSED_LOCATION, 100, Settings.FUSED_LOCATION_PACKAGE);
//        Aware.setSetting(this, "location_sensitivity", 0, Settings.FUSED_LOCATION_PACKAGE);

        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_GPS, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_NETWORK, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LOCATION_GPS, 0.25);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LOCATION_NETWORK, 0.25);
        Aware.setSetting(this, Aware_Preferences.MIN_LOCATION_GPS_ACCURACY, 0);
        Aware.setSetting(this, Aware_Preferences.MIN_LOCATION_NETWORK_ACCURACY, 0);
        Aware.setSetting(this, Aware_Preferences.LOCATION_EXPIRATION_TIME, 4);

        Aware.setSetting(this, Settings.STATUS_OPEN_WEATHER, true, Settings.OPEN_WEATHER_PACKAGE);
        Aware.setSetting(this, Settings.FREQUENCY_OPEN_WEATHER, 30, Settings.OPEN_WEATHER_PACKAGE);
        Aware.setSetting(this, Settings.API_KEY_OPEN_WEATHER, R.string.openweather, Settings.OPEN_WEATHER_PACKAGE);
    }

    private void registerListeners(){
        sensorThread = new HandlerThread("SensorDataHandlerThread");
        sensorThread.start();
        Looper looper = sensorThread.getLooper();
        sensorHandler = new Handler(looper);

        accelerometerReceiver = new AccelerometerReceiver();
        accelerometerReceiver.register(this, sensorHandler);
        linearAccelerometerReceiver = new LinearAccelerometerReceiver();
        linearAccelerometerReceiver.register(this, sensorHandler);
        gyroscopeReceiver = new GyroscopeReceiver();
        gyroscopeReceiver.register(this, sensorHandler);
        locationReceiver = new LocationReceiver();
        locationReceiver.register(this, sensorHandler);
        temperatureReceiver = new TemperatureReceiver();
        temperatureReceiver.register(this, sensorHandler);
    }

    private void unregisterListeners(){
        accelerometerReceiver.unregister(this);
        linearAccelerometerReceiver.unregister(this);
        gyroscopeReceiver.unregister(this);
        locationReceiver.unregister(this);
        temperatureReceiver.unregister(this);

        sensorHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sensorThread.quitSafely();
        } else {
            sensorThread.quit();
        }
    }

    private void startMonitoring() {
        registerListeners();

        Aware.startLocations(this);
        Aware.startAccelerometer(this);
        Aware.startGyroscope(this);
        Aware.startLinearAccelerometer(this);
//        Aware.startPlugin(this, Settings.FUSED_LOCATION_PACKAGE);
        Aware.startPlugin(this, Settings.OPEN_WEATHER_PACKAGE);
    }

    private void stopMonitoring() {
        Aware.stopLocations(this);
        Aware.stopAccelerometer(this);
        Aware.stopGyroscope(this);
        Aware.stopLinearAccelerometer(this);

        unregisterListeners();

        //TODO ??
//        Aware.stopPlugin(this, Settings.FUSED_LOCATION_PACKAGE);
//        Aware.stopPlugin(this, Settings.OPEN_WEATHER_PACKAGE);
    }

}
