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

public class HomeActivity extends AppCompatActivity {

    private String currentUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // השג את שם המשתמש המחובר (לדוגמה, מ-SharedPreferences)
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String email = preferences.getString("useremail", "");
        currentUsername = OmerUtils.getUserByEmail(this, email).getUserName();

        // אתחול הפרגמנט הראשי (HomeScreen)
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new HomeScreenFragment());
        fragmentTransaction.commit();
        TextView logo = findViewById(R.id.logo);

        // כפתור הפרופיל עם התפריט הנפתח
        ImageButton profileButton = findViewById(R.id.ProfileStButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // הצג את התפריט הנפתח
                showUserPopupMenu(v);
            }
        });
        // כפתור ה-Shop
        ImageButton shopButton = findViewById(R.id.shopButton);
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מעבר ל-Fragment של החנות
                replaceFragment(new ShoppingFragment());
                logo.setText(" SHOP ");
            }
        });

        // כפתור ה-Home
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מעבר ל-Fragment של הבית
                replaceFragment(new HomeScreenFragment());
                logo.setText(" RIDER ");
            }
        });

        // כפתור ההיסטוריה
        ImageButton historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new RideHistoryFragment());
                logo.setText(" HISTORY ");
            }
        });

    }

    // פונקציה להצגת תפריט המשתמש הנפתח
    private void showUserPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);

        // יצירת התפריט
        popupMenu.getMenu().add(0, 0, 0, "Online: " +  currentUsername).setEnabled(false);
        popupMenu.getMenu().add(0, 1, 2, "Log Out");

        // טיפול באירועי לחיצה על פריטי התפריט
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 1) { // האפשרות "התנתק"
                    logout();
                    return true;
                }
                return false;
            }
        });

        // הצגת התפריט
        popupMenu.show();
    }

    // פונקציה להתנתקות המשתמש
    private void logout() {
        // מחיקת פרטי המשתמש מה-SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        // מחיקת כל הנתונים במידת הצורך
        editor.clear();

        editor.apply();

        Toast.makeText(this, "מתנתק...", Toast.LENGTH_SHORT).show();

        // מעבר למסך ההתחברות
        Intent intent = new Intent(this, LoginPage.class);
        // הוסף דגלים כדי לנקות את ה-activity stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    // פונקציה כללית להחלפת פרגמנט
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // מאפשר חזרה אחורה
                .commit();
    }
}