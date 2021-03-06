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
import com.kynetics.ampsensors.device.Sensor;
import java.util.ArrayList;

public abstract class PlotFragmentULP extends PlotFragment {

    private LineChart lineGeneralChart;
    public static final int PLOT_POINTS = 30;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plot_fragment_ulp, container, false);
        this.lineGeneralChart = view.findViewById(R.id.generalChart);
        int s = this.getFragmentType().ordinal();
        Sensor.values()[s].configureAxis(lineGeneralChart.getXAxis());
        this.lineGeneralChart.getDescription().setEnabled(false);
        this.lineGeneralChart.setPinchZoom(true);
        this.lineGeneralChart.setDoubleTapToZoomEnabled(false);
        LineData lineData = new LineData();
        for (int i = 0; i < getDataType().getDimension(); i++) {
            LineDataSet lineDataSet = null;
            if(PlotFragmentULP.this.getFragmentType().equals(FragmentType.GYR)){
                lineDataSet = new LineDataSet(new ArrayList<>(), Sensor.values()[s].getLabel() + " " + Coordinate.values()[i+3].getLabel() + " " + Sensor.values()[s].getUlpUnit());
            }
            else{
                lineDataSet = new LineDataSet(new ArrayList<>(), Sensor.values()[s].getLabel() + " " + Coordinate.values()[i].getLabel() + " " + Sensor.values()[s].getUlpUnit());
            }
            Coordinate.values()[i].configureDataSet(lineDataSet);
            for (int k = 0; k < PLOT_POINTS; k++) {
                lineDataSet.addEntry(new Entry(k, 0));
            }
            lineData.addDataSet(lineDataSet);
        }
        this.lineGeneralChart.setData(lineData);

        return view;
    }


    @Override
    public void onDataReady(Entry entry, Sensor sensor, Coordinate coordinate) {

    }

    @Override
    public void onDataReady(ChartEntry entry) {
        Activity parentActivity = getActivity();
        if(this.lineGeneralChart != null){
            this.lineGeneralChart.post(new Runnable() {
                @Override
                public void run() {
                    LineData lineData;
                    ILineDataSet lineDataSet;
                    updateLineChart(entry, PlotFragmentULP.this.lineGeneralChart);
                }
            });
        }
    }

    private void updateLineChart(ChartEntry entry, LineChart lineChart) {
        LineData lineData = lineChart.getLineData();
        ILineDataSet lineDataSet = lineData.getDataSetByIndex(0);
        lineDataSet.removeFirst();
        lineDataSet.setDrawValues(false);
        lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getX()));
        lineData.notifyDataChanged();

        lineDataSet = lineData.getDataSetByIndex(1);
        lineDataSet.removeFirst();
        lineDataSet.setDrawValues(false);
        lineDataSet.addEntry(new Entry(entry.getIndex(), entry.getY()));
        lineData.notifyDataChanged();

        lineDataSet = lineData.getDataSetByIndex(2);
        lineDataSet.removeFirst();
        lineDataSet.setDrawValues(false);
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

}
