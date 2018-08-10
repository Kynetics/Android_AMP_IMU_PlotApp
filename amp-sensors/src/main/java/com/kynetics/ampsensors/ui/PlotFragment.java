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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class PlotFragment extends Fragment implements PlotUpdate {

    private LineChart lineChartAcc;
    private LineChart lineChartMag;
    private LineChart lineChartGyro;
    private LineChart chartToUpdate;
    public static final int PLOT_POINTS = 30;
    private List<LineChart> charts = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plot_fragment, container, false);
        this.lineChartAcc = view.findViewById(R.id.chartAcc);
        this.lineChartMag = view.findViewById(R.id.chartMag);
        this.lineChartGyro = view.findViewById(R.id.chartGyro);
        charts = Arrays.asList(this.lineChartAcc, this.lineChartMag, this.lineChartGyro);

        switch (this.getFragmentType()){
            case ACC:
                view.findViewById(R.id.chartMag).setVisibility(View.GONE);
                view.findViewById(R.id.chartGyro).setVisibility(View.GONE);
                chartToUpdate = lineChartAcc;
                break;
            case MAG:
                view.findViewById(R.id.chartAcc).setVisibility(View.GONE);
                view.findViewById(R.id.chartGyro).setVisibility(View.GONE);
                chartToUpdate = lineChartMag;
                break;
            case GYR:
                view.findViewById(R.id.chartMag).setVisibility(View.GONE);
                view.findViewById(R.id.chartAcc).setVisibility(View.GONE);
                chartToUpdate = lineChartGyro;
                break;
        }

        for (int s = 0; s < charts.size(); s++) {
            LineChart chart = charts.get(s);
            Sensor.values()[s].configureAxis(chart.getXAxis());
            chart.getDescription().setEnabled(false);
            chart.setPinchZoom(true);
            chart.setDoubleTapToZoomEnabled(false);
            LineData lineData = new LineData();
            for (int i = 0; i < getDataType().getDimension(); i++) {
                LineDataSet lineDataSet = null;
                switch(this.getDataType()){
                    case NORM_DATA:
                        lineDataSet = new LineDataSet(new ArrayList<>(), Sensor.values()[s].getLabel() + " " + Sensor.values()[s].getUnit());
                        break;
                    case VECTOR_DATA:
                        if(PlotFragment.this.getFragmentType().equals(FragmentType.GYR)){
                            lineDataSet = new LineDataSet(new ArrayList<>(), Sensor.values()[s].getLabel() + " " + Coordinate.values()[i+3].getLabel() + " " + Sensor.values()[s].getUnit());
                        }
                        else{
                            lineDataSet = new LineDataSet(new ArrayList<>(), Sensor.values()[s].getLabel() + " " + Coordinate.values()[i].getLabel() + " " + Sensor.values()[s].getUnit());
                        }
                        break;
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

    public abstract FragmentType getFragmentType();

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
    public void onDataReady(ChartEntry entry) {
        Activity parentActivity = getActivity();
        if(chartToUpdate != null){
            chartToUpdate.post(new Runnable() {
                @Override
                public void run() {
                    LineData lineData;
                    ILineDataSet lineDataSet;
                    switch(PlotFragment.this.getFragmentType()) {

                        case ACC:
                            updateLineChart(entry, PlotFragment.this.lineChartAcc);
                        break;
                        case MAG:
                            updateLineChart(entry, PlotFragment.this.lineChartMag);
                            break;

                        case GYR:
                            updateLineChart(entry, PlotFragment.this.lineChartGyro);
                            break;

                        case NORM:
                            updateLineChart(entry, PlotFragment.this.lineChartAcc);
                            updateLineChart(entry, PlotFragment.this.lineChartMag);
                            updateLineChart(entry, PlotFragment.this.lineChartGyro);
                            break;
                        case VECTOR:
                            updateLineChart(entry, PlotFragment.this.lineChartAcc);
                            updateLineChart(entry, PlotFragment.this.lineChartMag);
                            updateLineChart(entry, PlotFragment.this.lineChartGyro);
                            break;
                    }
                }
            });
        }
    }

    private void updateLineChart(ChartEntry entry, LineChart lineChart) {
        LineData lineData = lineChart.getLineData();
        ILineDataSet lineDataSet = lineData.getDataSetByIndex(0);
        lineDataSet.removeFirst();
        lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getX()));
        lineData.notifyDataChanged();

        lineDataSet = lineData.getDataSetByIndex(1);
        lineDataSet.removeFirst();
        lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getY()));
        lineData.notifyDataChanged();

        lineDataSet = lineData.getDataSetByIndex(2);
        lineDataSet.removeFirst();
        lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getZ()));
        lineData.notifyDataChanged();

        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public static class ChartEntry {
        private final int index;
        private  Float x;
        private  Float y;
        private  Float z;

        public ChartEntry(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public Float getX() {
            return x;
        }

        public void setX(Float x) {
            this.x = x;
        }

        public Float getY() {
            return y;
        }

        public void setY(Float y) {
            this.y = y;
        }

        public Float getZ() {
            return z;
        }

        public void setZ(Float z) {
            this.z = z;
        }

    }


    private void _onDataReady(List<Entry>  entryList, Sensor sensor, Coordinate[] coordinate) {
        for (int i = 0; i < entryList.size(); i++) {
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
