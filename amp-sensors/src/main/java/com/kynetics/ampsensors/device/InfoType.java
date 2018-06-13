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

public enum InfoType {
    NAME(0, 20), STATUS(20, 10), PERCENTAGE(30, 5), QUEUE(175, 11);

    private final int offset;
    private final int dimension;

    InfoType(int o, int d) {
        this.offset = o;
        this.dimension = d;
    }

    public int getDimension() {
        return dimension;
    }

    public int getOffset() {
        return offset;
    }
}
