package com.example.riderfinal;

// חבילות נדרשות למעקב מיקום, התראות ופונקציונליות שירות
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

// שירות רקע המטפל במעקב מיקום לרכיבת אופניים
public class LocationTrackingService extends Service {
    // קבועים עבור ערוץ ההתראות
    private static final String CHANNEL_ID = "BikeTrackingChannel";
    private static final int NOTIFICATION_ID = 1001;

    // רכיבי מעקב מיקום
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    // שמירת היסטוריית המיקומים
    public static List<Location> locationList = new ArrayList<>();

    // מצב המעקב
    private boolean isTracking = false;

    // מופע יחיד (סינגלטון) של השירות
    private static LocationTrackingService instance;

    // שמירת נקודות עבור פוליליין במפה (חיזוי המסלול)
    public static List<LatLng> points = new ArrayList<>();
    public static Polyline polyline;

    // פונקציית גישה למופע היחיד
    public static LocationTrackingService getInstance() {
        return instance;
    }

    // אתחול השירות בעת יצירתו
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        locationList = new ArrayList<>();
        initLocationTracking();
        createNotificationChannel();
    }

    // הגדרת רכיבי מעקב המיקום
    private void initLocationTracking() {
        // אתחול ספק המיקום המשולב
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // הגדרת בקשת המיקום
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdateDelayMillis(1000)
                .build();

        // הגדרת הקולבק שמטפל בעדכוני מיקום
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // עיבוד כל מיקום שהתקבל
                for (Location location : locationResult.getLocations()) {
                    // הוספה להיסטוריית המיקומים
                    locationList.add(location);

                    // עדכון הפוליליין במפה אם הוא קיים
                    if (polyline != null) {
                        points = polyline.getPoints();
                        points.add(new LatLng(location.getLatitude(), location.getLongitude()));
                        polyline.setPoints(points);
                    }

                    // עדכון ההתראה עם המהירות הנוכחית
                    updateNotification(location);

                    // שידור עדכון המיקום לכל רכיב מאזין
                    @SuppressLint("UnsafeImplicitIntentLaunch") Intent intent = new Intent("location_update");
                    intent.putExtra("location", location);
                    sendBroadcast(intent);
                }
            }
        };
    }

    // יצירת ערוץ התראות עבור אנדרואיד 8.0 (אוראו) ומעלה
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bike Tracking Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            // ביטול צליל להתראה
            serviceChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    // טיפול בפקודות שנשלחות לשירות
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "START_TRACKING":
                        startTracking();
                        break;
                    case "STOP_TRACKING":
                        stopTracking();
                        break;
                }
            }
        }
        // אם המערכת סוגרת את השירות, הוא יופעל מחדש עם אינטנט ריק
        return START_STICKY;
    }

    // התחלת מעקב מיקום
    private void startTracking() {
        if (!isTracking) {
            // הפעלת השירות בחזית עם התראה קבועה
            startForeground(NOTIFICATION_ID, buildNotification());
            try {
                // בקשת עדכוני מיקום
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        getMainLooper());
                isTracking = true;
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    // הפסקת מעקב מיקום
    private void stopTracking() {
        isTracking = false;
        // הסרת בקשות עדכון מיקום
        fusedLocationClient.removeLocationUpdates(locationCallback);
        // הסרת התראת החזית
        stopForeground(true);
        // עצירת השירות
        stopSelf();
    }

    // יצירת ההתראה הראשונית
    private Notification buildNotification() {
        // יצירת אינטנט לפתיחת האפליקציה בעת לחיצה על ההתראה
        Intent notificationIntent = new Intent(this, StartScreenActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // בניית והחזרת ההתראה
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bike Tracking Active")
                .setContentText("Recording your ride...")
                .setSmallIcon(R.drawable.electric1234)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    // עדכון ההתראה עם המהירות הנוכחית
    private void updateNotification(Location location) {
        if (!isTracking) return;

        // המרת מהירות ממטר/שנייה לקמ/שעה
        float speedKmh = location.getSpeed() * 3.6f;

        // בניית התראה מעודכנת עם מידע על המהירות
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bike Tracking Active")
                .setContentText(String.format("Current Speed: %.1f km/h", speedKmh))
                .setSmallIcon(R.drawable.electric1234)
                .setOngoing(true);

        // עדכון ההתראה הקיימת
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // החזרת עותק של היסטוריית המיקומים
    public List<Location> getLocationList() {
        return new ArrayList<>(locationList);
    }

    // ניקוי היסטוריית המיקומים
    public void clearLocations() {
        locationList.clear();
    }

    // החזרת מצב המעקב הנוכחי
    public boolean isTracking() {
        return isTracking;
    }

    // שירות זה אינו תומך בקשירה (binding)
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ניקוי כאשר השירות מושמד
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isTracking) {
            stopTracking();
        }
        instance = null;
    }
}