package com.example.riderfinal;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        // אתחול הפרגמנט הראשי (HomeScreen)
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new HomeScreen());
        fragmentTransaction.commit();

        // כפתור ה-Shop
        ImageButton shopButton = findViewById(R.id.shopButton);
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מעבר ל-Fragment של החנות
                replaceFragment(new ShoppingFragment());
            }
        });

        // כפתור ה-Home
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מעבר ל-Fragment של הבית
                replaceFragment(new HomeScreen());
            }
        });

        // כפתור ההיסטוריה
        ImageButton historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new RideHistoryFragment());
            }
        });

        ImageButton ProfileStButton = findViewById(R.id.ProfileStButton);
        ProfileStButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new ProfileFragment());
            }
        });
    }

    // פונקציה כללית להחלפת פרגמנט
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // מאפשר חזרה אחורה
                .commit();
    }
}