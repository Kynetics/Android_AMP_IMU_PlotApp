package com.kynetics.ampsensors.ui;


import com.kynetics.ampsensors.device.DataType;


public class MagnetometerPlotFragmentULP extends PlotFragmentULP {
    @Override
    protected DataType getDataType() {
        return DataType.VECTOR_DATA;
    }

    @Override
    public FragmentType getFragmentType() {
        return FragmentType.MAG;
    }

}
