package com.kynetics.ampsensors.ui;

import com.kynetics.ampsensors.device.DataType;

public class VectorPlotFragment extends PlotFragment {
    @Override
    protected DataType getDataType() {
        return DataType.VECTOR_DATA;
    }
}
