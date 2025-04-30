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

// פעילות המוודאת שהרשאות GPS ניתנו לפני שמאפשרים גישה לאפליקציה
public class MustUseGPS extends AppCompatActivity {

    // כפתור להפעלת הרשאות GPS
    private Button enableGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // הפעלת תצוגה מקצה לקצה (מסך מלא)
        EdgeToEdge.enable(this);
        // הגדרת הפריסה לפעילות זו
        setContentView(R.layout.activity_must_use_gps);

        // אתחול כפתור enableGPS
        enableGPS = findViewById(R.id.enableGPS);

        // הגדרת מאזין לחיצה עבור כפתור ה-GPS
        enableGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocationPermissionGranted()) {
                    // ההרשאה כבר ניתנה, אך אין פעולה מוגדרת כאן
                    // זהו פער לוגי פוטנציאלי בקוד
                } else {
                    // ההרשאה לא ניתנה, פתיחת הגדרות האפליקציה כדי לאפשר למשתמש להעניק אותה
                    openAppSettings();
                }
            }
        });
    }

    // בדיקה אם הרשאת המיקום ניתנה
    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // ניווט למסך הבית
    private void navigateToHome() {
        Intent intent = new Intent(MustUseGPS.this, HomeActivity.class);
        startActivity(intent);
        finish(); // סגירת פעילות זו
    }

    // פתיחת דף הגדרות האפליקציה כדי לאפשר למשתמש להעניק הרשאות
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    // נקרא כאשר הפעילות הופכת לגלויה למשתמש
    @Override
    protected void onResume() {
        super.onResume();

        // טעינת פרטי המשתמש מ-SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String useremail = sharedPreferences.getString("useremail", "");
        String userpassword = sharedPreferences.getString("password", "");

        // בדיקת סטטוס ההרשאה
        if (isLocationPermissionGranted()) {
            if (useremail.isEmpty() || userpassword.isEmpty()) {
                // אין פרטי משתמש שמורים - ניווט למסך התחברות
                navigateToLogin();
            } else {
                // ההרשאה ניתנה ופרטי המשתמש קיימים - ניווט למסך הבית
                navigateToHome();
            }
        }
        // אם ההרשאה לא ניתנה, נשאר במסך זה כדי לבקש מהמשתמש
    }

    // ניווט למסך ההתחברות
    private void navigateToLogin() {
        Intent intent = new Intent(MustUseGPS.this, LoginPage.class);
        startActivity(intent);
        finish(); // סגירת פעילות זו
    }
}