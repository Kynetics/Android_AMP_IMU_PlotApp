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

package com.kynetics.ampsensors.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.kynetics.ampsensors.R;
import com.kynetics.ampsensors.device.Coordinate;
import com.kynetics.ampsensors.device.DataType;
import com.kynetics.ampsensors.device.Sensor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class PlotFragment extends Fragment implements PlotUpdate {

    private LineChart lineChartAcc;
    private LineChart lineChartMag;
    private LineChart lineChartGyro;
    public static final int PLOT_POINTS = 30;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plot_fragment, container, false);
        this.lineChartAcc = view.findViewById(R.id.chartAcc);
//        this.lineChartMag = view.findViewById(R.id.chartMag);
//        this.lineChartGyro = view.findViewById(R.id.chartGyro);
        List<LineChart> charts = Arrays.asList(this.lineChartAcc);//, this.lineChartMag, this.lineChartGyro);
        for (int s = 0; s < charts.size(); s++) {
            LineChart chart = charts.get(s);
            Sensor.values()[s].configureAxis(chart.getXAxis());
            chart.setVisibleYRangeMaximum(10, YAxis.AxisDependency.LEFT);
            chart.getDescription().setEnabled(false);
            chart.setScaleEnabled(false);
            chart.setPinchZoom(false);
            chart.setDoubleTapToZoomEnabled(false);
            LineData lineData = new LineData();
            for (int i = 0; i < getDataType().getDimension(); i++) {
                LineDataSet lineDataSet = null;
                if(this.getDataType().equals(DataType.NORM_DATA)){
                    lineDataSet = new LineDataSet(new ArrayList<>(), Sensor.values()[s].getLabel() + " " + Sensor.values()[s].getUnit());
                }
                else {
                    lineDataSet = new LineDataSet(new ArrayList<>(), Sensor.values()[s].getLabel() + " " + Coordinate.values()[i].getLabel() + " " + Sensor.values()[s].getUnit());
                }
                Coordinate.values()[i].configureDataSet(lineDataSet);
                for (int k = 0; k < PLOT_POINTS; k++) {
                    lineDataSet.addEntry(new Entry(k, 0));

                }
                lineData.addDataSet(lineDataSet);
            }
            chart.setData(lineData);
        }
        return view;
    }

    protected abstract DataType getDataType();

    @Override
    public void onDataReady(List<Entry>  entryList, Sensor sensor, Coordinate[] coordinate) {
        Activity parentActivity = getActivity();
        if(parentActivity != null){
             parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _onDataReady(entryList, sensor, coordinate);
            }
        });
        }
    }

    @Override
    public void onDataReady(PlotFragment.MyEntry entry) {
        Activity parentActivity = getActivity();
        if(parentActivity != null){
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    LineData lineData = PlotFragment.this.lineChartAcc.getLineData();
                    ILineDataSet lineDataSet = lineData.getDataSetByIndex(0);
                    lineDataSet.removeFirst();
                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getAcc_x()));
                    lineData.notifyDataChanged();

                    lineDataSet = lineData.getDataSetByIndex(1);
                    lineDataSet.removeFirst();
                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getAcc_y()));
                    lineData.notifyDataChanged();

                    lineDataSet = lineData.getDataSetByIndex(2);
                    lineDataSet.removeFirst();
                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getAcc_z()));
                    lineData.notifyDataChanged();

                    PlotFragment.this.lineChartAcc.notifyDataSetChanged();
                    PlotFragment.this.lineChartAcc.invalidate();

