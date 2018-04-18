package com.kynetics.ampsensors.device;

import android.graphics.Color;
import com.github.mikephil.charting.components.XAxis;
import com.kynetics.ampsensors.ui.PlotFragment;

public enum Sensor {


    ACC("accelerometer"), MAG("magnetometer"), GYR("gyroscope");
    private final String label;


    Sensor(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void configureAxis(XAxis xAxis) {


        switch (this) {
            case ACC:
                configureAccAxis(xAxis);
                break;

            case MAG:
                configureAccAxis(xAxis);
                break;

            case GYR:
                configureAccAxis(xAxis);
                break;


        }
    }

    private void configureAccAxis(XAxis xAxis) {
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(PlotFragment.PLOT_POINTS);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    }
}
