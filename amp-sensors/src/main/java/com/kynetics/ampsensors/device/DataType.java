package com.kynetics.ampsensors.device;

public enum DataType {

    VECTOR_DATA(3), NORM_DATA(1);

    private final int dimension;
    private static final int SIZE_OF_FLOAT = 4;

    DataType(int d) {
        this.dimension = d;
    }

    public int getBufferSize() {
        return this.getSampleCount() * SIZE_OF_FLOAT;
    }

    public int getDimension() {
        return dimension;
    }

    public int getSampleCount() {
        return this.dimension * Sensor.values().length;
    }
}
