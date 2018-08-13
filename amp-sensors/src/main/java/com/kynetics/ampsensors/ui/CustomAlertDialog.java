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

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.kynetics.ampsensors.R;
import com.kynetics.ampsensors.device.BoardType;

import java.util.ArrayList;
import java.util.List;

public class CustomAlertDialog extends Dialog implements AlertDialogUpdate, android.view.View.OnClickListener{


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
    private Activity activity;
    private boolean drawable;
    LinearLayout linlaHeaderProgress;
    private final String PIE_CHART_LABEL = "% TIME";
    private final String HORIZONTAL_BAR_CHART_LABEL = "queue's messages update";
    private BoardType boardType;

    protected CustomAlertDialog(Activity activity, BoardType boardType) {
        super(activity);
        this.activity = activity;
        this.boardType = boardType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog_alert);
        dataSentName = findViewById(R.id.name_data_sent);
        dataSentStatus = findViewById(R.id.status_data_sent);
        idleName = findViewById(R.id.name_idle);
        idleStatus = findViewById(R.id.status_idle);
        tmpName = findViewById(R.id.name_tmp);
        tmpStatus = findViewById(R.id.status_tmp);
        statName = findViewById(R.id.name_stat);
        statStatus = findViewById(R.id.status_stat);
        imuName = findViewById(R.id.name_imu);
        imuStatus = findViewById(R.id.status_imu);
        if(boardType.equals(BoardType.ULP)) {
            horizontalBarChart = findViewById(R.id.horizontal_chart);
            horizontalBarChart.setVisibility(View.GONE);
        }
        pieChart = findViewById(R.id.pie_chart);
        pieChart.setUsePercentValues(true);
        pieChart.setVisibility(View.GONE);
        Button okBtm = findViewById(R.id.btn_ok);
        okBtm.setOnClickListener(this);
        drawable = true;

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

    @Override
    public void onDataReady(String[] stringArray, List<PieEntry> retListPieChart, int[] queue) {

         this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _onDataReady(stringArray, retListPieChart, queue);
                }
            });
    }

    private void _onDataReady(String[] stringArray, List<PieEntry> retListPieChart, int[] queue) {
        if (drawable) {
            linlaHeaderProgress.setVisibility(View.GONE);
            pieChart.setVisibility(View.VISIBLE);
            if(boardType.equals(BoardType.D)) {
                horizontalBarChart.setVisibility(View.VISIBLE);
            }
            setTaskNameAndStatus(stringArray);


            if(boardType.equals(BoardType.D)) {
                ArrayList<BarEntry> entries = new ArrayList<>();
                entries.add(new BarEntry(3, (int) queue[0]));
                setHorizontalBarChart(entries, queue[1]);
            }

            PieDataSet set = new PieDataSet(retListPieChart, PIE_CHART_LABEL);
            setPieChart(set);
        }
    }

    private void setTaskNameAndStatus(String[] stringArray) {
        dataSentName.setText(stringArray[0]);
        dataSentStatus.setText(stringArray[1]);
        idleName.setText(stringArray[2]);
        idleStatus.setText(stringArray[3]);
        tmpName.setText(stringArray[4]);
        tmpStatus.setText(stringArray[5]);
        statName.setText(stringArray[6]);
        statStatus.setText(stringArray[7]);
        imuName.setText(stringArray[8]);
        imuStatus.setText(stringArray[9]);
    }

    private void setPieChart(PieDataSet set) {
        set.setColors(ColorTemplate.VORDIPLOM_COLORS);
        set.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        set.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        set.setValueTextSize(10);
        PieData data = new PieData(set);
        data.setValueTextSize(10);
        data.setValueFormatter(new PercentFormatter());
        pieChart.invalidate();
        pieChart.clear();
        pieChart.setData(data);
        pieChart.setDrawEntryLabels(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);
        pieChart.setTouchEnabled(false);
        pieChart.isUsePercentValuesEnabled();
        pieChart.setEntryLabelTextSize(10);
        pieChart.setUsePercentValues(true);
    }

    private void setHorizontalBarChart(ArrayList<BarEntry> entries, int elementsNumber) {

        horizontalBarChart.setData(new BarData(new BarDataSet(entries, HORIZONTAL_BAR_CHART_LABEL)));
        horizontalBarChart.getXAxis().setDrawGridLines(true);
        horizontalBarChart.getAxisLeft().setValueFormatter(new IntegerFormatter());
        horizontalBarChart.getAxisRight().setValueFormatter(new IntegerFormatter());
        horizontalBarChart.getAxisRight().setDrawGridLines(false);
        horizontalBarChart.getDescription().setEnabled(false);
        horizontalBarChart.getXAxis().setEnabled(false);
        horizontalBarChart.getAxisLeft().setEnabled(false);
        horizontalBarChart.setVisibleXRange(0, 2);
        horizontalBarChart.setVisibleYRange(0, elementsNumber, YAxis.AxisDependency.LEFT);
        horizontalBarChart.getAxisLeft().setAxisMinimum(0);
        horizontalBarChart.getAxisRight().setAxisMinimum(0);
        horizontalBarChart.getAxisLeft().setAxisMaximum(10);
        horizontalBarChart.getAxisRight().setAxisMaximum(10);
        horizontalBarChart.setClickable(false);
        horizontalBarChart.setTouchEnabled(false);
        horizontalBarChart.invalidate();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                dismiss();
                drawable = false;
                break;
            default:
                break;
        }
        dismiss();
    }

    public void setDrawable(){
        this.drawable = true;
    }
}
