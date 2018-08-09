package com.kynetics.ampsensors.math;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.kynetics.ampsensors.device.DeviceManager;
import com.kynetics.ampsensors.device.DeviceManagerAware;
import com.kynetics.ampsensors.device.InputConsumer;
import com.kynetics.ampsensors.ui.PlotFragment;
import com.kynetics.ampsensors.ui.PlotUpdate;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;


public class SensorInputConsumer implements InputConsumer , DeviceManagerAware , SensorEventListener {

    private final PlotUpdate plotUpdate;
    private DeviceManager deviceManager = null;
    private volatile boolean running = true;
    private CountDownLatch cdl = null;
    private int indexEntry = PlotFragment.PLOT_POINTS;
    private SensorManager sensorManager;
    private Sensor sensor;
    private float[] accelerometerValue = null;
    private float[] magnetometerValue = null;
    private float[] gyroscopeValue = null;
    private PlotFragment.ChartEntry chartEntry;

    public SensorInputConsumer(PlotFragment plotUpdate, SensorManager sensorManager) {
        this.plotUpdate = plotUpdate;
        this.sensorManager = sensorManager;
        this.sensor = plotUpdate.getFragmentType().getSensor(sensorManager);

    }

    @Override
    public void onDeviceManagerCreated(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Override
    public void onInputOpen() {
        this.accelerometerValue = new float[3];
        this.magnetometerValue = new float[3];
        this.gyroscopeValue =new float[3];
        chartEntry = new PlotFragment.ChartEntry(++indexEntry);
        this.cdl = new CountDownLatch(1);
        running = true;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HandlerThread mHandlerThread = new HandlerThread("sensorThread");


                mHandlerThread.start();

                Handler handler_roberto = new Handler(mHandlerThread.getLooper());

                sensorManager.registerListener(SensorInputConsumer.this, sensor, SensorManager.SENSOR_DELAY_UI, handler_roberto);
               // sensorManager.unregisterListener(SensorInputConsumer.this, sensor);
           //     sensorManager.registerListener(SensorInputConsumer.this, sensor, SensorManager.SENSOR_DELAY_GAME);
           //     sensorManager.unregisterListener(SensorInputConsumer.this, sensor);
           //     sensorManager.registerListener(SensorInputConsumer.this, sensor, SensorManager.SENSOR_DELAY_GAME);
           //     sensorManager.unregisterListener(SensorInputConsumer.this, sensor);
          //      sensorManager.registerListener(SensorInputConsumer.this, sensor, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(SensorInputConsumer.this, sensor, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(SensorInputConsumer.this, sensor, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(SensorInputConsumer.this, sensor, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(SensorInputConsumer.this, sensor, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(SensorInputConsumer.this, sensor, SensorManager.SENSOR_DELAY_GAME);

                startDoStepThread();
            }
        }, 5000);

    }

    private void startDoStepThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (running) {
                    try {
                        if(chartEntry.isReady() ) {
                            doStep();
                            chartEntry = new PlotFragment.ChartEntry(++indexEntry);
                        }
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                cdl.countDown();
            }
        }).start();
    }

    @Override
    public void onInputClosing() {
        running = false;
        sensorManager.unregisterListener(this, sensor);
    }



    private void doStep() throws IOException {
        this.plotUpdate.onDataReady(chartEntry);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER :
                this.updateAccelerometer(sensorEvent);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD :
                this.updateMagnetometer(sensorEvent);
                break;
            case Sensor.TYPE_GYROSCOPE :
                this.updateGyroscope(sensorEvent);
                break;
        }
    }

    private void updateAccelerometer(SensorEvent event) {
        float alpha = (float) 0.8;
        float[] gravity = new float[3];

        for (int i = 0; i < gravity.length; i++){
            gravity[i] = alpha * gravity[i] + (1 - alpha) * event.values[i];
            Log.d("fill acc", "");
            switch (i){
                case 0:
                    chartEntry.setX(event.values[i] - gravity[i]);
                    break;
                case 1:
                    chartEntry.setY(event.values[i] - gravity[i]);
                    break;
                case 2:
                    chartEntry.setZ(event.values[i] - gravity[i]);
                    break;
            }
        }
    }

    private void updateGyroscope(SensorEvent sensorEvent) {

        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(
                rotationMatrix, sensorEvent.values);
        // Remap coordinate system
        float[] remappedRotationMatrix = new float[16];
        SensorManager.remapCoordinateSystem(rotationMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                remappedRotationMatrix);
        // Convert to orientations
        SensorManager.getOrientation(remappedRotationMatrix, this.gyroscopeValue);


        Log.d("fill gyro", ""+sensorEvent.values[0]);
        Log.d("fill gyro", ""+sensorEvent.values[1]);
        Log.d("fill gyro", ""+sensorEvent.values[2]);
        chartEntry.setX(sensorEvent.values[0]);
        chartEntry.setY(sensorEvent.values[1]);
        chartEntry.setZ(sensorEvent.values[2]);
    }

    private void updateMagnetometer(SensorEvent sensorEvent) {

        // get values for each axes X,Y,Z
        for(int i = 0; i<3; i++){
            Log.d("fill mag", ""+(sensorEvent.values[i]));
            switch (i){
                case 0:
                    chartEntry.setX(sensorEvent.values[i]);
                    break;
                case 1:
                    chartEntry.setY(sensorEvent.values[i]);
                    break;
                case 2:
                    chartEntry.setZ(sensorEvent.values[i]);
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
