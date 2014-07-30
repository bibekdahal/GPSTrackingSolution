package com.frobi.gpstrackingsolution.app;


import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static com.google.android.gms.maps.CameraUpdateFactory.*;


public class MainActivity extends FragmentActivity implements GPSListener{

    GPSTracker m_gpsTracker;
    private boolean m_isBound;

    private TextView locationLabel;
    private GoogleMap m_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_isBound = false;

        locationLabel = (TextView) findViewById(R.id.locationLabel);
        Button getLocationBtn = (Button) findViewById(R.id.getLocation);

        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                displayCurrentLocation();
            }
        });
        Button disconnectBtn = (Button) findViewById(R.id.disconnect);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!m_isBound) { StartService(); }
                m_gpsTracker.Disconnect();
                StopService();
                locationLabel.setText("Got disconnected....");
            }
        });
        Button connectBtn = (Button) findViewById(R.id.connect);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!m_isBound) StartService();
                //m_gpsTracker.Connect();
                //locationLabel.setText("Got connected....");
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
        Button toggleMapTypeBtn = (Button) findViewById(R.id.toggleMapType);
        toggleMapTypeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (m_map != null)
                    m_map.setMapType((m_map.getMapType() == GoogleMap.MAP_TYPE_NORMAL) ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
            }
        });
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
        locationLabel.setText(msg);

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
        if (!m_isBound) StartService();
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
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
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
            locationLabel.setText("Got connected....");
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

    private boolean IsServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GPSTracker.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
