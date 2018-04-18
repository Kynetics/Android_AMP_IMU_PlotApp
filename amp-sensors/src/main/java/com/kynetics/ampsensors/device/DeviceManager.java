package com.kynetics.ampsensors.device;

import android.util.Log;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

public class DeviceManager {
    private final StreamConsumer sc;
    private final DeviceManagerAware dma;
    private DeviceDescriptor dd;
    private FileChannel fileChannel;

    static {
        System.loadLibrary("native-lib");
    }


    static class DeviceDescriptor {
        public final int fileDescriptor;
        public final String devicePath;

        public DeviceDescriptor(int fileDescriptor, String devicePath) {
            this.fileDescriptor = fileDescriptor;
            this.devicePath = devicePath;
        }
    }


    public DeviceManager(StreamConsumer sc, DeviceManagerAware dma) {
        this.sc = sc;
        this.dma = dma;
        dma.onDeviceManagerCreated(this);
    }

    public void openDevice(DataType dt) {
        assert this.dd == null;
        assert this.fileChannel == null;
        this.dd = this.openDeviceNative();

        try {
            this.fileChannel = new RandomAccessFile(this.dd.devicePath, "rw").getChannel();
            Channels.newOutputStream(fileChannel).write(dt == DataType.VECTOR_DATA ? 1 : 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sc.onStreamOpen(Channels.newInputStream(fileChannel), dt);
        Log.d("print values : ", "debug1");

        Log.d("print values : ", "debug3");


    }

    public void closedData() {
        this.sc.onStreamClosing();
        try {
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.closeDeviceNative(this.dd.fileDescriptor);
        this.dd = null;
        this.fileChannel = null;
    }

    private native DeviceDescriptor openDeviceNative();

    private native void closeDeviceNative(int fd);
}
