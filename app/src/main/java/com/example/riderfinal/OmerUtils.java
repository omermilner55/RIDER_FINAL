package com.example.riderfinal;

import static android.content.Context.MODE_PRIVATE;
import static com.example.riderfinal.HelperDB.REWARDS_TABLE;
import static com.example.riderfinal.HelperDB.REWARD_DESCRIPTION;
import static com.example.riderfinal.HelperDB.REWARD_ID;
import static com.example.riderfinal.HelperDB.REWARD_IMG;
import static com.example.riderfinal.HelperDB.REWARD_NAME;
import static com.example.riderfinal.HelperDB.REWARD_POINTS_PRC;
import static com.example.riderfinal.HelperDB.RIDES_TABLE;
import static com.example.riderfinal.HelperDB.RIDE_AVG_SPEED;
import static com.example.riderfinal.HelperDB.RIDE_DATE;
import static com.example.riderfinal.HelperDB.RIDE_DISTANCE;
import static com.example.riderfinal.HelperDB.RIDE_DURATION;
import static com.example.riderfinal.HelperDB.RIDE_END_LOCATION;
import static com.example.riderfinal.HelperDB.RIDE_ID;
import static com.example.riderfinal.HelperDB.RIDE_POINTS;
import static com.example.riderfinal.HelperDB.RIDE_START_LOCATION;
import static com.example.riderfinal.HelperDB.RIDE_TIME;
import static com.example.riderfinal.HelperDB.RIDE_TRUCK_IMG;
import static com.example.riderfinal.HelperDB.RIDE_USER_EMAIL;
import static com.example.riderfinal.HelperDB.USERS_TABLE;
import static com.example.riderfinal.HelperDB.USER_EMAIL;
import static com.example.riderfinal.HelperDB.USER_NAME;
import static com.example.riderfinal.HelperDB.USER_PHONE;
import static com.example.riderfinal.HelperDB.USER_POINTS;
import static com.example.riderfinal.HelperDB.USER_PWD;
import static com.example.riderfinal.HelperDB.USER_REWARDS_TABLE;
import static com.example.riderfinal.HelperDB.USER_REWARD_CODE;
import static com.example.riderfinal.HelperDB.USER_REWARD_DATE;
import static com.example.riderfinal.HelperDB.USER_REWARD_REWARDID;
import static com.example.riderfinal.HelperDB.USER_REWARD_USERNAME;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OmerUtils {
    private static final float MIN_LEGAL_SPEED = 5.0f; // km/h
    private static final float MAX_LEGAL_SPEED = 26.5f; // km/h
    private static final float PENALTY_SPEED_THRESHOLD = 27.0f; // km/h
    private static final long CONTINUOUS_LEGAL_RIDE_THRESHOLD = 30 * 60 * 1000; // 30 minutes in milliseconds
    private static int Ridepoints = 0;
    private static double lastLegalDistance = 0;
    private static long lastLegalSpeedTimestamp = 0;
    private static long continuousLegalRideStartTime = 0;
    private static boolean isRidingLegal = false;
    private static int bonusAwarded = 0;


    // מחזירה את השעה הנוכחית בפורמט של שעות:דקות:שניות
    public static String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // מחזירה את התאריך של היום הנוכחי בפורמט של יום/חודש/שנה
    public static String getTodaysDate() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }


    // מקבלת מיקום ומחזירה כתובת אנושית (Street, City וכו') בעזרת Geocoder
    public static String getLocationAddress(Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown location";
    }

    // מחשבת את המרחק הכולל בין רשימת מיקומים
    public static double calculateDistance(List<Location> locationList) {
        double totalDistance = 0;
        if (locationList.size() > 1) {
            for (int i = 0; i < locationList.size() - 1; i++) {
                totalDistance += locationList.get(i).distanceTo(locationList.get(i + 1));
            }
        }
        return totalDistance;
    }

    // משנה את הגובה של פרגמנט מסוים בהתאם לפרמטר
    public static void changeFragmentLayout(FrameLayout frameLayout, int height) {
        if (frameLayout != null) {
            ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
            params.height = height;
            frameLayout.setLayoutParams(params);
        } else {
            throw new NullPointerException("FrameLayout is null. Make sure you are passing a valid view.");
        }
    }

    // שומרת תמונת מפה בזיכרון הפנימי של המכשיר ומחזירה את הנתיב לקובץ
    public static String saveBitmapToInternalStorage(Context context, Bitmap bitmap, int rideId) {
        FileOutputStream fos = null;
        try {
            File file = new File(context.getFilesDir(), "ride_" + rideId + ".png");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving map snapshot: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // מכניסה נתוני נסיעה חדשה לטבלת הרכיבות בבסיס הנתונים
    public static void insertNewRideToDatabase(Context context, HelperDB helperDB, int rideID, List<Location> locationList) {
        try {
            // בדיקה שהרשימה לא ריקה
            if (locationList == null || locationList.isEmpty()) {
                Toast.makeText(context, "שגיאה: רשימת מיקומים ריקה", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = helperDB.getWritableDatabase();

            // קבלת שם המשתמש הנוכחי מה-SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
            String useremail = prefs.getString("useremail", "");

            if (useremail == null || useremail.isEmpty()) {
                Toast.makeText(context, "שגיאה: משתמש לא מזוהה", Toast.LENGTH_SHORT).show();
                return;
            }

            String startLocation = getLocationAddress(context, locationList.get(0));
            if (startLocation == null || startLocation.isEmpty()) {
                startLocation = "התחלה"; // ערך ברירת מחדל במקרה שה-geocoding נכשל
            }

            ContentValues values = new ContentValues();
            values.put(RIDE_ID, rideID);
            values.put(RIDE_DATE, getTodaysDate());
            values.put(RIDE_TIME, getCurrentTime());
            values.put(RIDE_POINTS, 0);
            values.put(RIDE_START_LOCATION, startLocation);
            values.put(RIDE_USER_EMAIL, useremail);

            long newRowId = db.insert(RIDES_TABLE, null, values);

            if (newRowId == -1) {
                Toast.makeText(context, "שגיאה בהכנסת נסיעה", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "נסיעה חדשה נוספה בהצלחה. ID: " + rideID, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // מעדכנת את נתוני הנסיעה (מרחק, מהירות, משך וכו') בבסיס הנתונים לאחר סיום נסיעה
    public static void updateRideDataInDatabase(Context context, HelperDB helperDB, int rideID,
                                                List<Location> locationList, long startTime) {
        try {
            // בדיקה שהרשימה לא ריקה
            if (locationList == null || locationList.isEmpty()) {
                Log.e("OmerUtils", "Location list is empty or null, cannot update ride data");
                Toast.makeText(context, "שגיאה: נתוני מיקום חסרים", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = helperDB.getWritableDatabase();
            Location lastLocation = locationList.get(locationList.size() - 1);

            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // בשניות
            double distance = calculateDistance(locationList); // במטרים
            double avgSpeed = elapsedTime > 0 ? (distance / elapsedTime) * 3.6 : 0;

            String endLocation = getLocationAddress(context, lastLocation);
            String duration = formatTime(System.currentTimeMillis() - startTime);
            String distanceStr = formatDistance(distance);
            String avgSpeedStr = formatSpeed((float) avgSpeed);

            // בדיקה שכל הערכים הקריטיים לא null
            if (endLocation == null || duration == null || distanceStr == null || avgSpeedStr == null) {
                Log.e("OmerUtils", "One or more critical ride values are null");
                Toast.makeText(context, "שגיאה: נתוני רכיבה חסרים", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put(RIDE_END_LOCATION, endLocation);
            values.put(RIDE_DURATION, duration);
            values.put(RIDE_DISTANCE, distanceStr);
            values.put(RIDE_AVG_SPEED, avgSpeedStr);
            values.put(RIDE_POINTS, Ridepoints);

            int updated = db.update(RIDES_TABLE, values, RIDE_ID + " = ?",
                    new String[]{String.valueOf(rideID)});

            if (updated > 0) {
                updateUserPoints(context, db, Ridepoints);
            } else {
                Log.e("OmerUtils", "Failed to update ride with ID: " + rideID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error updating ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // מעדכנת את נקודות המשתמש בבסיס הנתונים לאחר נסיעה
    public static void updateUserPoints(Context context, SQLiteDatabase db, int newPoints) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
        String useremail = prefs.getString("useremail", "");
        User user = OmerUtils.getUserByEmail(context, useremail);
        Cursor cursor = db.query(USERS_TABLE, new String[]{USER_POINTS},
                USER_NAME + "=?", new String[]{user.getUserName()}, null, null, null);

        if (cursor.moveToFirst()) {
            int currentPoints = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put(USER_POINTS, currentPoints + newPoints);
            db.update(USERS_TABLE, values, USER_NAME + "=?", new String[]{user.getUserName()});
            cursor.close();
        }
    }


    // ממירה זמן ממילישניות לפורמט של שעות:דקות:שניות
    public static String formatTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    // ממירה מרחק למטרים או קילומטרים עם פורמט תצוגה נוח
    public static String formatDistance(double distance) {
        DecimalFormat df = new DecimalFormat("#.##");
        return distance < 1000 ?
                (int) distance + " m" :
                df.format(distance / 1000) + " km";
    }

    // ממירה מהירות ממטר לשנייה לקמ"ש ומחזירה כתוב בפורמט תצוגה
    public static String formatSpeed(float speedKph) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(speedKph) + " km/h";
    }

    // מאפסת את התצוגה של הממשק (טיימר, מרחק, מהירות, נקודות) לאחר עצירה
    public static void resetUi(TextView timerTxt, TextView distanceTxt,
                               TextView speedTxt, TextView pointsTxt) {
        timerTxt.setText("00:00:00");
        distanceTxt.setText("  Distance: 000 m");
        speedTxt.setText("Speed: 00.00 km/h");
        pointsTxt.setText("Points: 0000 pt");
    }

    // מאתחלת קו פוליליין חדש למפת GoogleMap
    public static Polyline initializePolyline(GoogleMap googleMap) {
        if (googleMap != null) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(0xFFFFA500)  // כתום
                    .width(10f);
            return googleMap.addPolyline(polylineOptions);
        }
        return null;
    }


    @SuppressLint("MissingPermission")
    // שומרת צילום של מפת המסלול ומעדכנת את הנתיב לקובץ בבסיס הנתונים
    public static void saveMapSnapshot(GoogleMap googleMap, List<Location> locationList,
                                       Context context, HelperDB helperDB, int rideID) {
        if (googleMap != null && !locationList.isEmpty()) {
            googleMap.setMyLocationEnabled(false);

            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (Location location : locationList) {
                boundsBuilder.include(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            LatLngBounds bounds = boundsBuilder.build();

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 400), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    new android.os.Handler().postDelayed(() -> {
                        googleMap.snapshot(bitmap -> {
                            if (bitmap != null) {
                                String filePath = saveBitmapToInternalStorage(context, bitmap, rideID);
                                if (filePath != null) {
                                    SQLiteDatabase db = helperDB.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put(RIDE_TRUCK_IMG, filePath);
                                    db.update(RIDES_TABLE, values, RIDE_ID + " = ?", new String[]{String.valueOf(rideID)});
                                    db.close();
                                }
                            }

                            try {
                                googleMap.setMyLocationEnabled(true);
                                // Reset camera to follow user
                                if (!locationList.isEmpty()) {
                                    Location lastLocation = locationList.get(locationList.size() - 1);
                                    LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
                                }
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                        });
                    }, 1500);
                }

                @Override
                public void onCancel() {
                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    // מעדכנת את הממשק החי (UI) עם נתוני מרחק, מהירות ונקודות בעת הנסיעה
    public static void updateValuesInUi(List<Location> locationList, boolean isPlaying,
                                        TextView distanceTxt, TextView speedTxt, TextView pointsTxt) {
        if (!isPlaying) {
            distanceTxt.setText("Distance: 000 m");
            speedTxt.setText("Speed: 000.00 km/h");
            pointsTxt.setText("Points: 0000 pt");
            resetPointsTracking();
            return;
        }

        if (!locationList.isEmpty()) {
            DecimalFormat df = new DecimalFormat("#.##");
            double distance = calculateDistance(locationList);

            // Update distance display
            if (distance < 1000) {
                distanceTxt.setText("  Distance: " + (int) distance + " m");
            } else {
                distanceTxt.setText("  Distance: " + df.format(distance / 1000) + " km");
            }

            // Get current speed and update display
            Location lastLocation = locationList.get(locationList.size() - 1);
            float speedKmh = lastLocation.getSpeed() * 3.6f;
            speedTxt.setText("Speed: " + df.format(speedKmh) + " km/h");

            // Calculate points based on speed and distance
            int displaypoints = calculatePoints(speedKmh, distance, System.currentTimeMillis());
            pointsTxt.setText("Points: " + displaypoints + " pt");
        }
    }

    // מחשבת את הנקודות שהמשתמש צבר לפי המהירות והמרחק, כולל בונוסים/קנסות
    public static int calculatePoints(float currentSpeed, double distance, long currentTime) {
        // בדיקה אם המהירות חוקית
        boolean isCurrentSpeedLegal = currentSpeed >= MIN_LEGAL_SPEED && currentSpeed <= MAX_LEGAL_SPEED;

        if (isCurrentSpeedLegal) {
            // אם זו התחלה של נסיעה חוקית
            if (!isRidingLegal) {
                isRidingLegal = true;
                continuousLegalRideStartTime = currentTime;
                lastLegalDistance = distance;
            }

            // חישוב המרחק שנוסף מאז המדידה האחרונה
            double newDistance = distance - lastLegalDistance;
            if (newDistance >= 100) { // כל 100 מטר
                Ridepoints += 5;
                lastLegalDistance = distance;
            }

            // בדיקת בונוס על נסיעה חוקית רציפה
            long legalRideDuration = currentTime - continuousLegalRideStartTime;
            if (legalRideDuration >= CONTINUOUS_LEGAL_RIDE_THRESHOLD) {
                Ridepoints += 10; // בונוס על 10 דקות נסיעה חוקית

            }
        } else {
            // אם המהירות לא חוקית
            isRidingLegal = false;

            // הורדת נקודות על חריגת מהירות
            if (currentSpeed > PENALTY_SPEED_THRESHOLD) {
                if (currentSpeed > 30) {
                    Ridepoints = Math.max(0, Ridepoints - 5); // הורדת 5 נקודות כשהמהירות מעל 30
                } else {
                    Ridepoints = Math.max(0, Ridepoints - 1); // הורדת 1 נקודות כשהמהירות מעל 26.5
                }
            }
        }

        return Ridepoints;
    }

    // פונקציה לאיפוס מעקב הנקודות
    public static void resetPointsTracking() {
        Ridepoints = 0;
        lastLegalDistance = 0;
        lastLegalSpeedTimestamp = 0;
        continuousLegalRideStartTime = 0;
        isRidingLegal = false;
        bonusAwarded = 0;
    }

    // מחזירה את כל הנסיעות של המשתמש הנוכחי, ממויינות לפי תאריך
    public static ArrayList<Ride> getAllRidesSortedByDate(Context context) {
        ArrayList<Ride> rides = new ArrayList<>();

        // Get database instance from HelperDB
        HelperDB helperDB = new HelperDB(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();

        // Get current username from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String email = prefs.getString("useremail", "");

        // Get user by email using HelperDB method
        User user = OmerUtils.getUserByEmail(context, email);
        Toast.makeText(context, "User: " + user.getUserName(), Toast.LENGTH_SHORT).show();
        if (user == null) {
            Log.e("DatabaseUtility", "User not found for email: " + email);
            db.close();
            return rides;
        }

        // Query rides table for the current user's rides
        Cursor cursor = db.rawQuery("SELECT * FROM " + RIDES_TABLE +
                        " WHERE " + RIDE_USER_EMAIL + " = ?" +
                        " ORDER BY " + RIDE_DATE + " DESC, " + RIDE_TIME + " DESC",
                new String[]{user.getUserEmail()});

        try {
            if (cursor.moveToFirst()) {
                do {
                    int rideId = cursor.getColumnIndex(RIDE_ID) != -1 ?
                            cursor.getInt(cursor.getColumnIndexOrThrow(RIDE_ID)) : -1;

                    String rideDate = cursor.getColumnIndex(RIDE_DATE) != -1 ?
                            cursor.getString(cursor.getColumnIndexOrThrow(RIDE_DATE)) : null;

                    String rideTime = cursor.getColumnIndex(RIDE_TIME) != -1 ?
                            cursor.getString(cursor.getColumnIndexOrThrow(RIDE_TIME)) : null;

                    String rideDistance = cursor.getColumnIndex(RIDE_DISTANCE) != -1 ?
                            cursor.getString(cursor.getColumnIndexOrThrow(RIDE_DISTANCE)) : null;

                    String rideDuration = cursor.getColumnIndex(RIDE_DURATION) != -1 ?
                            cursor.getString(cursor.getColumnIndexOrThrow(RIDE_DURATION)) : null;

                    String rideAvgSpeed = cursor.getColumnIndex(RIDE_AVG_SPEED) != -1 ?
                            cursor.getString(cursor.getColumnIndexOrThrow(RIDE_AVG_SPEED)) : null;

                    String rideStartLocation = cursor.getColumnIndex(RIDE_START_LOCATION) != -1 ?
                            cursor.getString(cursor.getColumnIndexOrThrow(RIDE_START_LOCATION)) : null;

                    String rideEndLocation = cursor.getColumnIndex(RIDE_END_LOCATION) != -1 ?
                            cursor.getString(cursor.getColumnIndexOrThrow(RIDE_END_LOCATION)) : null;

                    int ridePoints = cursor.getColumnIndex(RIDE_POINTS) != -1 ?
                            cursor.getInt(cursor.getColumnIndexOrThrow(RIDE_POINTS)) : 0;

                    String rideTruckImg = cursor.getColumnIndex(RIDE_TRUCK_IMG) != -1 ?
                            cursor.getString(cursor.getColumnIndexOrThrow(RIDE_TRUCK_IMG)) : null;

                    // Create Ride object and add to list
                    Ride ride = new Ride(rideAvgSpeed, rideDate, rideDistance, rideDuration,
                            rideEndLocation, rideTruckImg, rideId, ridePoints,
                            rideStartLocation, rideTime);

                    rides.add(ride);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error retrieving rides: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return rides;
    }

    // מאפשרת למשתמש לרכוש פרס אם יש לו מספיק נקודות, ומחזירה קוד מימוש
    public static String purchaseReward(Context context, String email, int rewardId) {
        HelperDB helperDB = new HelperDB(context);
        SQLiteDatabase db = null;

        try {
            db = helperDB.getWritableDatabase();

            // בדוק האם המשתמש והפרס קיימים
            User user = getUserByEmail(context, email);
            Reward reward = getRewardById(context, rewardId);

            if (user == null || reward == null) {
                Log.e("DatabaseUtility", "User or reward not found");
                return null; // משתמש או פרס לא נמצאו
            }

            // בדוק האם למשתמש יש מספיק נקודות
            if (user.getUserPoints() < reward.getRewardPointsPrice()) {
                Log.e("DatabaseUtility", "Not enough points. User has: " +
                        user.getUserPoints() + ", Reward costs: " + reward.getRewardPointsPrice());
                return null; // אין מספיק נקודות
            }

            // בדוק האם המשתמש כבר רכש את הפרס הזה
            if (hasUserPurchasedReward(context, user.getUserName(), rewardId)) {
                // אם המשתמש כבר רכש את הפרס, החזר את הקוד הקיים
                return getSavedRedemptionCode(context, user.getUserName(), rewardId);
            }

            // חישוב נקודות חדש לאחר הרכישה
            int newPoints = user.getUserPoints() - reward.getRewardPointsPrice();

            // עדכון נקודות המשתמש בטרנזקציה
            db.beginTransaction();
            try {
                // עדכון נקודות בטבלת המשתמשים
                ContentValues userValues = new ContentValues();
                userValues.put(USER_POINTS, newPoints);
                int updatedRows = db.update(USERS_TABLE, userValues,
                        USER_NAME + "=?", new String[]{user.getUserName()});

                if (updatedRows <= 0) {
                    Log.e("DatabaseUtility", "Failed to update user points");
                    return null; // עדכון נקודות נכשל
                }

                // יצירת קוד מימוש ייחודי
                String redemptionCode = generateRedemptionCode(rewardId);

                // שמירת הרכישה בטבלת UserRewards
                ContentValues rewardValues = new ContentValues();
                rewardValues.put(USER_REWARD_USERNAME, user.getUserName());
                rewardValues.put(USER_REWARD_REWARDID, rewardId);
                rewardValues.put(USER_REWARD_DATE, getCurrentDate());
                rewardValues.put(USER_REWARD_CODE, redemptionCode);

                long result = db.insert(USER_REWARDS_TABLE, null, rewardValues);

                if (result == -1) {
                    Log.e("DatabaseUtility", "Failed to record reward purchase");
                    return null; // הוספת רשומת רכישה נכשלה
                }

                // אם הגענו לכאן, הכל הצליח - אישור הטרנזקציה
                db.setTransactionSuccessful();

                return redemptionCode;

            } finally {
                // סיום הטרנזקציה בכל מקרה
                db.endTransaction();
            }

        } catch (Exception e) {
            Log.e("DatabaseUtility", "Error purchasing reward: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // בודקת האם המשתמש כבר רכש את הפרס הזה בעבר
    public static boolean hasUserPurchasedReward(Context context, String username, int rewardId) {
        HelperDB helperDB = new HelperDB(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    USER_REWARDS_TABLE,
                    null,
                    USER_REWARD_USERNAME + "=? AND " + USER_REWARD_REWARDID + "=?",
                    new String[]{username, String.valueOf(rewardId)},
                    null,
                    null,
                    null
            );

            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("DatabaseUtility", "Error checking user purchase: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // בודקת האם קיים משתמש עם המייל והסיסמה הנתונים
    public static boolean checkUser(Context context, String email, String password) {
        HelperDB helperDB = new HelperDB(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USERS_TABLE + " WHERE " + USER_EMAIL + " = ? AND " + USER_PWD + " = ?",
                new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // מחזירה את מזהה הנסיעה הבא (Ride ID) בהתבסס על הערך המקסימלי הקיים בטבלה
    public static int getNextRideId(Context context) {
        HelperDB helperDB = new HelperDB(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + RIDE_ID + ") FROM " + RIDES_TABLE, null);

        int nextId = 1; // Default starting ID
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            nextId = cursor.getInt(0) + 1;
        }
        cursor.close();
        db.close();
        return nextId;
    }

    // מחזירה את התאריך והשעה הנוכחיים בפורמט בסיסי (לשימוש בקוד מימוש)
    private static String getCurrentDate() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new java.util.Date());
    }

    // יוצרת קוד מימוש ייחודי לרכישת פרס
    private static String generateRedemptionCode(int rewardId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomDigits = String.valueOf((int) (Math.random() * 10000));
        return "RWD-" + rewardId + "-" + timestamp.substring(timestamp.length() - 5) + "-" + randomDigits;
    }

    // מחזירה אובייקט משתמש לפי מייל
    public static User getUserByEmail(Context context, String email) {
        HelperDB helperDB = new HelperDB(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();
        User user = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    USERS_TABLE,
                    null,
                    USER_EMAIL + "=?",
                    new String[]{email},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME));
                String userEmail = cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL));
                String userPwd = cursor.getString(cursor.getColumnIndexOrThrow(USER_PWD));
                String userPhone = cursor.getString(cursor.getColumnIndexOrThrow(USER_PHONE));
                int userPoints = cursor.getInt(cursor.getColumnIndexOrThrow(USER_POINTS));

                user = new User(userName, userEmail, userPwd, "null", userPhone, userPoints);
            }
        } catch (Exception e) {
            Log.e("DatabaseUtility", "Error retrieving user data: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return user;
    }

    // מחזירה אובייקט פרס לפי מזהה
    public static Reward getRewardById(Context context, int rewardId) {
        HelperDB helperDB = new HelperDB(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();
        Reward reward = null;

        try {
            String query = "SELECT * FROM " + REWARDS_TABLE + " WHERE " + REWARD_ID + " = " + rewardId;
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(REWARD_NAME));
                String img = cursor.getString(cursor.getColumnIndexOrThrow(REWARD_IMG));
                int points = cursor.getInt(cursor.getColumnIndexOrThrow(REWARD_POINTS_PRC));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(REWARD_DESCRIPTION));

                reward = new Reward(rewardId, name, img, points, desc);
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            Log.e("DatabaseUtility", "Error retrieving reward data: " + e.getMessage());
        }

        return reward;
    }

    // מוחקת רשומת נסיעה לפי מזהה
    public static boolean deleteRide(Context context, int rideId) {
        HelperDB helperDB = new HelperDB(context);
        SQLiteDatabase db = helperDB.getWritableDatabase();

        try {
            // Delete the ride with the specified ID
            int result = db.delete(RIDES_TABLE, RIDE_ID + " = ?", new String[]{String.valueOf(rideId)});

            // Return true if at least one row was deleted
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseUtility", "Error deleting ride: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // מחזירה את קוד המימוש של פרס שנרכש על ידי המשתמש
    public static String getSavedRedemptionCode(Context context, String username, int rewardId) {
        HelperDB helperDB = new HelperDB(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    HelperDB.USER_REWARDS_TABLE,
                    new String[]{HelperDB.USER_REWARD_CODE},
                    HelperDB.USER_REWARD_USERNAME + "=? AND " + HelperDB.USER_REWARD_REWARDID + "=?",
                    new String[]{username, String.valueOf(rewardId)},
                    null,
                    null,
                    HelperDB.USER_REWARD_DATE + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(HelperDB.USER_REWARD_CODE));
            }
            return "Code not found";
        } catch (Exception e) {
            Log.e("Database Error", "Error retrieving redemption code: " + e.getMessage());
            return "Code not found";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

}