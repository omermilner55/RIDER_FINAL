package com.example.riderfinal;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MustUseGPS extends AppCompatActivity {

    private Button enableGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_must_use_gps);

        enableGPS = findViewById(R.id.enableGPS);

        enableGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocationPermissionGranted()) {

                } else {
                    openAppSettings();
                }
            }
        });
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void navigateToHome() {
        Intent intent = new Intent(MustUseGPS.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // טוען SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String password = sharedPreferences.getString("password", "");

        // בדיקת הרשאות
        if (isLocationPermissionGranted()) {
            if (username.isEmpty() || password.isEmpty()) {
                // אין שם משתמש או סיסמה - מעבר למסך התחברות
                navigateToLogin();
            } else {
                // הרשאה ניתנה והמשתמש מחובר - מעבר למסך הבית
                navigateToHome();
            }
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MustUseGPS.this, LoginPage.class);
        startActivity(intent);
        finish();
    }
}