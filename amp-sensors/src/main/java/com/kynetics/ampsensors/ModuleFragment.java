package com.kynetics.ampsensors;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;



public class ModuleFragment extends Fragment {





    static {
        System.loadLibrary("native-lib");
    }

    private ArrayList<Entry> entriesAcc;
    private ArrayList<Entry> entriesMag;
    private ArrayList<Entry> entriesGyro;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_module, container, false);
        String devicePath = this.getArguments().getString("path");
        Log.d("Module fragment path  ", devicePath);
        readFromDevice(devicePath, view);


 //       destroyDevice();
        Log.d("print values : ", "destroy");

        return view;
    }


    private  void readFromDevice(String path, View view) {
        Log.d("path to read  ", path);


        DataOutputStream outputStream;
        byte[] byteArrayExample = new byte[600];


        try {

            File file = new File(path);
            Log.d("$$$ ", "file exist:" + file.exists() + "read: "+ file.canRead()+ "write: "+ file.canWrite());

            FileChannel fileChannel = new RandomAccessFile(file.getAbsolutePath(), "rw").getChannel();
            Log.d("print values : ", "debug1");

            outputStream = new DataOutputStream(Channels.newOutputStream(fileChannel));
            Log.d("print values : ", "debug2");

            final byte b = (byte) 0;
            outputStream.write(b);
            Log.d("print values : ", "debug3");
            //outputStream.close();

            Log.d("print values : ", "debug4");
            InputStream inRaw = Channels.newInputStream(fileChannel);
            InputStream dataInputStream = new DataInputStream(inRaw);
            Log.d("print values : ", "debug5");
            dataInputStream.read(byteArrayExample, 0, 600);

            Log.d("print values : ", "debug6");
            outputStream.close();
            dataInputStream.close();

        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString(), e);

        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString(), e);
        }
        Log.d("print values : ", "debug7");
        ByteBuffer buffer = ByteBuffer.wrap(byteArrayExample).order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer fb = buffer.asFloatBuffer();
        float[] floatArray = new float[fb.capacity()];
        fb.get(floatArray);


        plotPoints(floatArray, view);
    }


    private void plotPoints(float[] retBuffer, View view) {
        Log.d("buffer length module : ", String.valueOf(retBuffer.length));

        LineChart lineChartAcc = view.findViewById(R.id.chartAcc);
        LineChart lineChartMag = view.findViewById(R.id.chartMag);
        LineChart lineChartGyro = view.findViewById(R.id.chartGyro);

//        ArrayList<Entry> entriesAcc = getValues(0, retBuffer);
//        ArrayList<Entry> entriesMag = getValues(1, retBuffer);
//        ArrayList<Entry> entriesGyro = getValues(2, retBuffer);

        getValues(entriesAcc, entriesMag, entriesGyro, retBuffer);

        LineDataSet dataSetAcc = new LineDataSet(entriesAcc, "accelerator");
        dataSetAcc.setCircleRadius(2f);
        dataSetAcc.setColors(Color.BLUE);
        dataSetAcc.setValueTextSize(10f);
        dataSetAcc.setCircleColor(Color.BLACK);
        dataSetAcc.setLineWidth(1.5f);
        LineDataSet dataSetMag = new LineDataSet(entriesMag, "magnetometer");
        dataSetMag.setCircleRadius(2f);
        dataSetMag.setColors(Color.BLUE);
        dataSetMag.setValueTextSize(10f);
        dataSetMag.setCircleColor(Color.BLACK);
        dataSetMag.setLineWidth(1.5f);
        LineDataSet dataSetGyro = new LineDataSet(entriesGyro, "gyroscope");
        dataSetGyro.setCircleRadius(2f);
        dataSetGyro.setColors(Color.BLUE);
        dataSetGyro.setValueTextSize(10f);
        dataSetGyro.setCircleColor(Color.BLACK);
        dataSetGyro.setLineWidth(1.5f);

        LineData lineDataAcc = new LineData(dataSetAcc);
        LineData lineDataMag = new LineData(dataSetMag);
        LineData lineDataGyro = new LineData(dataSetGyro);

        lineChartAcc.setData(lineDataAcc);
        lineChartAcc.setPinchZoom(true);
        lineChartMag.setData(lineDataMag);
        lineChartMag.setPinchZoom(true);
        lineChartGyro.setData(lineDataGyro);
        lineChartGyro.setPinchZoom(true);

        XAxis xAxisAcc = lineChartAcc.getXAxis();
        xAxisAcc.setGranularity(1f);
        xAxisAcc.setLabelCount(10);
        xAxisAcc.setGranularityEnabled(true);
        xAxisAcc.setCenterAxisLabels(false);
        xAxisAcc.setDrawGridLines(true);
        xAxisAcc.setTextColor(Color.BLACK);
        xAxisAcc.setPosition(XAxis.XAxisPosition.BOTTOM);

        XAxis xAxisMag = lineChartMag.getXAxis();
        xAxisMag.setGranularity(1f);
        xAxisMag.setLabelCount(10);
        xAxisMag.setGranularityEnabled(true);
        xAxisMag.setCenterAxisLabels(false);
        xAxisMag.setDrawGridLines(true);
        xAxisMag.setTextColor(Color.BLACK);
        xAxisMag.setPosition(XAxis.XAxisPosition.BOTTOM);

        XAxis xAxisGyro = lineChartGyro.getXAxis();
        xAxisGyro.setGranularity(1f);
        xAxisGyro.setLabelCount(10);
        xAxisGyro.setGranularityEnabled(true);
        xAxisGyro.setCenterAxisLabels(false);
        xAxisGyro.setDrawGridLines(true);
        xAxisGyro.setTextColor(Color.BLACK);
        xAxisGyro.setPosition(XAxis.XAxisPosition.BOTTOM);

    }

    private ArrayList<Entry> getValues(ArrayList<Entry> entriesAcc, ArrayList<Entry> entriesMag, ArrayList<Entry> entriesGyro, float[] retBuffer) {
        ArrayList<Entry> retArrayList = new ArrayList<>();
        int index = 0;
        for(int i = 0 ; i < retBuffer.length; i+=3){
            retArrayList.add(new Entry(index, retBuffer[i]));
            Log.d("buffer", "indice "+i+" "+String.valueOf(retBuffer[i]));
            index++;
        }
        return retArrayList;
    }


    public native String destroyDevice();


}