//
//                    lineData = PlotFragment.this.lineChartGyro.getLineData();
//                    lineDataSet = lineData.getDataSetByIndex(0);
//                    lineDataSet.removeFirst();
//                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getGyro_x()));
//                    lineData.notifyDataChanged();
//
//                    lineDataSet = lineData.getDataSetByIndex(1);
//                    lineDataSet.removeFirst();
//                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getGyro_y()));
//
//                    PlotFragment.this.lineChartGyro.notifyDataSetChanged();
//                    PlotFragment.this.lineChartGyro.invalidate();
//
//
//                    lineData = PlotFragment.this.lineChartMag.getLineData();
//                    lineDataSet = lineData.getDataSetByIndex(0);
//                    lineDataSet.removeFirst();
//                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getMag_x()));
//                    lineData.notifyDataChanged();
//
//                    lineDataSet = lineData.getDataSetByIndex(1);
//                    lineDataSet.removeFirst();
//                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getMag_y()));
//                    lineData.notifyDataChanged();
//
//                    lineDataSet = lineData.getDataSetByIndex(2);
//                    lineDataSet.removeFirst();
//                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getMag_z()));
//                    lineData.notifyDataChanged();
//
//                    PlotFragment.this.lineChartMag.notifyDataSetChanged();
//                    PlotFragment.this.lineChartMag.invalidate();         lineData.notifyDataChanged();
//
//                    lineDataSet = lineData.getDataSetByIndex(2);
//                    lineDataSet.removeFirst();
//                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getGyro_z()));
//                    lineData.notifyDataChanged();
//
//                    PlotFragment.this.lineChartGyro.notifyDataSetChanged();
//                    PlotFragment.this.lineChartGyro.invalidate();
//
//
//                    lineData = PlotFragment.this.lineChartMag.getLineData();
//                    lineDataSet = lineData.getDataSetByIndex(0);
//                    lineDataSet.removeFirst();
//                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getMag_x()));
//                    lineData.notifyDataChanged();
//
//                    lineDataSet = lineData.getDataSetByIndex(1);
//                    lineDataSet.removeFirst();
//                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getMag_y()));
//                    lineData.notifyDataChanged();
//
//                    lineDataSet = lineData.getDataSetByIndex(2);
//                    lineDataSet.removeFirst();
//                    lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getMag_z()));
//                    lineData.notifyDataChanged();
//
//                    PlotFragment.this.lineChartMag.notifyDataSetChanged();
//                    PlotFragment.this.lineChartMag.invalidate();


                }
            });
        }
    }


    public static class MyEntry{
        private final int index;
        private  Float acc_x;
        private  Float acc_y;
        private  Float acc_z;

        private  Float mag_x = new Float(0);
        private  Float mag_y = new Float(0);
        private  Float mag_z = new Float(0);

        private  Float gyro_x = new Float(0);
        private  Float gyro_y = new Float(0);
        private  Float gyro_z= new Float(0);

        public MyEntry(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public float getAcc_x() {
            return acc_x;
        }

        public void setAcc_x(float acc_x) {
            this.acc_x = acc_x;
        }

        public float getAcc_y() {
            return acc_y;
        }

        public void setAcc_y(float acc_y) {
            this.acc_y = acc_y;
        }

        public float getAcc_z() {
            return acc_z;
        }

        public void setAcc_z(float acc_z) {
            this.acc_z = acc_z;
        }

        public float getMag_x() {
            return mag_x;
        }

        public void setMag_x(float mag_x) {
            this.mag_x = mag_x;
        }

        public float getMag_y() {
            return mag_y;
        }

        public void setMag_y(float mag_y) {
            this.mag_y = mag_y;
        }

        public float getMag_z() {
            return mag_z;
        }

        public void setMag_z(float mag_z) {
            this.mag_z = mag_z;
        }

        public float getGyro_x() {
            return gyro_x;
        }

        public void setGyro_x(float gyro_x) {
            this.gyro_x = gyro_x;
        }

        public float getGyro_y() {
            return gyro_y;
        }

        public void setGyro_y(float gyro_y) {
            this.gyro_y = gyro_y;
        }

        public float getGyro_z() {
            return gyro_z;
        }

        public void setGyro_z(float gyro_z) {
            this.gyro_z = gyro_z;
        }

        public boolean isReady(){
            return acc_x != null && acc_y != null && acc_z != null &&
                    gyro_x != null && gyro_y != null && gyro_z != null &&
                    mag_x != null && mag_y != null && mag_z != null;
        }
    }
    private void _onDataReady(List<Entry>  entryList, Sensor sensor, Coordinate[] coordinate) {
//        for(Entry entry : entryList) {
        for (int i = 0; i < entryList.size(); i++) {
            Log.d("PlotFragment", "" + entryList.get(i) + "\n" + sensor + "\n" + coordinate[i%3].ordinal() + "\n");
            LineChart chart = null;
            switch (sensor) {
                case ACC:
                    chart = this.lineChartAcc;
                    break;
                case MAG:
                    chart = this.lineChartMag;
                    break;
                case GYR:
                    chart = this.lineChartGyro;
                    break;
            }
            ILineDataSet lineDataSet = chart.getLineData().getDataSetByIndex(coordinate[i%3].ordinal());

            lineDataSet.removeFirst();
            lineDataSet.addEntry(entryList.get(i));

            chart.getLineData().notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();

        }

    }

}
