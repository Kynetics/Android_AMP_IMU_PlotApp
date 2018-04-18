package com.kynetics.ampsensors.device;

import android.graphics.Color;

import com.github.mikephil.charting.data.LineDataSet;

public enum Coordinate {

    X_OR_NORM("x"), Y("y"), Z("z");

    private final String label;

    Coordinate(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void configureDataSet(LineDataSet lineDataSet) {

        switch (this) {
            case X_OR_NORM:
                configureDataSetX(lineDataSet);
                break;

            case Y:
                configureDataSetY(lineDataSet);
                break;

            case Z:
                configureDataSetZ(lineDataSet);
                break;
        }
    }

    private void configureDataSetX(LineDataSet lineDataSet) {
        lineDataSet.setColor(Color.BLUE);
        _commonCFG(lineDataSet);
    }

    private void configureDataSetY(LineDataSet lineDataSet) {
        lineDataSet.setColor(Color.RED);
        _commonCFG(lineDataSet);
    }

    private void configureDataSetZ(LineDataSet lineDataSet) {
        lineDataSet.setColor(Color.GREEN);
        _commonCFG(lineDataSet);
    }

    private void _commonCFG(LineDataSet lineDataSet) {
        lineDataSet.setValueTextSize(7);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
    }
}
