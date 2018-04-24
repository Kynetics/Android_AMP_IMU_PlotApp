package com.kynetics.ampsensors.device;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

public class DeviceManager {
    private final StreamConsumer streamConsumer;
    private final DeviceManagerAware deviceManagerAware;
    private DeviceDescriptor deviceDescriptor;
    private FileChannel fileChannel;


    static class DeviceDescriptor {
        public final int fileDescriptor;
        public final String devicePath;

        public DeviceDescriptor(int fileDescriptor, String devicePath) {
            this.fileDescriptor = fileDescriptor;
            this.devicePath = devicePath;
        }
    }

    public DeviceManager(StreamConsumer streamConsumer, DeviceManagerAware deviceManagerAware) {
        this.streamConsumer = streamConsumer;
        this.deviceManagerAware = deviceManagerAware;
        deviceManagerAware.onDeviceManagerCreated(this);
    }

    public void closeDevice() {
        if (this.deviceDescriptor != null) {
            this.streamConsumer.onStreamClosing();
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.closeDeviceNative(this.deviceDescriptor.fileDescriptor);
            this.deviceDescriptor = null;
            this.fileChannel = null;
        }
    }

    private native void closeDeviceNative(int fileDescriptor);

    public void openDevice(DataType dataType) {
        if (this.deviceDescriptor == null) {
            assert this.fileChannel == null;
            this.deviceDescriptor = this.openDeviceNative();
            try {
                RandomAccessFile raf = new RandomAccessFile(this.deviceDescriptor.devicePath, "rw");
                this.fileChannel = raf.getChannel();
                Channels.newOutputStream(fileChannel).write(dataType == DataType.VECTOR_DATA ? 1 : 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.streamConsumer.onStreamOpen(Channels.newInputStream(fileChannel), dataType);
        }
    }

    private native DeviceDescriptor openDeviceNative();

}
