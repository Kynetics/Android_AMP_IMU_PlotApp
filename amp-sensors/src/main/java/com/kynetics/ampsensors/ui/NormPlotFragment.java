package com.kynetics.ampsensors.ui;

import com.kynetics.ampsensors.device.DataType;

public class NormPlotFragment extends PlotFragment {
    @Override
    protected DataType getDataType() {
        return DataType.NORM_DATA;
    }
}
