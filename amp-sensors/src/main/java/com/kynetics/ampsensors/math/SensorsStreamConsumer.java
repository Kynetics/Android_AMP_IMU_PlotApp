package com.kynetics.ampsensors.math;

import com.github.mikephil.charting.data.Entry;
import com.kynetics.ampsensors.device.Coordinate;
import com.kynetics.ampsensors.device.DataType;
import com.kynetics.ampsensors.device.DeviceManager;
import com.kynetics.ampsensors.device.DeviceManagerAware;
import com.kynetics.ampsensors.device.Sensor;
import com.kynetics.ampsensors.device.StreamConsumer;
import com.kynetics.ampsensors.ui.PlotFragment;
import com.kynetics.ampsensors.ui.PlotUpdate;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.CountDownLatch;

public class SensorsStreamConsumer implements StreamConsumer, DeviceManagerAware {

    private DeviceManager deviceManager = null;
    private InputStream inputStream = null;
    private byte[] buf = null;
    private float[] floatArray = null;
    private float[][] dataMatrix = null;
    private volatile boolean running = true;
    private CountDownLatch cdl = null;
    private final PlotUpdate plotUpdate;
    private int index = PlotFragment.PLOT_POINTS;

    public SensorsStreamConsumer(PlotUpdate plotUpdate) {
        this.plotUpdate = plotUpdate;
    }

    @Override
    public void onDeviceManagerCreated(DeviceManager dm) {
        this.deviceManager = dm;


    }

    @Override
    public void onStreamOpen(InputStream i, DataType dt) {
        assert deviceManager != null;
        assert inputStream == null;
        this.inputStream = i;
        this.buf = new byte[dt.getBufferSize()];
        this.floatArray = new float[dt.getSampleCount()];
        this.dataMatrix = new float[Sensor.values().length][dt.getDimension()];
        this.cdl = new CountDownLatch(1);
        running = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        doStep();
                        Thread.yield();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                cdl.countDown();
            }
        }).start();
    }

    @Override
    public void onStreamClosing() {
        running = false;
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.inputStream = null;
    }

    private void readChunk() throws IOException {
        this.inputStream.read(buf);
        ByteBuffer buffer = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer fb = buffer.asFloatBuffer();
        fb.get(floatArray);
    }

    private void trasformData() {
        for (int sensorIndex = 0; sensorIndex < dataMatrix.length; sensorIndex++) {
            for (int coordinateIndex = 0; coordinateIndex < dataMatrix[sensorIndex].length; coordinateIndex++) {
                dataMatrix[sensorIndex][coordinateIndex] = floatArray[sensorIndex * dataMatrix[sensorIndex].length + coordinateIndex];
            }
        }
    }

    private void doStep() throws IOException {
        this.readChunk();
        this.trasformData();
        for (int j = 0; j < dataMatrix.length; j++) {
            for (int i = 0; i < dataMatrix[j].length; i++) {
                this.plotUpdate.onDataReady(new Entry(index, dataMatrix[j][i]), Sensor.values()[j], Coordinate.values()[i]);
            }

        }
        index++;
    }
}
