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

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kynetics.ampsensors.R;
import com.kynetics.ampsensors.device.BootType;
import com.kynetics.ampsensors.device.DataType;
import com.kynetics.ampsensors.device.DeviceManager;
import com.kynetics.ampsensors.math.SensorsStreamConsumer;
import com.kynetics.ampsensors.math.StatisticsInfoConsumer;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DeviceManager currentDeviceManager = null;
    private DataType currentDataType = null;
    private BootType bootType = null;
    private CustomAlertDialog customAlertDialog;

    static {
        System.loadLibrary("native-lib");
    }

    private void exit() {
        if (this.currentDeviceManager != null) {
            this.currentDeviceManager.closeDevice();
            this.currentDataType = null;
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.customAlertDialog = new CustomAlertDialog(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                exit();
            }
        });

        this.bootType = BootType.ON_START;

        if (savedInstanceState == null) {
            switchFragment(new NormPlotFragment(), DataType.NORM_DATA, this.bootType);
        }


        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
                        }
                    }
                });

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if ((id == R.id.nav_module && currentDataType == DataType.NORM_DATA) || (id == R.id.nav_raw && currentDataType == DataType.VECTOR_DATA)) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        if (this.currentDeviceManager != null) {
            currentDeviceManager.closeDevice();
            currentDeviceManager = null;
            currentDataType = null;
        }
        if (id == R.id.nav_module) {
            switchFragment(new NormPlotFragment(), DataType.NORM_DATA, BootType.ON_START);
        } else if (id == R.id.nav_raw) {
            switchFragment(new VectorPlotFragment(), DataType.VECTOR_DATA, BootType.ON_START);
        } else if (id == R.id.nav_exit) {
            this.exit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {

        if (this.currentDeviceManager != null) {
            this.bootType = BootType.ON_RESUME;
            currentDeviceManager.openDevice(currentDataType, this.bootType);
        }
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.currentDeviceManager != null) {
            this.currentDeviceManager.closeDevice();
        }
    }

    private void switchFragment(PlotFragment fragment, DataType dataType, BootType bootType) {
        SensorsStreamConsumer asc = new SensorsStreamConsumer(fragment);
        StatisticsInfoConsumer ssc = new StatisticsInfoConsumer(this.customAlertDialog);
        this.currentDeviceManager = new DeviceManager(asc, asc, ssc);
        this.currentDeviceManager.openDevice(dataType, bootType);
        this.currentDataType = dataType;
        this.bootType = BootType.ON_RESUME;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void showDialog(View view) {
        this.customAlertDialog.setDrawable();
        this.customAlertDialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                showDialog(getWindow().getDecorView());
                return true;
            default:
                return true;
        }
    }
}