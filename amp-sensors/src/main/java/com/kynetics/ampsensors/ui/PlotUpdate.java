package com.kynetics.ampsensors.ui;

import com.github.mikephil.charting.data.Entry;
import com.kynetics.ampsensors.device.Coordinate;
import com.kynetics.ampsensors.device.Sensor;


public interface PlotUpdate {
    void onDataReady(Entry entry, Sensor sensor, Coordinate coordinate);
}
