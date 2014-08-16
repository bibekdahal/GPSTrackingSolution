package com.frobi.gpstrackingsolution.app;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GPSHistory extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "GPSTrackingSolutionDB";
    private static final String TABLE_NAME = "GPSHistory";

    private static final String KEY_ID = "id";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LONG= "longitude";
    private static final String KEY_SPEED= "speed";
    private static final String KEY_DIR= "direction";
    private static final String KEY_TIME= "time";
    private static final String KEY_UPLOADED = "uploaded";

    private final Context m_context;
    public GPSHistory(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        m_context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_LAT + " DOUBLE,"
                + KEY_LONG + " DOUBLE,"
                + KEY_SPEED + " DOUBLE,"
                + KEY_DIR + " DOUBLE,"
                + KEY_TIME + " DATETIME,"
                + KEY_UPLOADED + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void AddData(GPSData data) {
        GPSData lastData = GetLastData();
        if (lastData!=null && lastData.Equals(data)) return;

        SQLiteDatabase db = this.getWritableDatabase();
        if (db==null) return;

        ContentValues values = new ContentValues();
        values.put(KEY_LAT, data.GetLatitude());
        values.put(KEY_LONG, data.GetLongitude());
        values.put(KEY_SPEED, data.GetSpeed());
        values.put(KEY_DIR, data.GetDirection());
        values.put(KEY_TIME, data.GetTime());
        values.put(KEY_UPLOADED, 0);

        db.insert(TABLE_NAME, null, values);
    }

    public GPSData GetLastData() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db==null) return null;

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToLast()) {
            GPSData data = new GPSData();
            data.SetLatitude(cursor.getDouble(1));
            data.SetLongitude(cursor.getDouble(2));
            data.SetSpeed(cursor.getDouble(3));
            data.SetDirection(cursor.getDouble(4));
            data.SetTime(cursor.getString(5));
            return data;
        }
        return null;
    }

    public List<GPSData> GetAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db==null) return null;

        List<GPSData> dataList = new ArrayList<GPSData>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                GPSData data = new GPSData();
                data.SetLatitude(cursor.getDouble(1));
                data.SetLongitude(cursor.getDouble(2));
                data.SetSpeed(cursor.getDouble(3));
                data.SetDirection(cursor.getDouble(4));
                data.SetTime(cursor.getString(5));
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        return dataList;
    }

    public List<GPSData> GetNewData() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db==null) return null;

        List<GPSData> dataList = new ArrayList<GPSData>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                int updated = cursor.getInt(6);
                if (updated==0) {
                    GPSData data = new GPSData();
                    data.SetLatitude(cursor.getDouble(1));
                    data.SetLongitude(cursor.getDouble(2));
                    data.SetSpeed(cursor.getDouble(3));
                    data.SetDirection(cursor.getDouble(4));
                    data.SetTime(cursor.getString(5));
                    dataList.add(data);
                }
            } while (cursor.moveToNext());
        }
        return dataList;
    }

    public void SetUpdateAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        if (db==null) return;

        ContentValues values = new ContentValues();
        values.put(KEY_UPLOADED, 1);
        db.update(TABLE_NAME, values, null, null);
    }

    public int GetCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db==null) return -1;
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void DeleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db==null) return;
        db.delete(TABLE_NAME, null, null);
    }

    public String GetJSON() {
        JSONObject parent = new JSONObject();
        JSONArray array = new JSONArray();
        SharedPreferences settings = m_context.getSharedPreferences(RegisterActivity.PREFS_NAME, 0);
        try {
            JSONObject user = new JSONObject();
            user.put("Email", settings.getString("Email", ""));
            user.put("Password", settings.getString("Password", ""));
            parent.put("User", user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<GPSData> dataList = GetNewData();
        for (GPSData data : dataList) {
            JSONObject obj = new JSONObject();
            try
            {
                obj.put("Latitude", data.GetLatitude());
                obj.put("Longitude", data.GetLongitude());
                obj.put("Speed", data.GetSpeed());
                obj.put("Direction", data.GetDirection());
                obj.put("Time", data.GetTime());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            parent.put("History", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parent.toString();
    }
}
