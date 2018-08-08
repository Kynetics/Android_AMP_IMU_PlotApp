package com.kynetics.ampsensors.math;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
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
    private Sensor accelerometerSensor;
    private Sensor magnetometerSensor;
    private Sensor gyroscopeSensor;
    private float[] accelerometerValue = null;
    private float[] magnetometerValue = null;
    private float[] gyroscopeValue = null;
    private PlotFragment.MyEntry myEntry ;

    public SensorInputConsumer(PlotFragment plotUpdate, SensorManager sensorManager, Sensor accelerometerSensor, Sensor magnetometerSensor, Sensor gyroscopeSensor) {
        this.plotUpdate = plotUpdate;
        this.sensorManager = sensorManager;
        this.accelerometerSensor = accelerometerSensor;
        this.magnetometerSensor = magnetometerSensor;
        this.gyroscopeSensor = gyroscopeSensor;
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
        myEntry = new PlotFragment.MyEntry(++indexEntry);
        this.cdl = new CountDownLatch(1);
        running = true;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sensorManager.registerListener(SensorInputConsumer.this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(SensorInputConsumer.this, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(SensorInputConsumer.this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
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
                        if(myEntry.isReady() ) {
                            doStep();
                            myEntry = new PlotFragment.MyEntry(++indexEntry);
                        }
                        Thread.sleep(300);
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
        sensorManager.unregisterListener(this, accelerometerSensor);
//        sensorManager.unregisterListener(this, magnetometerSensor);
//        sensorManager.unregisterListener(this, gyroscopeSensor);
    }



    private void doStep() throws IOException {
        this.plotUpdate.onDataReady(myEntry);
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
//            Log.d("fill acc", ""+event.values[i] - gravity[i]);
            switch (i){
                case 0:
                    myEntry.setAcc_x(event.values[i] - gravity[i]);
                    break;
                case 1:
                    myEntry.setAcc_y(event.values[i] - gravity[i]);
                    break;
                case 2:
                    myEntry.setAcc_z(event.values[i] - gravity[i]);
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

        myEntry.setGyro_x(this.gyroscopeValue[0]);
        myEntry.setGyro_y(this.gyroscopeValue[1]);
        myEntry.setGyro_z(this.gyroscopeValue[2]);
    }

    private void updateMagnetometer(SensorEvent sensorEvent) {

        // get values for each axes X,Y,Z
        for(int i = 0; i<3; i++){
            Log.d("fill mag", ""+(sensorEvent.values[i]));
            switch (i){
                case 0:
                    myEntry.setMag_x(sensorEvent.values[i]);
                    break;
                case 1:
                    myEntry.setMag_y(sensorEvent.values[i]);
                    break;
                case 2:
                    myEntry.setMag_z(sensorEvent.values[i]);
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
