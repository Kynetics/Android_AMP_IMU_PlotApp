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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
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


public abstract class PlotFragmentD extends PlotFragment {

    private LineChart lineChartAcc;
    private LineChart lineChartMag;
    private LineChart lineChartGyro;
    public static final int PLOT_POINTS = 30;
    private List<LineChart> charts = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("PlotD", "onCreate");
        View view = inflater.inflate(R.layout.plot_fragment_d, container, false);
        this.lineChartAcc = view.findViewById(R.id.chartAcc);
        this.lineChartMag = view.findViewById(R.id.chartMag);
        this.lineChartGyro = view.findViewById(R.id.chartGyro);
        List<LineChart> charts = Arrays.asList(this.lineChartAcc, this.lineChartMag, this.lineChartGyro);
        for (int s = 0; s < charts.size(); s++) {
            LineChart chart = charts.get(s);
            chart.getDescription().setEnabled(false);
            Sensor.values()[s].configureAxis(chart.getXAxis());
            LineData lineData = new LineData();
            for (int i = 0; i < getDataType().getDimension(); i++) {
                LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(), Sensor.values()[s].getLabel() + " " + Coordinate.values()[i].getLabel());

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


    @Override
    public void onDataReady(Entry  entry, Sensor sensor, Coordinate coordinate) {
        Activity parentActivity = getActivity();
        Log.d("PlotFragmentD", "inDataReady");
        if(parentActivity != null){
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _onDataReady(entry, sensor, coordinate);
                }
            });
        }
    }

    @Override
    public void onDataReady(PlotFragmentULP.ChartEntry entry) {

    }

    private void _onDataReady(Entry  entry, Sensor sensor, Coordinate coordinate) {
        Log.d("PlotFragmentD", "_onDataReady");
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
        ILineDataSet lineDataSet = chart.getLineData().getDataSetByIndex(coordinate.ordinal());
        lineDataSet.removeFirst();
        lineDataSet.addEntry(entry);
        chart.getLineData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    @Override
    protected DataType getDataType() {
        return null;
    }

    @Override
    public FragmentType getFragmentType() {
        return null;
    }
}
