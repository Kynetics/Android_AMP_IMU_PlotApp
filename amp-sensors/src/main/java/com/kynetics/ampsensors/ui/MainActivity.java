package com.kynetics.ampsensors.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.kynetics.ampsensors.R;
import com.kynetics.ampsensors.device.DataType;
import com.kynetics.ampsensors.device.DeviceManager;
import com.kynetics.ampsensors.math.SensorsStreamConsumer;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DeviceManager currentDeviceManager = null;
    private DataType currentDataType = null;


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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (this.currentDeviceManager != null) {
            currentDeviceManager.closeDevice();
        }
        if (id == R.id.nav_module) {
            switchFragment(new NormPlotFragment(), DataType.NORM_DATA);
        } else if (id == R.id.nav_raw) {
            switchFragment(new VectorPlotFragment(), DataType.VECTOR_DATA);
        } else if (id == R.id.nav_exit) {
            this.exit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.currentDeviceManager != null) {
            currentDeviceManager.openDevice(currentDataType);
        }
    }

    @Override
    protected void onStop() {
        if (this.currentDeviceManager != null) {
            this.currentDeviceManager.closeDevice();
        }
        super.onStop();
    }

    private void switchFragment(PlotFragment fragment, DataType dataType) {
        SensorsStreamConsumer asc = new SensorsStreamConsumer(fragment);
        this.currentDeviceManager = new DeviceManager(asc, asc);
        this.currentDeviceManager.openDevice(dataType);
        this.currentDataType = dataType;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }


}
