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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
    private final BlockingQueue<PlotFragment.ChartEntry> blockingQueue = new ArrayBlockingQueue<>(1);


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
        this.cdl = new CountDownLatch(1);
        running = true;

        HandlerThread mHandlerThread = new HandlerThread("sensorThread");

        mHandlerThread.start();

        Handler handler = new Handler(mHandlerThread.getLooper());
        sensorManager.registerListener(SensorInputConsumer.this, sensor, SensorManager.SENSOR_DELAY_NORMAL, null);
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (running) {
                    try {
                        doStep();
//                        Thread.sleep(100);
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



    private void doStep() throws IOException, InterruptedException {
        final PlotFragment.ChartEntry chartEntry = blockingQueue.take();
        this.plotUpdate.onDataReady(chartEntry);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d("update ui", "33333333333333333");
        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER :
                blockingQueue.offer(this.updateAccelerometer(sensorEvent));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD :
                blockingQueue.offer(this.updateMagnetometer(sensorEvent));
                break;
            case Sensor.TYPE_GYROSCOPE :
                blockingQueue.offer(this.updateGyroscope(sensorEvent));
                break;
        }
    }

    private PlotFragment.ChartEntry updateAccelerometer(SensorEvent event) {
        float alpha = (float) 0.8;
        float[] gravity = new float[3];
        final  PlotFragment.ChartEntry chartEntry = new PlotFragment.ChartEntry(++indexEntry);
        for (int i = 0; i < gravity.length; i++){
            gravity[i] = alpha * gravity[i] + (1 - alpha) * event.values[i];
            Log.d("fill acc", "aaaa");
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
        return chartEntry;
    }

    private PlotFragment.ChartEntry updateGyroscope(SensorEvent sensorEvent) {

        Log.d("fill gyro", ""+sensorEvent.values[0]);
        Log.d("fill gyro", ""+sensorEvent.values[1]);
        Log.d("fill gyro", ""+sensorEvent.values[2]);
        final PlotFragment.ChartEntry chartEntry = new PlotFragment.ChartEntry(++indexEntry);
        chartEntry.setX(sensorEvent.values[0]);
        chartEntry.setY(sensorEvent.values[1]);
        chartEntry.setZ(sensorEvent.values[2]);
        return chartEntry;
    }

    private PlotFragment.ChartEntry updateMagnetometer(SensorEvent sensorEvent) {
        final PlotFragment.ChartEntry chartEntry = new PlotFragment.ChartEntry(++indexEntry);
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
        return chartEntry;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
