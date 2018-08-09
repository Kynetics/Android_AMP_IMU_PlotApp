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

import android.graphics.Color;
import com.github.mikephil.charting.data.LineDataSet;

public enum Coordinate {

    X_OR_NORM("x"), Y("y"), Z("z"), ROLL("roll"), PITCH("pitch"), YAW("yaw");

    private final String label;

    Coordinate(String label) {
        this.label = label;
    }

    private void _commonCFG(LineDataSet lineDataSet) {
        lineDataSet.setValueTextSize(7);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
    }

    public void configureDataSet(LineDataSet lineDataSet) {

        switch (this) {
            case X_OR_NORM:
                configureDataSetX(lineDataSet);
                break;

            case Y:
                configureDataSetY(lineDataSet);
                break;

            case Z:
                configureDataSetZ(lineDataSet);
                break;
        }
    }

    private void configureDataSetX(LineDataSet lineDataSet) {
        lineDataSet.setColor(Color.BLUE);
        _commonCFG(lineDataSet);
    }

    private void configureDataSetY(LineDataSet lineDataSet) {
        lineDataSet.setColor(Color.RED);
        _commonCFG(lineDataSet);
    }

    private void configureDataSetZ(LineDataSet lineDataSet) {
        lineDataSet.setColor(Color.GREEN);
        _commonCFG(lineDataSet);
    }

    public String getLabel() {
        return label;
    }
}
