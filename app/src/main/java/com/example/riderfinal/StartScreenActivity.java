package com.example.riderfinal;

import static com.example.riderfinal.LocationTrackingService.locationList;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class StartScreenActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("location_update")) {
                Location location = intent.getParcelableExtra("location");
                if (location != null) {
                    locationList.add(location);

                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        // בדיקת הרשאות מיקום
        if (!arePermissionsGranted()) {
            requestLocationPermission();
        } else {
            proceedWithAppFlow();
        }
    }

    // בדיקת האם כל ההרשאות הנדרשות קיימות
    private boolean arePermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // בקשת הרשאת מיקום
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "The app needs location permission to function properly.", Toast.LENGTH_LONG).show();
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    // המשך זרימת האפליקציה (לאחר מתן הרשאות או אם כבר קיימות)
    private void proceedWithAppFlow() {
        startTracking();
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                // יכול להישאר ריק אם אין צורך להציג משהו בזמן הספירה
            }

            @Override
            public void onFinish() {
                SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String useremail = sharedPreferences.getString("useremail", "");
                String userpassword = sharedPreferences.getString("password", "");

                if (useremail.isEmpty() || userpassword.isEmpty()) {
                    // אם אין פרטי התחברות שמורים, מעבר למסך התחברות
                    Intent intent = new Intent(StartScreenActivity.this, LoginPage.class);
                    startActivity(intent);
                } else {
                    // המשתמש כבר מחובר, המשך למסך הבית
                    Intent intent = new Intent(StartScreenActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
                finish(); // סיים את הפעולה של המסך הנוכחי
            }
        }.start();
    }
    private void startTracking() {
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        serviceIntent.setAction("START_TRACKING");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(serviceIntent);
        } else {
            this.startService(serviceIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.registerReceiver(
                    locationReceiver,
                    new IntentFilter("location_update"),
                    Context.RECEIVER_EXPORTED
            );
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.registerReceiver(locationReceiver, new IntentFilter("location_update"), Context.RECEIVER_EXPORTED);
            }
        }

    }

    // טיפול בתוצאות בקשת הרשאות
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ההרשאה התקבלה, המשך באפליקציה
                proceedWithAppFlow();
            } else {
                // ההרשאה נדחתה, העבר למסך שמבקש מהמשתמש להשתמש ב-GPS
                Intent intent = new Intent(StartScreenActivity.this, MustUseGPS.class);
                startActivity(intent);
                finish();
            }
        }
    }
}