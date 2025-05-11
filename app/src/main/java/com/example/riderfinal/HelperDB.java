package com.example.riderfinal;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class HelperDB extends SQLiteOpenHelper {
    public static final int Oldversion = 3;
    public static final int Version = 53;

    // Database name
    public static final String DB_FILE = "RiderSQL.db";

    // Users table
    public static final String USERS_TABLE = "Users";
    public static final String USER_NAME = "UserName";
    public static final String USER_EMAIL = "UserEmail";
    public static final String USER_PWD = "UserPassword";
    public static final String USER_PHONE = "UserPhone";
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
    public static final String RIDE_USER_EMAIL = "RideUseremail";
    // Rewards table
    public static final String REWARDS_TABLE = "Rewards";
    public static final String REWARD_ID = "RewardId";
    public static final String REWARD_NAME = "RewardName";
    public static final String REWARD_IMG = "RewardImage";
    public static final String REWARD_POINTS_PRC = "RewardPoints";
    public static final String REWARD_DESCRIPTION = "RewardDescription";

    // הוספה לחלק העליון של המחלקה - הגדרת טבלה חדשה
    public static final String USER_REWARDS_TABLE = "UserRewards";
    public static final String USER_REWARD_ID = "UserRewardId";
    public static final String USER_REWARD_USERNAME = "UserName";
    public static final String USER_REWARD_REWARDID = "RewardId";
    public static final String USER_REWARD_DATE = "RedemptionDate";
    public static final String USER_REWARD_CODE = "RedemptionCode";


    public HelperDB(@Nullable Context context) {
        super(context, DB_FILE, null, Version);
    }

    // להוסיף לפונקציית onCreate
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(buildUserTable());
        db.execSQL(buildRidesTable());
        db.execSQL(buildRewardsTable());
        db.execSQL(buildUserRewardsTable()); // הוספת הטבלה החדשה
        insertInitialRewards(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // במקרה שיש צורך בעדכון מבנה מסד הנתונים
            // אפשר להוסיף כאן לוגיקה לשדרוג מסד הנתונים
        }
    }

    public String buildUserTable() {
        return "CREATE TABLE IF NOT EXISTS " + USERS_TABLE + " (" +
                USER_NAME + " TEXT PRIMARY KEY, " +
                USER_EMAIL + " TEXT, " +
                USER_PWD + " TEXT, " +
                USER_PHONE + " TEXT, " +
                USER_POINTS + " INTEGER);";
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
                RIDE_POINTS + " INTEGER, " +
                RIDE_USER_EMAIL + " TEXT);";
    }

    public String buildRewardsTable() {
        return "CREATE TABLE IF NOT EXISTS " + REWARDS_TABLE + " (" +
                REWARD_ID + " INTEGER PRIMARY KEY, " +
                REWARD_NAME + " TEXT, " +
                REWARD_IMG + " TEXT, " +
                REWARD_POINTS_PRC + " INTEGER, " +
                REWARD_DESCRIPTION + " TEXT);";
    }

    // להוסיף מתודה חדשה עבור יצירת טבלת UserRewards
    public String buildUserRewardsTable() {
        return "CREATE TABLE IF NOT EXISTS " + USER_REWARDS_TABLE + " (" +
                USER_REWARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_REWARD_USERNAME + " TEXT, " +
                USER_REWARD_REWARDID + " INTEGER, " +
                USER_REWARD_DATE + " TEXT, " +
                USER_REWARD_CODE + " TEXT, " +
                "FOREIGN KEY (" + USER_REWARD_USERNAME + ") REFERENCES " + USERS_TABLE + "(" + USER_NAME + "), " +
                "FOREIGN KEY (" + USER_REWARD_REWARDID + ") REFERENCES " + REWARDS_TABLE + "(" + REWARD_ID + "));";
    }


    private void insertInitialRewards(SQLiteDatabase db) {
        // מערך של פרסים התחלתיים עם האייקונים החדשים
        Object[][] initialRewards = {
                // {id, שם, URI של תמונה, נקודות נדרשות, תיאור}
                {1, "20% discount for a subscription to the Alien Network", "alien1234", 500, "Get an exclusive 20% discount on your subscription to the Alien Network. Experience premium content and exclusive features."},
                {2, "Free ReShake drink size L", "healthshake", 1000, "Enjoy a free large ReShake drink at any of our partner locations. Perfect refreshment after your ride!"},
                {3, "25% discount in the Sports Hall", "sports1234", 550, "Save 25% on your next purchase at the Sports Hall. Valid for all equipment and apparel."},
                {4, "JBJ headphones for 99₪", "hf1", 3000, "Get premium JBJ headphones for just 99₪ instead of the regular price. Limited time offer."},
                {5, "15% discount at the electric king", "electric1234", 100, "Save 15% on your next purchase at the Electric King. Valid for all electronic devices and accessories."}
        };

        // הכנסת כל הפרסים לטבלה
        for (Object[] reward : initialRewards) {
            ContentValues values = new ContentValues();
            values.put(REWARD_ID, (Integer) reward[0]);
            values.put(REWARD_NAME, (String) reward[1]);
            values.put(REWARD_IMG, (String) reward[2]);
            values.put(REWARD_POINTS_PRC, (Integer) reward[3]);
            values.put(REWARD_DESCRIPTION, (String) reward[4]);

            db.insert(REWARDS_TABLE, null, values);
        }
    }










}