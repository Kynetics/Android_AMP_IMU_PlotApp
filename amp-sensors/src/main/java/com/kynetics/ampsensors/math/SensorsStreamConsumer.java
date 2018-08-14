/*
 * Copyright (c) Kynetics LLC. Author: Marta Todeschini
 *
 *               This program is free software: you can redistribute it and/or modify
 *               it under the terms of the GNU General Public License as published by
 *               the Free Software Foundation, either version 3 of the License, or
 *               (at your option) any later version.
 *
 *               This program is distributed in the hope that it will be useful,
 *               but WITHOUT ANY WARRANTY; without even the implied warranty of
 *               MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *               GNU General Public License for more details.
 *
 *               You should have received a copy of the GNU General Public License
 *               along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kynetics.ampsensors.math;


import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.kynetics.ampsensors.device.Coordinate;
import com.kynetics.ampsensors.device.DataType;
import com.kynetics.ampsensors.device.DeviceManager;
import com.kynetics.ampsensors.device.DeviceManagerAware;
import com.kynetics.ampsensors.device.Sensor;
import com.kynetics.ampsensors.device.StreamConsumer;
import com.kynetics.ampsensors.ui.PlotFragment;
import com.kynetics.ampsensors.ui.PlotUpdate;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.CountDownLatch;

public class SensorsStreamConsumer implements StreamConsumer, DeviceManagerAware {

    private DeviceManager deviceManager = null;
    private DataInputStream inputStream = null;
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
    public void onDeviceManagerCreated(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Override
    public void onStreamOpen(InputStream inputStream, DataType dataType) {
        Log.d("SensorStreamConsumer", "on stream open");
        assert deviceManager != null;
        assert this.inputStream == null;
        this.inputStream = new DataInputStream(inputStream);
        this.buf = new byte[dataType.getBufferSize()];
        this.floatArray = new float[dataType.getSampleCount()];
        this.dataMatrix = new float[com.kynetics.ampsensors.device.Sensor.values().length][dataType.getDimension()];
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

        inputStream.readFully(buf);
        ByteBuffer buffer = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer fb = buffer.asFloatBuffer();
        fb.get(floatArray);
    }


    private void transformData() {
        for (int sensorIndex = 0; sensorIndex < dataMatrix.length; sensorIndex++) {
            for (int coordinateIndex = 0; coordinateIndex < dataMatrix[sensorIndex].length; coordinateIndex++) {
                dataMatrix[sensorIndex][coordinateIndex] = floatArray[sensorIndex * dataMatrix[sensorIndex].length + coordinateIndex];
            }
        }
    }

    private void doStep() throws IOException {
        Log.d("SensorStreamConsumer", "do step");
        this.readChunk();
        this.transformData();
        for (int j = 0; j < dataMatrix.length; j++) {
            for (int i = 0; i < dataMatrix[j].length; i++) {
                Log.d("SensorStreamConsumer", "\nindex : "+index+"\nvalue : "+dataMatrix[j][i]+"\nsensor "+Sensor.values()[j]+"\ncoord "+Coordinate.values()[i]);
                this.plotUpdate.onDataReady(new Entry(index, dataMatrix[j][i]), Sensor.values()[j], Coordinate.values()[i]);
            }

        }
        index++;
    }

}
