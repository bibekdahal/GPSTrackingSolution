package com.frobi.gpstrackingsolution.app;


import android.content.ComponentName;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class MainActivity extends FragmentActivity implements GPSListener{

    GPSTracker m_gpsTracker;
    private boolean m_isBound;

    private TextView locationLabel;

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
                if (!m_isBound) { StartService(); return; }
                m_gpsTracker.Connect();
                locationLabel.setText("Got connected....");
            }
        });
    }

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
        if (!m_isBound)  return;
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
}
