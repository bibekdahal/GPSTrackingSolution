package com.frobi.gpstrackingsolution.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.app.Service;
import android.content.Intent;

public class GPSTracker extends Service implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static boolean m_isRunning = false;
    public static boolean IsRunning() { return m_isRunning; }

    private Context m_context;
    private double m_lastLatitude;
    private double m_lastLongitude;
    private double m_lastSpeed;
    private double m_lastDirection;
    private long m_lastTime;
    private GPSListener m_listener;
    private boolean m_started = false;

    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int FAST_CEILING_IN_SECONDS = 1;
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    LocationClient m_locationClient;
    private LocationRequest m_locationRequest;


    private void ShowNotification(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent1 = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("GPS TRACKING")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(intent1)
                .build();

        startForeground(1234, notification);

    }

    public void Initialize(GPSListener listener, int updateIntervalInSeconds){
        if (m_started) {
            m_listener = listener;
            return;
        }
        m_locationRequest = LocationRequest.create();

        long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * updateIntervalInSeconds;
        m_locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        m_locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        m_locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        m_locationClient = new LocationClient(this, this, this);
        m_context = this;
        m_listener = listener;
        m_started = true;

        m_isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ShowNotification();
        return START_STICKY;
    }

    public void Connect(){
        m_locationClient.connect();
        m_isRunning = true;
    }
    public void Disconnect(){
        if (m_locationClient.isConnected())
            m_locationClient.removeLocationUpdates(this);
        m_locationClient.disconnect();
        m_isRunning = false;
    }

    public double GetLastLatitude(){
        return m_lastLatitude;
    }
    public double GetLastLongitude(){
        return m_lastLongitude;
    }
    public double GetLastSpeed(){
        return m_lastSpeed;
    }
    public double GetLastDirection(){
        return m_lastDirection;
    }
    public long GetLastTime(){
        return m_lastTime;
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        m_locationClient.requestLocationUpdates(m_locationRequest, this);
        //Toast.makeText(m_context, "Connected", Toast.LENGTH_SHORT).show();
        m_isRunning = true;
    }
    @Override
    public void onDisconnected() {
        m_locationClient.removeLocationUpdates(this);
        Toast.makeText(m_context, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        m_isRunning = false;
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(m_context, "Connection Failure : " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
    }

    public void StoreData(){
        GPSHistory history = new GPSHistory(this);
        GPSData newData = new GPSData(m_lastLatitude, m_lastLongitude, m_lastSpeed, m_lastDirection, m_lastTime);
        history.AddData(newData);
    }

    public boolean Update() {
        if (!m_locationClient.isConnected()) return false;
        if (!servicesConnected()) return false;
        Location currentLocation = m_locationClient.getLastLocation();
        if (currentLocation!=null){
            m_lastLatitude = currentLocation.getLatitude();
            m_lastLongitude = currentLocation.getLongitude();
            m_lastTime = currentLocation.getTime();
            m_lastSpeed = currentLocation.getSpeed();
            m_lastDirection = currentLocation.getBearing();

            StoreData();
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        Update();
        if (m_listener!=null) m_listener.LocationChanged();
    }

    private final IBinder m_binder = new GPSBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    public class GPSBinder extends Binder {
        GPSTracker getService() {
            return GPSTracker.this;
        }
    }

    /*public static class ErrorDialogFragment extends DialogFragment {
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
    }*/
    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(m_context);
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            /*Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000);
            if (errorDialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }*/
            return false;
        }
    }


}
