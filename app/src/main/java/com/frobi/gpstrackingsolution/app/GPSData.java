package com.frobi.gpstrackingsolution.app;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GPSData {
    private double m_latitude, m_longitude, m_speed, m_direction;
    private String m_time;
    private int m_localid;

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
    public int GetId() { return m_localid; }
    public void SetId(int id) { m_localid=id; }

    public static String ConvertTimeToString(long time) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(time);
    }

    public String GetDataString(int j) {
        switch (j)
        {
            case 0: return Double.toString(m_latitude);
            case 1: return Double.toString(m_longitude);
            case 2: return Double.toString(m_speed);
            case 3: return Double.toString(m_direction);
            case 4: return m_time;
            default: return "";
        }
    }

    public boolean Equals(GPSData other){
        return (m_latitude==other.m_latitude &&
                m_longitude==other.m_longitude &&
                m_speed==other.m_speed &&
                m_direction==other.m_direction);
        /*double min = 0.000005;
        return (Math.abs(m_latitude-other.m_latitude)<min &&
                Math.abs(m_longitude-other.m_longitude)<min &&
                Math.abs(m_speed-other.m_speed)<min &&
                Math.abs(m_direction-other.m_direction)<min);// &&
                //m_time.equals(other.m_time));*/
    }
}
