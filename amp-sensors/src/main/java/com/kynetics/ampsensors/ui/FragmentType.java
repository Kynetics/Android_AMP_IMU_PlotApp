package com.kynetics.ampsensors.ui;

import android.hardware.Sensor;
import android.hardware.SensorManager;

public enum FragmentType {
    ACC(android.hardware.Sensor.TYPE_ACCELEROMETER), MAG(Sensor.TYPE_MAGNETIC_FIELD), GYR(Sensor.TYPE_GYROSCOPE), NORM(-1), VECTOR(-1);

    FragmentType(int type) {
        this.type = type;
    }

    private int type;

    public Sensor getSensor(SensorManager sensorManager) {
        return type == -1 ? null : sensorManager.getDefaultSensor(type);
    }


}
