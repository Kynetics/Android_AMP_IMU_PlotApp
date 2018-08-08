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


import com.github.mikephil.charting.data.PieEntry;
import com.kynetics.ampsensors.device.BoardType;
import com.kynetics.ampsensors.device.DeviceManager;
import com.kynetics.ampsensors.device.DeviceManagerAware;
import com.kynetics.ampsensors.device.InfoConsumer;
import com.kynetics.ampsensors.device.InfoType;
import com.kynetics.ampsensors.ui.AlertDialogUpdate;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class StatisticsInfoConsumer implements InfoConsumer, DeviceManagerAware {

    private DeviceManager deviceManager = null;
    private DataInputStream inputStream = null;
    private final AlertDialogUpdate alertDialogUpdate;
    private byte[] buf = null;
    private String[] stringArray = null;
    private volatile boolean running = true;
    private CountDownLatch cdl = null;
    private List<PieEntry> retListPieChart;
    private String[] retStringArray = null;
    private int[] retQueueArray = null;
    private HashMap<String, Float> hashMap;
    private  int BUF_LENGTH ;
    private final int STRING_ARRAY_LENGTH = 15;
    private final int RET_STRING_ARRAY_LENGTH = 10;
    private  int RET_QUEUE_ARRAY_LENGTH = 2;
    private  BoardType boardType;

    public StatisticsInfoConsumer(AlertDialogUpdate alertDialogUpdate) {
        this.alertDialogUpdate = alertDialogUpdate;
    }

    @Override
    public void onDeviceManagerCreated(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Override
    public void onStreamOpen(InputStream inputStream, BoardType boardType) {
        this.boardType = boardType;
        if(boardType.equals(BoardType.D)){
            BUF_LENGTH = 186;
            RET_QUEUE_ARRAY_LENGTH = 2;
        }
        else{
            BUF_LENGTH = 175;
            RET_QUEUE_ARRAY_LENGTH = 0;
        }
        assert deviceManager != null;
        assert this.inputStream == null;
        this.inputStream = new DataInputStream(inputStream);
        this.buf = new byte[BUF_LENGTH];
        this.stringArray = new String[STRING_ARRAY_LENGTH];
        this.retStringArray = new String[RET_STRING_ARRAY_LENGTH];
        if(boardType.equals(BoardType.D)){
            this.retQueueArray = new int[RET_QUEUE_ARRAY_LENGTH];
        }

        cdl = new CountDownLatch(1);
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

    private void doStep() throws IOException {
        this.retListPieChart = new ArrayList<>();
        this.readChunk();
        this.fillDataReady();
        this.alertDialogUpdate.onDataReady(this.retStringArray, this.retListPieChart, this.retQueueArray);
    }


    @Override
    public void onStreamClosing() {
        running = false;
        try {
            this.cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.inputStream = null;
    }


    private void readChunk() throws IOException {
        inputStream.readFully(buf);
        int length = 0;
        ByteBuffer bufferLittleEndian = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                this.stringArray[length] = normalizedString(convertToString(InfoType.values()[j].getDimension(), InfoType.values()[j].getOffset() + (i * 35), bufferLittleEndian.array()));
                length++;
            }
        }
        if(boardType.equals(BoardType.D)) {
            String tmpStringToParse = convertToString(InfoType.valueOf(InfoType.QUEUE.name()).getDimension(), InfoType.QUEUE.getOffset(), bufferLittleEndian.array());
            int index = tmpStringToParse.indexOf('\0');
            splitQueueString(tmpStringToParse.substring(0, index), "/");
        }
    }

    private void splitQueueString(String stringToParse, String splitSymbol) {
        String[] parts = stringToParse.split(splitSymbol);
        this.retQueueArray[0] = Integer.parseInt(parts[0]);
        this.retQueueArray[1] = Integer.parseInt(parts[1]);
    }

    private String convertToString(int length, int offset, byte[] fullByteArray) {
        byte[] partOfByteBuffer = new byte[length];
        System.arraycopy(fullByteArray, offset, partOfByteBuffer, 0, length);
        return new String(partOfByteBuffer);
    }

    private String normalizedString(String stringToNormalize) {
        return stringToNormalize.replaceAll("\u0000.*", "").replaceAll("[^\\x00-\\x7F]", "");
    }

    private void fillDataReady() {
        int index = 0;
        float smallPercValue = 0;
        float bigPercValue = 0;
        this.hashMap = new HashMap<>();
        for (int i = 0; i < this.stringArray.length; i++) {
            switch (i % 3) {
                case 0:
                    this.retStringArray[index] = this.stringArray[i] + ":";
                    index++;
                    break;
                case 1:
                    this.retStringArray[index] = this.stringArray[i];
                    index++;
                    break;
                case 2:
                    if (stringArray[i].equals("<1%")){
                        stringArray[i] = "0";
                        smallPercValue++;
                    }
                    else{
                        bigPercValue+=Float.valueOf(this.stringArray[i].replace("%", ""));
                    }
                    this.hashMap.put(this.stringArray[i - 2], Float.valueOf(this.stringArray[i].replace("%", "")));
                    break;
            }
        }
        this.fillPieChart(this.hashMap, smallPercValue, bigPercValue);
    }

    private void fillPieChart(HashMap<String, Float> hashMap, float smallPercValue, float bigPercValue){
        for(Map.Entry e : hashMap.entrySet()){
            if((Float)e.getValue()==0){
                this.retListPieChart.add(new PieEntry((Float)(100-bigPercValue)/smallPercValue, (String)e.getKey()));
            }
            else{
                this.retListPieChart.add(new PieEntry((Float) e.getValue(), (String)e.getKey()));
            }
        }
    }
}
