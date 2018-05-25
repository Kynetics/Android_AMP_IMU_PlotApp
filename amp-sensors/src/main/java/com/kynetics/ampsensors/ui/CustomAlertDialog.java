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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.kynetics.ampsensors.R;

import java.util.ArrayList;
import java.util.List;


public class CustomAlertDialog extends AlertDialog {


    private TextView dataSentName;
    private TextView dataSentStatus;
    private TextView idleName;
    private TextView idleStatus;
    private TextView tmpName;
    private TextView tmpStatus;
    private TextView statName;
    private TextView statStatus;
    private TextView imuName;
    private TextView imuStatus;
    private HorizontalBarChart horizontalBarChart;
    private PieChart pieChart;

    protected CustomAlertDialog(Context context) {
        super(context);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();


        View inflatedView = inflater.inflate(R.layout.custom_dialog_alert, null);
        builder.setView(inflatedView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        dataSentName = inflatedView.findViewById(R.id.name_data_sent);
        dataSentStatus = inflatedView.findViewById(R.id.status_data_sent);
        idleName = inflatedView.findViewById(R.id.name_idle);
        idleStatus = inflatedView.findViewById(R.id.status_idle);
        tmpName = inflatedView.findViewById(R.id.name_tmp);
        tmpStatus = inflatedView.findViewById(R.id.status_tmp);
        statName = inflatedView.findViewById(R.id.name_stat);
        statStatus = inflatedView.findViewById(R.id.status_stat);
        imuName = inflatedView.findViewById(R.id.name_imu);
        imuStatus = inflatedView.findViewById(R.id.status_imu);
        horizontalBarChart = inflatedView.findViewById(R.id.horizontal_chart);
        pieChart = inflatedView.findViewById(R.id.pie_chart);
        setHorizontalBarChart(horizontalBarChart);
        setPieChart(pieChart);
//        builder.create();
        builder.create().show();
    }


    public HorizontalBarChart getHorizontalBarChart() {
        return horizontalBarChart;
    }

    public PieChart getPieChart() {
        return pieChart;
    }

    public void setHorizontalBarChart(HorizontalBarChart horizontalBarChart) {
        ArrayList<BarEntry> entries = new ArrayList();
        entries.add(new BarEntry(10, 1));
        BarDataSet dataset = new BarDataSet(entries, "queue's messages");
        dataset.setColor(Color.GREEN);
        BarData data = new BarData(dataset);
        horizontalBarChart.setData(data);
        horizontalBarChart.getXAxis().setDrawGridLines(false);
        horizontalBarChart.getAxisLeft().setDrawGridLines(false);
        horizontalBarChart.getAxisRight().setDrawGridLines(false);
        horizontalBarChart.getDescription().setEnabled(false);
        horizontalBarChart.getXAxis().setEnabled(false);
        horizontalBarChart.getAxisLeft().setEnabled(false);
        horizontalBarChart.getAxisRight().setEnabled(false);

        horizontalBarChart.invalidate();

    }

    public void setPieChart(PieChart pieChart) {
        List<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(18.5f, "Green"));
        entries.add(new PieEntry(26.7f, "Yellow"));
        entries.add(new PieEntry(24.0f, "Red"));
        entries.add(new PieEntry(30.8f, "Blue"));

        PieDataSet set = new PieDataSet(entries, "Colors");
        set.setColors(ColorTemplate.VORDIPLOM_COLORS);
        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.setEntryLabelTextSize(10);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);

        pieChart.invalidate();

    }
}
