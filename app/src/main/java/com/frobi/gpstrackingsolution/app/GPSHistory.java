package com.frobi.gpstrackingsolution.app;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    public GPSHistory(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_LAT + " DOUBLE,"
                + KEY_LONG + " DOUBLE,"
                + KEY_SPEED + " DOUBLE,"
                + KEY_DIR + " DOUBLE,"
                + KEY_TIME+ " DATETIME" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void AddData(GPSData data) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db==null) return;

        ContentValues values = new ContentValues();
        values.put(KEY_LAT, data.GetLatitude());
        values.put(KEY_LONG, data.GetLongitude());
        values.put(KEY_SPEED, data.GetSpeed());
        values.put(KEY_DIR, data.GetDirection());
        values.put(KEY_TIME, data.GetTime());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<GPSData> GetAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db==null) return null;

        List<GPSData> dataList = new ArrayList<GPSData>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                GPSData data = new GPSData();
                data.SetLatitude(Double.parseDouble(cursor.getString(1)));
                data.SetLongitude(Double.parseDouble(cursor.getString(2)));
                data.SetSpeed(Double.parseDouble(cursor.getString(3)));
                data.SetDirection(Double.parseDouble(cursor.getString(4)));
                data.SetTime(cursor.getString(5));
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        return dataList;
    }

    public int GetCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db==null) return -1;
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }

    public void DeleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db!=null)
            db.delete(TABLE_NAME, null, null);
    }
}
