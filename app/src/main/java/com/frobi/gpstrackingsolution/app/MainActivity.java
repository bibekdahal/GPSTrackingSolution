package com.frobi.gpstrackingsolution.app;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class MainActivity extends FragmentActivity implements GPSListener{

    GPSTracker m_gpsTracker = new GPSTracker();
    private TextView locationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_gpsTracker.Initialize(this, 5);

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
                m_gpsTracker.Disconnect();
                locationLabel.setText("Got disconnected....");
            }
        });
        Button connectBtn = (Button) findViewById(R.id.connect);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                m_gpsTracker.Connect();
                locationLabel.setText("Got connected....");
            }
        });
    }

    public void displayCurrentLocation(){
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

    }
    @Override
    public void onResume() {

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
        m_gpsTracker.Connect();
        locationLabel.setText("Got connected....");
    }
    @Override
    protected void onStop() {
        m_gpsTracker.Disconnect();
        super.onStop();
        locationLabel.setText("Got disconnected....");
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
}
