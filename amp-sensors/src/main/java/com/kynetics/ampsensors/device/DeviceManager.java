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
