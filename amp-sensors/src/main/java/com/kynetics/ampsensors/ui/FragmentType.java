/*
 * Copyright (c) Kynetics LLC. Author: Marta Todeschini
 *
 *               This program is free software: you can redistribute it and/or modify
 *               it under the terms of the GNU General Public License as published by
 *               the Free Software Foundation, either version 3 of the License, or
 *               (at your option) any later version.
 *
 *               This program is distributed in the hope that it will be useful,
 *               but WITHOUT ANY WARRANTY; without even the implied warranty of
 *               MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *               GNU General Public License for more details.
 *
 *               You should have received a copy of the GNU General Public License
 *               along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.kynetics.ampsensors.ui;

import android.hardware.Sensor;
import android.hardware.SensorManager;

public enum FragmentType {
    ACC(android.hardware.Sensor.TYPE_ACCELEROMETER), MAG(Sensor.TYPE_MAGNETIC_FIELD), GYR(Sensor.TYPE_GYROSCOPE), NORM(-1), VECTOR(-2);

    FragmentType(int type) {
        this.type = type;
    }

    private int type;

    public Sensor getSensor(SensorManager sensorManager) {
        return type <= -1 ? null : sensorManager.getDefaultSensor(type);
    }


}
