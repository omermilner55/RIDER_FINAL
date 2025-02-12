
package com.example.riderfinal;

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

public class LocationTrackingService extends Service {
    private static final String CHANNEL_ID = "BikeTrackingChannel";
    private static final int NOTIFICATION_ID = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    public static List<Location> locationList = new ArrayList<>();
    private boolean isTracking = false;
    private static LocationTrackingService instance;
    public static List<LatLng> points = new ArrayList<>();
    public static Polyline polyline;
    public static LocationTrackingService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        locationList = new ArrayList<>();
        initLocationTracking();
        createNotificationChannel();
    }

    private void initLocationTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdateDelayMillis(1000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
             //   Toast.makeText(getApplicationContext(),
                       // "Location update received in service", Toast.LENGTH_SHORT).show();

                for (Location location : locationResult.getLocations()) {
                    locationList.add(location);
                 //   Toast.makeText(getApplicationContext(),
                        //    "New Location: " + location.getLatitude(), Toast.LENGTH_SHORT).show();

                    if (polyline != null) {
                        points = polyline.getPoints();
                        points.add(new LatLng(location.getLatitude(), location.getLongitude()));
                        polyline.setPoints(points);
                    }
                    updateNotification(location);

                    @SuppressLint("UnsafeImplicitIntentLaunch") Intent intent = new Intent("location_update");
                    intent.putExtra("location", location);
                    sendBroadcast(intent);
                 //   Toast.makeText(getApplicationContext(),
                       //     "Broadcast sent from service", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bike Tracking Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

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
        return START_STICKY;
    }

    private void startTracking() {
        if (!isTracking) {
          //  Toast.makeText(this, "Starting location tracking", Toast.LENGTH_SHORT).show();
            startForeground(NOTIFICATION_ID, buildNotification());
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        getMainLooper());
                isTracking = true;
              //  Toast.makeText(this, "Location updates requested", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
              //  Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void stopTracking() {
        isTracking = false;
        fusedLocationClient.removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bike Tracking Active")
                .setContentText("Recording your ride...")
                .setSmallIcon(R.drawable.electric1234)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(Location location) {
        if (!isTracking) return;

        float speedKmh = location.getSpeed() * 3.6f;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bike Tracking Active")
                .setContentText(String.format("Current Speed: %.1f km/h", speedKmh))
                .setSmallIcon(R.drawable.electric1234)
                .setOngoing(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public List<Location> getLocationList() {
        return new ArrayList<>(locationList);
    }

    public void clearLocations() {
        locationList.clear();
    }

    public boolean isTracking() {
        return isTracking;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isTracking) {
            stopTracking();
        }
        instance = null;
    }
}