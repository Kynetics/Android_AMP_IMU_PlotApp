package com.kynetics.ampsensors.ui;

import com.github.mikephil.charting.data.Entry;
import com.kynetics.ampsensors.device.Coordinate;
import com.kynetics.ampsensors.device.DataType;
import com.kynetics.ampsensors.device.Sensor;

public class MagnetometerPlotFragment extends PlotFragment {
    @Override
    protected DataType getDataType() {
        return DataType.VECTOR_DATA;
    }

    @Override
    public FragmentType getFragmentType() {
        return FragmentType.MAG;
    }

    @Override
    public void onDataReady(Entry entry, Sensor sensor, Coordinate coordinate) {

    }
}
