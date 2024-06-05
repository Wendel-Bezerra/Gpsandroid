package com.ucsalpdm.gps;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "trilhas.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "trilhas";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_START_LATITUDE = "start_latitude";
    private static final String COLUMN_START_LONGITUDE = "start_longitude";
    private static final String COLUMN_END_LATITUDE = "end_latitude";
    private static final String COLUMN_END_LONGITUDE = "end_longitude";

    public LocationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_START_LATITUDE + " REAL, " +
                COLUMN_START_LONGITUDE + " REAL, " +
                COLUMN_END_LATITUDE + " REAL, " +
                COLUMN_END_LONGITUDE + " REAL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addTrilha(LatLng start, LatLng end) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_START_LATITUDE, start.latitude);
        values.put(COLUMN_START_LONGITUDE, start.longitude);
        values.put(COLUMN_END_LATITUDE, end.latitude);
        values.put(COLUMN_END_LONGITUDE, end.longitude);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<Trilha> getAllTrilhas() {
        List<Trilha> trilhas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                LatLng start = new LatLng(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_START_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_START_LONGITUDE)));
                LatLng end = new LatLng(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_END_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_END_LONGITUDE)));
                trilhas.add(new Trilha(start, end));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return trilhas;
    }

    public static class Trilha {
        private LatLng start;
        private LatLng end;

        public Trilha(LatLng start, LatLng end) {
            this.start = start;
            this.end = end;
        }

        public LatLng getStart() {
            return start;
        }

        public LatLng getEnd() {
            return end;
        }
    }
}

