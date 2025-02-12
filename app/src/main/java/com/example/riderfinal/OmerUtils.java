package com.example.riderfinal;

import static android.content.Context.MODE_PRIVATE;
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
import static com.example.riderfinal.HelperDB.USERS_TABLE;
import static com.example.riderfinal.HelperDB.USER_NAME;
import static com.example.riderfinal.HelperDB.USER_POINTS;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OmerUtils {
    private static final float MIN_LEGAL_SPEED = 5.0f; // km/h
    private static final float MAX_LEGAL_SPEED = 26.5f; // km/h
    private static final float PENALTY_SPEED_THRESHOLD = 27.0f; // km/h
    private static final long CONTINUOUS_LEGAL_RIDE_THRESHOLD = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static int Ridepoints = 0;
    private static double lastLegalDistance = 0;
    private static long lastLegalSpeedTimestamp = 0;
    private static long continuousLegalRideStartTime = 0;
    private static boolean isRidingLegal = false;
    private static int bonusAwarded = 0;



    // Format current time
    public static String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // Format today's date
    public static String getTodaysDate() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }


    // Get a formatted location address
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
    // Calculate distance
    public static double calculateDistance(List<Location> locationList) {
        double totalDistance = 0;
        if (locationList.size() > 1) {
            for (int i = 0; i < locationList.size() - 1; i++) {
                totalDistance += locationList.get(i).distanceTo(locationList.get(i + 1));
            }
        }
        return totalDistance;
    }

    // Change fragment layout
    public static void changeFragmentLayout(FrameLayout frameLayout, int height) {
        if (frameLayout != null) {
            ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
            params.height = height;
            frameLayout.setLayoutParams(params);
        } else {
            throw new NullPointerException("FrameLayout is null. Make sure you are passing a valid view.");
        }
    }
    // Saving map photo path to database
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

    public static void insertNewRideToDatabase(Context context, HelperDB helperDB, int rideID, List<Location> locationList) {
        try {
            SQLiteDatabase db = helperDB.getWritableDatabase();


            ContentValues values = new ContentValues();
            values.put(RIDE_ID, rideID);
            values.put(RIDE_DATE, getTodaysDate());
            values.put(RIDE_TIME, getCurrentTime());
            values.put(RIDE_POINTS, 0);
            values.put(RIDE_START_LOCATION, locationList.isEmpty() ? "התחלה" :
                    getLocationAddress(context, locationList.get(0)));

            long newRowId = db.insert(RIDES_TABLE, null, values);

            if (newRowId == -1) {
                Toast.makeText(context, "שגיאה בהכנסת נסיעה", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "נסיעה חדשה נוספה בהצלחה. ID: " + rideID, Toast.LENGTH_SHORT).show();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void updateRideDataInDatabase(Context context, HelperDB helperDB, int rideID,
                                                List<Location> locationList, long startTime) {
        try {
            SQLiteDatabase db = helperDB.getWritableDatabase();
            Location lastLocation = locationList.get(locationList.size() - 1);

            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            double distance = calculateDistance(locationList);
            double avgSpeed = distance / 1000.0 / (elapsedTime / 3600.0);

            // חישוב הנקודות הסופיות
            float finalSpeed = lastLocation.getSpeed() * 3.6f;


            ContentValues values = new ContentValues();
            values.put(RIDE_END_LOCATION, getLocationAddress(context, lastLocation));
            values.put(RIDE_DURATION, formatTime(System.currentTimeMillis() - startTime));
            values.put(RIDE_DISTANCE, formatDistance(distance));
            values.put(RIDE_AVG_SPEED, formatSpeed((float) avgSpeed));

            int updated = db.update(RIDES_TABLE, values, RIDE_ID + " = ?",
                    new String[]{String.valueOf(rideID)});

            if (updated > 0) {
                updateUserPoints(context, db, Ridepoints);
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error updating ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public static void updateUserPoints(Context context, SQLiteDatabase db, int newPoints) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");

        Cursor cursor = db.query(USERS_TABLE, new String[]{USER_POINTS},
                USER_NAME + "=?", new String[]{username}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int currentPoints = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put(USER_POINTS, currentPoints + newPoints);
            db.update(USERS_TABLE, values, USER_NAME + "=?", new String[]{username});
            cursor.close();
        }
    }


    // Format time from milliseconds to HH:mm:ss
    public static String formatTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Format distance to meters or kilometers
    public static String formatDistance(double distance) {
        DecimalFormat df = new DecimalFormat("#.##");
        return distance < 1000 ?
                (int)distance + " m" :
                df.format(distance / 1000) + " km";
    }

    // Format speed from m/s to km/h
    public static String formatSpeed(float speedMps) {
        DecimalFormat df = new DecimalFormat("#.##");
        float speedKph = speedMps * 3.6f;
        return df.format(speedKph) + " km/h";
    }


    public static void resetUi(TextView timerTxt, TextView distanceTxt,
                               TextView speedTxt, TextView pointsTxt) {
        timerTxt.setText("00:00:00");
        distanceTxt.setText("Distance: 0 m");
        speedTxt.setText("  Speed: 0 km/h");
        pointsTxt.setText("  Points: 0 pt");
    }


    // צריך לשנות את הפונקציה ב-OmerUtils ל:
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
    public static void updateValuesInUi(List<Location> locationList, boolean isPlaying,
                                        TextView distanceTxt, TextView speedTxt, TextView pointsTxt) {
        if (!isPlaying) {
            distanceTxt.setText("Distance: 0 m");
            speedTxt.setText("  Speed: 0 km/h");
            pointsTxt.setText("  Points: 0 pt");
            resetPointsTracking(); // הוספנו את זה
            return;
        }

        if (!locationList.isEmpty()) {
            DecimalFormat df = new DecimalFormat("#.##");
            double distance = calculateDistance(locationList);

            // Update distance display
            if (distance < 1000) {
                distanceTxt.setText("Distance: " + (int) distance + " m");
            } else {
                distanceTxt.setText("Distance: " + df.format(distance / 1000) + " km");
            }

            // Get current speed and update display
            Location lastLocation = locationList.get(locationList.size() - 1);
            float speedKmh = lastLocation.getSpeed() * 3.6f;
            speedTxt.setText("  Speed: " + df.format(speedKmh) + " km/h");

            // Calculate points based on speed and distance
            int displaypoints = calculatePoints(speedKmh, distance, System.currentTimeMillis());
            pointsTxt.setText("  Points: " + displaypoints + " pt");
        }
    }

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
            if (legalRideDuration >= CONTINUOUS_LEGAL_RIDE_THRESHOLD && bonusAwarded == 0) {
                Ridepoints += 10; // בונוס על 5 דקות נסיעה חוקית
                bonusAwarded = 1;
            }
        } else {
            // אם המהירות לא חוקית
            isRidingLegal = false;
            bonusAwarded = 0;

            // הורדת נקודות על חריגת מהירות
            if (currentSpeed > PENALTY_SPEED_THRESHOLD) {
                if (currentSpeed > 30) {
                    Ridepoints = Math.max(0, Ridepoints - 10); // הורדת 15 נקודות כשהמהירות מעל 30
                } else {
                    Ridepoints = Math.max(0, Ridepoints - 5); // הורדת 5 נקודות כשהמהירות מעל 26.5
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



}