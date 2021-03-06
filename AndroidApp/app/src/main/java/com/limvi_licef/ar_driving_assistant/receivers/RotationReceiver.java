package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.config.SensorDataCollection;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeAzimuthRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAzimuthRunnable;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Statistics;
import com.limvi_licef.ar_driving_assistant.models.TimestampedDouble;

import static android.content.Context.SENSOR_SERVICE;

public class RotationReceiver implements SensorReceiver, SensorEventListener {

    public boolean isRegistered;

    private ComputeAlgorithmRunnable runnable;
    private RewriteAlgorithmRunnable rewriteRunnable;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Context context;
    private float[] orientation = new float[3];
    private float[] rMat = new float[16];
    private long previousTimestamp = 0;

    public RotationReceiver() {}

    public void register(Context context, Handler handler) {
        isRegistered = true;
        this.context = context;
        runnable = new ComputeAzimuthRunnable(handler, context);
        handler.postDelayed(runnable, SensorDataCollection.SHORT_DELAY);
        rewriteRunnable = new RewriteAzimuthRunnable(handler, context);
        handler.postDelayed(rewriteRunnable, SensorDataCollection.LONG_DELAY);
        sensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL, handler);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            savePrematurely();
            sensorManager.unregisterListener(this, rotationSensor);
            isRegistered = false;
            return true;
        }
        return false;
    }

    public void savePrematurely(){
        if(!runnable.isRunning()) runnable.run();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            if(SensorDataCollection.LOGGING_ENABLED) {
                Broadcasts.sendWriteToUIBroadcast(context, "Received Rotation Data");
            }
            if(System.currentTimeMillis() - previousTimestamp <= SensorDataCollection.MINIMUM_DELAY) return;
            if (event.values.length == 0) return;

            //Get usable data from rotation vector
            SensorManager.getRotationMatrixFromVector(rMat, event.values);

            //Round off timestamp to avoid clutter
            long roundedTimestamp = Statistics.roundOffTimestamp(System.currentTimeMillis(), SensorDataCollection.ROTATION_PRECISION);

            //calculate Azimuth and send it to runnable
            runnable.accumulateData(new TimestampedDouble(roundedTimestamp, (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360));
            previousTimestamp = System.currentTimeMillis();
        }
    }
}
