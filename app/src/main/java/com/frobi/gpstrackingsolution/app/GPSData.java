package com.frobi.gpstrackingsolution.app;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GPSData {
    private double m_latitude, m_longitude, m_speed, m_direction;
    private String m_time;

    public GPSData() {}
    public GPSData(double latitude, double longitude, double speed, double direction, long time) {
        m_latitude = latitude;
        m_longitude = longitude;
        m_speed = speed;
        m_direction = direction;
        m_time = ConvertTimeToString(time);
    }

    public double GetLatitude() { return m_latitude; }
    public void SetLatitude(double latitude) { m_latitude=latitude; }
    public double GetLongitude() { return m_longitude; }
    public void SetLongitude(double longitude) { m_longitude=longitude; }
    public double GetSpeed() { return m_speed; }
    public void SetSpeed(double speed) { m_speed=speed; }
    public double GetDirection() { return m_direction; }
    public void SetDirection(double direction) { m_direction=direction; }
    public String GetTime() { return m_time; }
    public void SetTime(String time) { m_time=time; }
    public void SetTime(long time) { m_time=ConvertTimeToString(time); }

    public static String ConvertTimeToString(long time) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(time);
    }


}
