package com.frobi.gpstrackingsolution.app;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class MainActivity extends FragmentActivity implements GPSListener{

    GPSTracker m_gpsTracker;
    private boolean m_isBound;

    private TextView m_locationLabel;
    private GoogleMap m_map;

    private final String START_TEXT = "Start Service";
    private final String STOP_TEXT = "Stop Service";
    private void UpdateUI()
    {
        Button connectBtn = (Button) findViewById(R.id.connect);
        if (GPSTracker.IsRunning()) connectBtn.setText(STOP_TEXT);
        else connectBtn.setText(START_TEXT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_locationLabel = (TextView) findViewById(R.id.locationLabel);
        m_isBound = false;
        if (GPSTracker.IsRunning()) StartService();
        UpdateUI();

        final Button connectBtn = (Button) findViewById(R.id.connect);
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

        Button takePhotoBtn = (Button) findViewById(R.id.takePhoto);
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                PictureManager.TakePicture(MainActivity.this);
            }
        });

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment!=null)
        m_map = mapFragment.getMap();
    }

    private Marker m_marker = null;
    public void displayCurrentLocation(){
        if (!m_isBound) { StartService(); return; }
        String msg;
        if (m_gpsTracker.Update())
        {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            msg =   "Current Location: " +
                    Double.toString(m_gpsTracker.GetLastLatitude()) + ", " +
                    Double.toString(m_gpsTracker.GetLastLongitude()) + " at " + dateFormat.format(m_gpsTracker.GetLastTime());
        }
        else
            msg = "FAILED";

        // Display the current location in the UI
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_toggleMapType:
                if (m_map != null)
                    m_map.setMapType((m_map.getMapType() == GoogleMap.MAP_TYPE_NORMAL) ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void LocationChanged() {
        displayCurrentLocation();
    }


    private ServiceConnection m_gpsConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            m_gpsTracker = ((GPSTracker.GPSBinder)service).getService();
            m_gpsTracker.Initialize(MainActivity.this, 5);
            m_gpsTracker.Connect();
            m_locationLabel.setText("Got connected....");
            UpdateUI();
        }
        public void onServiceDisconnected(ComponentName arg0) {
            m_gpsTracker = null;
        }
    };

    private void UnbindService(){
        if (m_isBound) {
            unbindService(m_gpsConnection);
            m_isBound = false;
        }
    }

    private void StartService(){
        Intent objIntent = new Intent(this,  GPSTracker.class);
        startService(objIntent);
        bindService(objIntent, m_gpsConnection, BIND_AUTO_CREATE);
        m_isBound = true;
    }

    private void StopService(){
        UnbindService();
        stopService(new Intent(this, GPSTracker.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UnbindService();
    }

    public void PanicButton_Click(View view) {
        Toast.makeText(this, "PANICKED", Toast.LENGTH_SHORT).show();
    }
}
