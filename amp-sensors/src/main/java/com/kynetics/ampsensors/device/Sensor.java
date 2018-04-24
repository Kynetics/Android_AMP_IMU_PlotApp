package com.kynetics.ampsensors.device;

import android.graphics.Color;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.kynetics.ampsensors.ui.PlotFragment;

public enum Sensor {
    ACC("accelerometer", "(g)"), MAG("magnetometer", "(µT)"), GYR("gyroscope", "(dps)");
    private final String label;
    private final String unit;

    Sensor(String label, String unit) {
        this.label = label;
        this.unit = unit;
    }

    public String getLabel() {
        return label;
    }

    public String getUnit(){
        return unit;
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
