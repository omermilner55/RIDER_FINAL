package com.example.riderfinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// מחלקת פעילות ראשית המציגה את דף הבית של האפליקציה
public class HomeActivity extends AppCompatActivity {

    // הגדרת משתנים - שם המשתמש הנוכחי וכפתורים לניווט בממשק המשתמש
    private String currentUsername;
    ImageButton detailsButton, shopButton, historyButton, homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // הפעלת אפקטים ויזואליים של מסגרת האפליקציה מקצה לקצה
        EdgeToEdge.enable(this);
        // טעינת קובץ העיצוב של מסך הבית
        setContentView(R.layout.activity_home);

        // שליפת מידע המשתמש המחובר מזיכרון המכשיר
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String email = preferences.getString("useremail", "");
        // המרת כתובת הדוא"ל לשם משתמש באמצעות פונקציית עזר
        currentUsername = OmerUtils.getUserByEmail(this, email).getUserName();

        // יצירה והצגה של מסך הבית הראשי באמצעות מערכת הפרגמנטים
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new HomeScreenFragment());
        fragmentTransaction.commit();

        // הגדרת הלוגו בראש המסך
        TextView logo = findViewById(R.id.logo);

        // הגדרת כפתור הפרופיל והתפריט הנפתח שלו
        detailsButton = findViewById(R.id.DetailsStButton);
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // פתיחת תפריט אפשרויות המשתמש בלחיצה על כפתור הפרופיל
                showUserPopupMenu(v);
            }
        });

        // הגדרת כפתור החנות וניווט למסך החנות בלחיצה
        shopButton = findViewById(R.id.shopButton);
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // החלפת התוכן המרכזי למסך החנות
                replaceFragment(new ShoppingFragment());
                // עדכון הכותרת בהתאם למסך הנוכחי
                logo.setText(" SHOP ");
            }
        });

        // הגדרת כפתור הבית וניווט למסך הבית בלחיצה
        homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // החלפת התוכן המרכזי למסך הבית
                replaceFragment(new HomeScreenFragment());
                // עדכון הכותרת בהתאם למסך הנוכחי
                logo.setText(" RIDER ");
            }
        });

        // הגדרת כפתור ההיסטוריה וניווט למסך היסטוריית הנסיעות בלחיצה
        historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // החלפת התוכן המרכזי למסך ההיסטוריה
                replaceFragment(new RideHistoryFragment());
                // עדכון הכותרת בהתאם למסך הנוכחי
                logo.setText(" HISTORY ");
            }
        });
    }

    // פונקציה ליצירת תפריט משתמש קופץ (פופאפ)
    private void showUserPopupMenu(View anchorView) {
        // יצירת אובייקט תפריט קופץ
        PopupMenu popupMenu = new PopupMenu(this, anchorView);

        // הוספת פריטי תפריט
        // הצגת שם המשתמש המחובר - לא ניתן ללחוץ עליו (רק להצגה)
        popupMenu.getMenu().add(0, 0, 0, "Online: " +  currentUsername).setEnabled(false);
        // אפשרות להתנתקות מהמערכת
        popupMenu.getMenu().add(0, 1, 2, "Log Out");

        // הגדרת פעולות בעת לחיצה על פריטי התפריט
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 1) { // בחירה בפריט "התנתק"
                    // הפעלת פונקציית התנתקות
                    logout();
                    return true;
                }
                return false;
            }
        });

        // הצגת התפריט למשתמש
        popupMenu.show();
    }

    // פונקציה לביצוע התנתקות מהמערכת
    private void logout() {
        // גישה למאגר הנתונים המקומי של האפליקציה
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        // מחיקת כל הנתונים השמורים של המשתמש
        editor.clear();
        // שמירת השינויים
        editor.apply();

        // הצגת הודעה למשתמש על תהליך ההתנתקות
        Toast.makeText(this, "מתנתק...", Toast.LENGTH_SHORT).show();

        // מעבר למסך ההתחברות
        Intent intent = new Intent(this, LoginPage.class);
        // סימון דגלים למחיקת כל המסכים הקודמים מהזיכרון
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // הפעלת מעבר למסך התחברות
        startActivity(intent);
        // סגירת המסך הנוכחי
        finish();
    }

    // פונקציה להחלפת תוכן המסך באמצעות מערכת הפרגמנטים
    private void replaceFragment(Fragment fragment) {
        // החלפת הפרגמנט המוצג במיכל המרכזי
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // הוספה למחסנית החזרה - מאפשר למשתמש לחזור למסך הקודם
                .commit();
    }
}