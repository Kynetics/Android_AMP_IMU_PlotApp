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
