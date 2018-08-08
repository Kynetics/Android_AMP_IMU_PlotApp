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

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

public class DeviceManager {
    private final StreamConsumer streamConsumer;
    private final DeviceManagerAware deviceManagerAware;
    private DeviceDescriptor deviceDescriptor;
    private FileChannel fileChannelImu;
    private FileChannel fileChannelStat;
    private InfoConsumer infoConsumer;
    private InputConsumer inputConsumer;


    static class DeviceDescriptor {
        public final int fileDescriptor;
        public final String devicePathImu;
        public final String devicePathStat;

        public DeviceDescriptor(int fileDescriptor, String devicePathImu, String devicePathStat) {
            this.fileDescriptor = fileDescriptor;
            this.devicePathImu = devicePathImu;
            this.devicePathStat = devicePathStat;
        }
    }


    public DeviceManager(StreamConsumer streamConsumer, DeviceManagerAware deviceManagerAware, InfoConsumer infoConsumer, InputConsumer inputConsumer) {
        this.streamConsumer = streamConsumer;
        this.deviceManagerAware = deviceManagerAware;
        deviceManagerAware.onDeviceManagerCreated(this);
        this.infoConsumer = infoConsumer;
        this.inputConsumer = inputConsumer;
    }

    public void closeDevice(BoardType boardType) {
        if (this.deviceDescriptor != null) {
            this.streamConsumer.onStreamClosing();
            this.infoConsumer.onStreamClosing();
            this.inputConsumer.onInputClosing();
            try {
                switch (boardType){
                    case D:
                        fileChannelImu.close();
                        break;
                }
                fileChannelStat.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.closeDeviceNative(this.deviceDescriptor.fileDescriptor);
            this.deviceDescriptor = null;
            this.fileChannelImu = null;
            this.fileChannelStat = null;
        }
    }

    private native void closeDeviceNative(int fileDescriptor);

    public void openDevice(DataType dataType, BootType bootType, BoardType boardType) {
        Log.d("******data type", "**********************************"+dataType);
        if (this.deviceDescriptor == null) {
            assert this.fileChannelImu == null;
            assert this.fileChannelStat == null;
            this.deviceDescriptor = this.openDeviceNative(boardType);

            switch (boardType){
                case D :
                    try {
                        /*Channel for Imu*/
                        if(boardType.equals(BoardType.D)) {
                            RandomAccessFile rafImu = new RandomAccessFile(this.deviceDescriptor.devicePathImu, "rw");
                            this.fileChannelImu = rafImu.getChannel();
                            String dataTypeString = ((dataType == DataType.VECTOR_DATA) ? "VECTOR" : "NORM");
                            byte[] byteArrayDataType = new byte[10];
                            System.arraycopy(dataTypeString.getBytes(), 0, byteArrayDataType, 0, dataTypeString.length());
                            byte[] byteArrayBootType = new byte[1];
                            byteArrayBootType[0] = (byte) ((bootType == BootType.ON_START) ? 1 : 0);
                            ByteArrayOutputStream outByteArray = new ByteArrayOutputStream();
                            outByteArray.write(byteArrayDataType);
                            outByteArray.write(byteArrayBootType);
                            byte bigByte[] = outByteArray.toByteArray();
                            Channels.newOutputStream(fileChannelImu).write(bigByte);
                            this.streamConsumer.onStreamOpen(Channels.newInputStream(fileChannelImu), dataType);

                            /*Channel for Statistics*/
                            RandomAccessFile rafStat = new RandomAccessFile(this.deviceDescriptor.devicePathStat, "r");
                            this.fileChannelStat = rafStat.getChannel();
                            this.infoConsumer.onStreamOpen(Channels.newInputStream(fileChannelStat), boardType);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case ULP:
                    try {
                        /*Channel for Statistics*/
                        RandomAccessFile rafStat = new RandomAccessFile(this.deviceDescriptor.devicePathStat, "r");
                        this.fileChannelStat = rafStat.getChannel();
                        this.infoConsumer.onStreamOpen(Channels.newInputStream(fileChannelStat), boardType);

                        /*Input for sensor*/
                        this.inputConsumer.onInputOpen();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private native DeviceDescriptor openDeviceNative(BoardType boardType);

}
