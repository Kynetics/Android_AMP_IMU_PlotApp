package com.example.marta.hello_cmake;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.softmoore.android.graphlib.Graph;
import com.softmoore.android.graphlib.GraphView;
import com.softmoore.android.graphlib.Point;
import android.graphics.Color;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class RawFragment extends Fragment {


    static {
        System.loadLibrary("native-lib");
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_raw, container, false);
        String devicePath = this.getArguments().getString("path");
        readFromDevice(devicePath, view);

        TextView textView = view.findViewById(R.id.graph_view_label);
        textView.setText("Raw mode");

        return view;
    }


    private  void readFromDevice(String path, View view) {
        Log.d("path to read  ", path);


        DataOutputStream outputStream  = null;

        byte[] byteArrayExample = new byte[360];

        try {

            File file = new File(path);
            Log.d("$$$ ", "file exist:" + file.exists() + "read: "+ file.canRead()+ "write: "+ file.canWrite());

            FileChannel fileChannel = new RandomAccessFile(file.getAbsolutePath(), "rw").getChannel();
            Log.d("print values : ", "debug1");

            outputStream = new DataOutputStream(Channels.newOutputStream(fileChannel));
            Log.d("print values : ", "debug2");

            final byte bToSEnd = (byte) 1;
            outputStream.write(bToSEnd);

            Log.d("print values : ", "debug3");
            //outputStream.close();

            Log.d("print values : ", "debug4");
            InputStream inRaw = Channels.newInputStream(fileChannel);
            InputStream dataInputStream = new DataInputStream(inRaw);
            Log.d("print values : ", "debug5");

            dataInputStream.read(byteArrayExample, 0, 360);

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

    private void plotPoints(float[] retBuffer , View view) {
        Log.d("buffer length raw : ", String.valueOf(retBuffer.length));


        LineChart lineChartAcc = view.findViewById(R.id.chartAcc);
        LineChart lineChartMag = view.findViewById(R.id.chartMag);
        LineChart lineChartGyro = view.findViewById(R.id.chartGyro);

        ArrayList<Entry> entriesAccFirst = getValues(0, retBuffer);
        ArrayList<Entry> entriesAccSecond = getValues(1, retBuffer);
        ArrayList<Entry> entriesAccThird = getValues(2, retBuffer);
        ArrayList<Entry> entriesMagFirst = getValues(3, retBuffer);
        ArrayList<Entry> entriesMagSecond = getValues(4, retBuffer);
        ArrayList<Entry> entriesMagThird = getValues(5, retBuffer);
        ArrayList<Entry> entriesGyroFirst = getValues(6, retBuffer);
        ArrayList<Entry> entriesGyroSecond = getValues(7, retBuffer);
        ArrayList<Entry> entriesGyroThird = getValues(8, retBuffer);

        LineDataSet dataSetAccFirst = new LineDataSet(entriesAccFirst, "accelerator first");
        dataSetAccFirst.setCircleRadius(2f);
        dataSetAccFirst.setColors(Color.RED);
        dataSetAccFirst.setValueTextSize(10f);
        dataSetAccFirst.setCircleColor(Color.BLACK);
        dataSetAccFirst.setLineWidth(1.5f);
        LineDataSet dataSetAccSecond = new LineDataSet(entriesAccSecond, "accelerator second");
        dataSetAccSecond.setCircleRadius(2f);
        dataSetAccSecond.setColors(Color.BLUE);
        dataSetAccSecond.setValueTextSize(10f);
        dataSetAccSecond.setCircleColor(Color.BLACK);
        dataSetAccSecond.setLineWidth(1.5f);
        LineDataSet dataSetAccThird = new LineDataSet(entriesAccThird, "accelerator third");
        dataSetAccThird.setCircleRadius(2f);
        dataSetAccThird.setColors(Color.GREEN);
        dataSetAccThird.setValueTextSize(10f);
        dataSetAccThird.setCircleColor(Color.BLACK);
        dataSetAccThird.setLineWidth(1.5f);

        LineDataSet dataSetMagFirst = new LineDataSet(entriesMagFirst, "magnetometer first");
        dataSetMagFirst.setCircleRadius(2f);
        dataSetMagFirst.setColors(Color.RED);
        dataSetMagFirst.setValueTextSize(10f);
        dataSetMagFirst.setCircleColor(Color.BLACK);
        dataSetMagFirst.setLineWidth(1.5f);
        LineDataSet dataSetMagSecond = new LineDataSet(entriesMagSecond, "magnetometer second");
        dataSetMagSecond.setCircleRadius(2f);
        dataSetMagSecond.setColors(Color.BLUE);
        dataSetMagSecond.setValueTextSize(10f);
        dataSetMagSecond.setCircleColor(Color.BLACK);
        dataSetMagFirst.setLineWidth(1.5f);
        LineDataSet dataSetMagThird = new LineDataSet(entriesMagThird, "magnetometer third");
        dataSetMagThird.setCircleRadius(2f);
        dataSetMagThird.setColors(Color.GREEN);
        dataSetMagThird.setValueTextSize(10f);
        dataSetMagThird.setCircleColor(Color.BLACK);
        dataSetMagFirst.setLineWidth(1.5f);

        LineDataSet dataSetGyroFirst = new LineDataSet(entriesGyroFirst, "gyroscope first");
        dataSetGyroFirst.setCircleRadius(2f);
        dataSetGyroFirst.setColors(Color.RED);
        dataSetGyroFirst.setValueTextSize(10f);
        dataSetGyroFirst.setCircleColor(Color.BLACK);
        dataSetGyroFirst.setLineWidth(1.5f);
        LineDataSet dataSetGyroSecond = new LineDataSet(entriesGyroSecond, "gyroscope second");
        dataSetGyroSecond.setCircleRadius(2f);
        dataSetGyroSecond.setColors(Color.BLUE);
        dataSetGyroSecond.setValueTextSize(10f);
        dataSetGyroSecond.setCircleColor(Color.BLACK);
        dataSetGyroSecond.setLineWidth(1.5f);
        LineDataSet dataSetGyroThird = new LineDataSet(entriesGyroThird, "gyroscope third");
        dataSetGyroThird.setCircleRadius(2f);
        dataSetGyroThird.setColors(Color.GREEN);
        dataSetGyroThird.setValueTextSize(10f);
        dataSetGyroThird.setCircleColor(Color.BLACK);
        dataSetGyroThird.setLineWidth(1.5f);


        ArrayList<ILineDataSet> dataSetsAcc = new ArrayList<>();
        dataSetsAcc.add(dataSetAccFirst);
        dataSetsAcc.add(dataSetAccSecond);
        dataSetsAcc.add(dataSetAccThird);
        LineData dataAcc = new LineData(dataSetsAcc);
        lineChartAcc.setData(dataAcc);
        lineChartAcc.setPinchZoom(true);

        ArrayList<ILineDataSet> dataSetsMag = new ArrayList<>();
        dataSetsMag.add(dataSetMagFirst);
        dataSetsMag.add(dataSetMagSecond);
        dataSetsMag.add(dataSetMagThird);
        LineData dataMag = new LineData(dataSetsMag);
        lineChartMag.setData(dataMag);
        lineChartMag.setPinchZoom(true);

        ArrayList<ILineDataSet> dataSetsGyro = new ArrayList<>();
        dataSetsGyro.add(dataSetGyroFirst);
        dataSetsGyro.add(dataSetGyroSecond);
        dataSetsGyro.add(dataSetGyroThird);
        LineData dataGyro = new LineData(dataSetsGyro);
        lineChartGyro.setData(dataGyro);
        lineChartGyro.setPinchZoom(true);

        // xAxis customization
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

    private ArrayList<Entry> getValues(int type, float[] retBuffer) {
        ArrayList<Entry> retArrayList = new ArrayList<>();
        int index = 0;
        for(int i = type ; i < retBuffer.length; i+=9){
            retArrayList.add(new Entry(index, retBuffer[i]));
            Log.d("buffer", "indice "+i+" "+String.valueOf(retBuffer[i]));
            index++;
        }
        return retArrayList;
    }

}
