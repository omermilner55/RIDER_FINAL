package com.example.riderfinal;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class HelperDB extends SQLiteOpenHelper {
    public static final int Oldversion = 3;
    public static final int Version = 50;

    // Database name
    public static final String DB_FILE = "RiderSQL.db";

    // Users table
    public static final String USERS_TABLE = "Users";
    public static final String USER_NAME = "UserName";
    public static final String USER_EMAIL = "UserEmail";
    public static final String USER_PWD = "UserPassword";
    public static final String USER_RETYPE = "UserReType";
    public static final String USER_PHONE = "UserPhone";
    public static final String USER_IMAGE_URI = "UserImageUri";
    public static final String USER_POINTS = "UserPoints";

    // Rides table
    public static final String RIDES_TABLE = "Rides";
    public static final String RIDE_ID = "RideId";
    public static final String RIDE_DATE = "RideDate";
    public static final String RIDE_TIME = "RideTime";
    public static final String RIDE_START_LOCATION = "RideStartLocation";
    public static final String RIDE_END_LOCATION = "RideEndLocation";
    public static final String RIDE_DISTANCE = "RideDistance";
    public static final String RIDE_DURATION = "RideDuration";
    public static final String RIDE_AVG_SPEED = "RideAvgSpeed";
    public static final String RIDE_TRUCK_IMG = "RideTruckImg";
    public static final String RIDE_POINTS = "RidePoints";

    public HelperDB(@Nullable Context context) {
        super(context, DB_FILE, null, Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(buildUserTable());
        db.execSQL(buildRidesTable());
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {

        }
    }

    public String buildUserTable() {
        return "CREATE TABLE IF NOT EXISTS " + USERS_TABLE + " (" +
                USER_NAME + " TEXT PRIMARY KEY, " +
                USER_EMAIL + " TEXT, " +
                USER_PWD + " TEXT, " +
                USER_RETYPE + " TEXT, " +
                USER_PHONE + " TEXT, " +
                USER_POINTS + " INTEGER, " +
                USER_IMAGE_URI + " TEXT);";
    }

    public String buildRidesTable() {
        return "CREATE TABLE IF NOT EXISTS " + RIDES_TABLE + " (" +
                RIDE_ID + " INTEGER PRIMARY KEY, " +
                RIDE_DATE + " TEXT, " +
                RIDE_TIME + " TEXT, " +
                RIDE_START_LOCATION + " TEXT, " +
                RIDE_END_LOCATION + " TEXT, " +
                RIDE_DISTANCE + " TEXT, " +
                RIDE_DURATION + " TEXT, " +
                RIDE_AVG_SPEED + " TEXT, " +
                RIDE_TRUCK_IMG + " TEXT, " +
                RIDE_POINTS + " INTEGER);";
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USERS_TABLE + " WHERE " + USER_EMAIL + " = ? AND " + USER_PWD + " = ?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public ArrayList<Ride> getAllRidesSortedByDate() {
        ArrayList<Ride> rides = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // וודא שהטבלה קיימת לפני ביצוע השאילתה
        Cursor cursor = db.rawQuery("SELECT * FROM " + RIDES_TABLE + " ORDER BY " + RIDE_DATE + " DESC, " + RIDE_TIME + " DESC", null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    int rideId = cursor.getColumnIndex(RIDE_ID) != -1 ? cursor.getInt(cursor.getColumnIndexOrThrow(RIDE_ID)) : -1;
                    String rideDate = cursor.getColumnIndex(RIDE_DATE) != -1 ? cursor.getString(cursor.getColumnIndexOrThrow(RIDE_DATE)) : null;
                    String rideTime = cursor.getColumnIndex(RIDE_TIME) != -1 ? cursor.getString(cursor.getColumnIndexOrThrow(RIDE_TIME)) : null;
                    String rideDistance = cursor.getColumnIndex(RIDE_DISTANCE) != -1 ? cursor.getString(cursor.getColumnIndexOrThrow(RIDE_DISTANCE)) : null;
                    String rideDuration = cursor.getColumnIndex(RIDE_DURATION) != -1 ? cursor.getString(cursor.getColumnIndexOrThrow(RIDE_DURATION)) : null;
                    String rideAvgSpeed = cursor.getColumnIndex(RIDE_AVG_SPEED) != -1 ? cursor.getString(cursor.getColumnIndexOrThrow(RIDE_AVG_SPEED)) : null;
                    String rideStartLocation = cursor.getColumnIndex(RIDE_START_LOCATION) != -1 ? cursor.getString(cursor.getColumnIndexOrThrow(RIDE_START_LOCATION)) : null;
                    String rideEndLocation = cursor.getColumnIndex(RIDE_END_LOCATION) != -1 ? cursor.getString(cursor.getColumnIndexOrThrow(RIDE_END_LOCATION)) : null;
                    int ridePoints = cursor.getColumnIndex(RIDE_POINTS) != -1 ? Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(String.valueOf(RIDE_POINTS)))) : null;
                    String rideTruckImg = cursor.getColumnIndex(RIDE_TRUCK_IMG) != -1 ? cursor.getString(cursor.getColumnIndexOrThrow(RIDE_TRUCK_IMG)) : null;

                    // צור אובייקט Ride והוסף לרשימה
                    Ride ride = new Ride(rideAvgSpeed, rideDate, rideDistance, rideDuration, rideEndLocation, rideTruckImg, rideId, ridePoints, rideStartLocation, rideTime);
                    rides.add(ride);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("Database Error", "Error reading rides: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return rides;
    }





}