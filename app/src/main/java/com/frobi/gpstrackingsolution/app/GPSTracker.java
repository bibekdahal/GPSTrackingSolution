package com.frobi.gpstrackingsolution.app;


import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class GPSTracker extends FragmentActivity implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private double m_lastLatitude;
    private double m_lastLongitude;
    private long m_lastTime;
    private GPSListener m_listener;

    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int FAST_CEILING_IN_SECONDS = 1;
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    LocationClient m_locationClient;
    private LocationRequest m_locationRequest;

    public void Initialize(GPSListener listener, int updateIntervalInSeconds){
        m_locationRequest = LocationRequest.create();

        long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * updateIntervalInSeconds;
        m_locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        m_locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        m_locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        m_locationClient = new LocationClient(this, this, this);

        m_listener = listener;
    }

    public void Connect(){
        m_locationClient.connect();
    }
    public void Disconnect(){
        m_locationClient.disconnect();
    }

    public double GetLastLatitude(){
        return m_lastLatitude;
    }
    public double GetLastLongitude(){
        return m_lastLongitude;
    }
    public long GetLastTime(){
        return m_lastTime;
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        m_locationClient.requestLocationUpdates(m_locationRequest, this);
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDisconnected() {
        m_locationClient.removeLocationUpdates(this);
       Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failure : " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
    }

    public boolean Update() {
        if (!servicesConnected()) return false;
        Location currentLocation = m_locationClient.getLastLocation();
        if (currentLocation!=null){
            m_lastLatitude = currentLocation.getLatitude();
            m_lastLongitude = currentLocation.getLongitude();
            m_lastTime = currentLocation.getTime();
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        Update();
        m_listener.LocationChanged();
    }


    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000);
            if (errorDialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }
            return false;
        }
    }


}
