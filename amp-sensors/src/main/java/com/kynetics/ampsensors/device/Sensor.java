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
import com.github.mikephil.charting.components.XAxis;
import com.kynetics.ampsensors.ui.PlotFragment;

public enum Sensor {
    ACC("accelerometer", "(g)",  "(m/s^2)"), MAG("magnetometer", "(µT)",  "(µT)"), GYR("gyroscope", "(dps)" , "(rad/s)");
    private final String label;
    private final String d_unit;
    private final String ulp_unit;

    Sensor(String label, String d_unit, String ulp_unit) {
        this.label = label;
        this.d_unit = d_unit;
        this.ulp_unit = ulp_unit;

    }

    public String getLabel() {
        return label;
    }

    public String getDUnit(){
        return d_unit;
    }

    public String getUlpUnit(){
        return ulp_unit;
    }

    public void configureAxis(XAxis xAxis) {
        switch (this) {
            case ACC:
                configureAccAxis(xAxis);
                break;

            case MAG:
                configureAccAxis(xAxis);
                break;

            case GYR:
                configureAccAxis(xAxis);
                break;
        }
    }

    private void configureAccAxis(XAxis xAxis) {
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(PlotFragment.PLOT_POINTS);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    }
}
