package com.frobi.gpstrackingsolution.app;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Picture;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class MainActivity extends FragmentActivity implements GPSListener{
    PopupMenu m_imageMenu;

    GPSTracker m_gpsTracker;
    private boolean m_isBound;

    private TextView m_locationLabel;
    private GoogleMap m_map;

    private final String START_TEXT = "Start Service";
    private final String STOP_TEXT = "Stop Service";
    private void UpdateUI()
    {
        Button connectBtn = (Button) findViewById(R.id.serviceStartStop);
        ImageButton takePhotoBtn = (ImageButton) findViewById(R.id.takePhoto);
        if (GPSTracker.IsRunning()) {
            connectBtn.setText(STOP_TEXT);
            takePhotoBtn.setEnabled(true);
        }
        else {
            connectBtn.setText(START_TEXT);
            takePhotoBtn.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_locationLabel = (TextView) findViewById(R.id.locationLabel);
        m_isBound = false;
        if (GPSTracker.IsRunning()) StartService();
        UpdateUI();

        final Button connectBtn = (Button) findViewById(R.id.serviceStartStop);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (connectBtn.getText()==START_TEXT) {
                    if (!m_isBound) StartService();
                } else {
                    if (!m_isBound) StartService();
                    m_gpsTracker.Disconnect();
                    StopService();
                    //m_locationLabel.setText("Got disconnected....");
                }
                UpdateUI();
            }
        });

        ImageButton takePhotoBtn = (ImageButton) findViewById(R.id.takePhoto);
        m_imageMenu = new PopupMenu(this, takePhotoBtn);
        m_imageMenu.getMenu().add("Take Photo");
        m_imageMenu.getMenu().add("Select Existing Photo");
        m_imageMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle()==("Take Photo"))
                {
                    PictureManager.TakePicture(MainActivity.this);
                    return true;
                }
                PictureManager.SelectImage(MainActivity.this);
                return true;
            }
        });
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                m_imageMenu.show();
                //PictureManager.TakePicture(MainActivity.this);
            }
        });

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) fragment;
        m_map = mapFragment.getMap();
    }

    private Marker m_marker = null;
    public void DisplayCurrentLocation(){
        if (!m_isBound) { StartService(); return; }
        if (m_panicOnNextUpdate) {
            GPSData gspdata = new GPSData(m_gpsTracker.GetLastLatitude(), m_gpsTracker.GetLastLongitude(), m_gpsTracker.GetLastSpeed(),
                    m_gpsTracker.GetLastDirection(), m_gpsTracker.GetLastTime());
            SMSManager.Panic(this, gspdata);
            m_panicOnNextUpdate = false;
        }

        String msg;
        if (m_gpsTracker.Update())
        {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            msg =   "Current Location: " +
                    Double.toString(m_gpsTracker.GetLastLatitude()) + ", " +
                    Double.toString(m_gpsTracker.GetLastLongitude()) + " at " + dateFormat.format(m_gpsTracker.GetLastTime())
                    + "\nSpeed: " + Double.toString(m_gpsTracker.GetLastSpeed())
                    + " Dir: " + Double.toString(m_gpsTracker.GetLastDirection());
        }
        else
            msg = "FAILED";

        m_locationLabel.setText(msg);

        if (m_map==null) return;
        LatLng latlng = new LatLng(m_gpsTracker.GetLastLatitude(), m_gpsTracker.GetLastLongitude());
        m_map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));
        if (m_marker!=null) m_marker.remove();
        m_marker = m_map.addMarker(new MarkerOptions().position(latlng).title("Current Position"));
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        UpdateUI();
        super.onResume();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        if (!m_isBound)  {
            super.onStop();
            return;
        }
        UnbindService();
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggleMapType:
                if (m_map != null)
                    m_map.setMapType((m_map.getMapType() == GoogleMap.MAP_TYPE_NORMAL) ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.action_logOut:
                LogOut();
                return true;
            case R.id.action_showHistory:
                HistoryActivity.SetTracker(m_gpsTracker);
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                SettingsActivity.SetMainActivity(this);
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void LocationChanged() {
        DisplayCurrentLocation();
    }


    private ServiceConnection m_gpsConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            m_gpsTracker = ((GPSTracker.GPSBinder)service).getService();
            m_gpsTracker.Initialize(MainActivity.this, SettingsActivity.GetIntSetting("interval", 5, MainActivity.this));
            m_gpsTracker.Connect();
            m_locationLabel.setText("Got connected....");
            UpdateUI();
        }
        public void onServiceDisconnected(ComponentName arg0) {
            m_gpsTracker = null;
        }
    };

    private class DoRestart extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (m_isBound || GPSTracker.IsRunning()) ;
            StartService();
            return null;
        }
    }


    private void UnbindService(){
        if (m_isBound) {
            unbindService(m_gpsConnection);
            m_isBound = false;
        }
    }

    public void RestartService() {
        if (!GPSTracker.IsRunning()) return;
        m_gpsTracker.Disconnect();
        StopService();
        new DoRestart().execute();
    }

    private void StartService(){
        Intent objIntent = new Intent(this,  GPSTracker.class);
        startService(objIntent);
        bindService(objIntent, m_gpsConnection, BIND_AUTO_CREATE);
        m_isBound = true;
    }

    private void StopService(){
        UnbindService();
        stopService(new Intent(MainActivity.this, GPSTracker.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UnbindService();
    }

    private boolean m_panicOnNextUpdate=false;
    public void PanicButton_Click(View view) {
        if (!GPSTracker.IsRunning() || !m_isBound) {
            m_panicOnNextUpdate = true;
            StartService();
            return;
        }
        m_gpsTracker.Update();
        GPSData gspdata = new GPSData(m_gpsTracker.GetLastLatitude(), m_gpsTracker.GetLastLongitude(), m_gpsTracker.GetLastSpeed(),
                m_gpsTracker.GetLastDirection(), m_gpsTracker.GetLastTime());
        SMSManager.Panic(this, gspdata);
    }

    public void LogOut() {
        SharedPreferences settings = getSharedPreferences(RegisterActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Email", "");
        editor.putString("Password", "");
        editor.commit();
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PictureManager.Result(this, requestCode, resultCode, data);
    }
}
