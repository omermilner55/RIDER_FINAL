package com.example.riderfinal;

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
import static com.example.riderfinal.LocationTrackingService.locationList;
import static com.example.riderfinal.LocationTrackingService.points;
import static com.example.riderfinal.LocationTrackingService.polyline;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

// פרגמנט מסך הבית - מציג את המפה ומאפשר התחלת וסיום נסיעה
public class HomeScreenFragment extends Fragment implements OnMapReadyCallback {

    // הגדרת משתנים לרכיבי הממשק
    private ImageButton StartStop; // כפתור להתחלת/עצירת נסיעה
    private TextView Timertxt, DistanceTxt, SpeedTxt, PointsTxt; // שדות טקסט להצגת מידע על הנסיעה
    private Timer timer; // טיימר למדידת זמן הנסיעה
    private TimerTask timerTask; // משימת הטיימר שתרוץ בכל פעימה
    private long startTime = 0; // זמן התחלת הנסיעה
    private int rideID1; // מזהה הנסיעה הנוכחית
    private HelperDB helperDB; // עזר לגישה למסד הנתונים
    private GoogleMap googleMap; // אובייקט המפה
    private boolean isCameraFollowing = true; // האם המצלמה עוקבת אחרי המיקום
    private boolean isPlaying = false; // האם יש נסיעה פעילה כרגע

    // מקלט לעדכוני מיקום מהשירות
    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("location_update")) {
                // קבלת עדכון מיקום חדש מהשירות
                Location location = intent.getParcelableExtra("location");
                if (location != null) {
                    // הוספת המיקום החדש לרשימת המיקומים
                    locationList.add(location);
                    // עדכון ערכי המרחק, המהירות והנקודות בממשק המשתמש
                    OmerUtils.updateValuesInUi(locationList, isPlaying, DistanceTxt, SpeedTxt, PointsTxt);

                    // המרת המיקום לקואורדינטות למפה
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // הזזת המצלמה למיקום החדש עם אנימציה חלקה
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));

                    // עדכון קו המסלול על המפה
                    if (polyline != null) {
                        List<LatLng> points = polyline.getPoints();
                        points.add(latLng);
                        polyline.setPoints(points);
                    }
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // יצירת מבנה הפרגמנט מקובץ העיצוב
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);

        // קישור רכיבי הממשק למשתנים
        StartStop = view.findViewById(R.id.StartStop);
        Timertxt = view.findViewById(R.id.Timertxt);
        DistanceTxt = view.findViewById(R.id.DistanceTxt);
        SpeedTxt = view.findViewById(R.id.SpeedTxt);
        PointsTxt = view.findViewById(R.id.PointsTxt);

        // יצירת מופע של מסד הנתונים
        helperDB = new HelperDB(requireContext());

        // קבלת מיכל הפרגמנט הראשי מהפעילות המארחת
        FrameLayout frameLayout = requireActivity().findViewById(R.id.fragment_container);

        // הגדרת פעולת לחיצה על כפתור התחל/עצור
        StartStop.setOnClickListener(v -> {
            if (isPlaying) {
                // אם הנסיעה פעילה - עצור אותה
                // שינוי הכפתור חזרה לסגנון "התחל"
                StartStop.setBackgroundResource(R.drawable.startbut);
                // הצגת כפתור הפרופיל מחדש
                ImageButton profilebt = requireActivity().findViewById(R.id.DetailsStButton);
                profilebt.setVisibility(View.VISIBLE);
                // עצירת מעקב המיקום
                stopRideTracking();
                // שמירת נתוני הנסיעה במסד הנתונים
                OmerUtils.updateRideDataInDatabase(requireContext(), helperDB, rideID1, locationList, startTime);
                // עצירת הטיימר
                stopTimer();
                // שינוי גודל הפרגמנט לתצוגה רגילה
                OmerUtils.changeFragmentLayout(frameLayout, 2200);
                // פתיחת מסך פרטי הנסיעה האחרונה לאחר השהייה
                openLastRideDetailsWithDelay();
                // איפוס תצוגת הערכים בממשק
                OmerUtils.resetUi(Timertxt, DistanceTxt, SpeedTxt, PointsTxt);
                // ניקוי רשימת המיקומים
                locationList.clear();
            } else {
                // אם הנסיעה אינה פעילה - התחל נסיעה חדשה
                // ניקוי רשימת המיקומים הקודמת
                locationList.clear();
                // התחלת מעקב אחר המיקום
                startRideTracking();
                // קבלת מזהה חדש לנסיעה
                rideID1 = OmerUtils.getNextRideId(getContext());
                // התחלת הטיימר למדידת זמן
                startTimer();
                // השהייה קצרה לפני הכנסת נסיעה חדשה למסד הנתונים
                new Handler().postDelayed(() -> {
                    if (isAdded()) {
                        OmerUtils.insertNewRideToDatabase(requireContext(), helperDB, rideID1, locationList);
                    }
                }, 4000);

                // יצירת קו מסלול חדש על המפה
                polyline = OmerUtils.initializePolyline(googleMap);
                // הסתרת כפתור הפרופיל בזמן נסיעה
                ImageButton profilebt = requireActivity().findViewById(R.id.DetailsStButton);
                profilebt.setVisibility(View.INVISIBLE);
                // הגדלת הפרגמנט למסך מלא
                OmerUtils.changeFragmentLayout(frameLayout, LinearLayout.LayoutParams.MATCH_PARENT);
                // שינוי הכפתור לסגנון "עצור"
                StartStop.setBackgroundResource(R.drawable.stopbut);
            }
            // החלפת מצב הנסיעה
            isPlaying = !isPlaying;
        });

        // אתחול פרגמנט המפה
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            // בקשה להכנת המפה (תפעיל את onMapReady כשהמפה מוכנה)
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    // התחלת מעקב אחר מיקום הנסיעה
    private void startRideTracking() {
       // Toast.makeText(requireContext(), "Starting ride tracking", Toast.LENGTH_SHORT).show();
        // הפעלת מעקב מצלמה אחרי המיקום
        isCameraFollowing = true;
        // איפוס מעקב הנקודות
        OmerUtils.resetPointsTracking();
        // יצירת כוונה להפעלת שירות המיקום
        Intent serviceIntent = new Intent(requireContext(), LocationTrackingService.class);
        serviceIntent.setAction("START_TRACKING");

        // בדיקת גרסת אנדרואיד להפעלת השירות בצורה המתאימה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // בגרסאות חדשות יש להפעיל כשירות קדמי
          //  Toast.makeText(requireContext(),
                 //   "Starting foreground service", Toast.LENGTH_SHORT).show();
            requireContext().startForegroundService(serviceIntent);
        } else {
            // בגרסאות ישנות ניתן להפעיל כשירות רגיל
         //   Toast.makeText(requireContext(),
               //     "Starting regular service", Toast.LENGTH_SHORT).show();
            requireContext().startService(serviceIntent);
        }

        // הרשמה למקלט עדכוני המיקום
        IntentFilter filter = new IntentFilter("location_update");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // רישום מקלט בגרסה חדשה של אנדרואיד
            requireContext().registerReceiver(locationReceiver, filter,
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            // רישום מקלט בגרסה ישנה של אנדרואיד
            requireContext().registerReceiver(locationReceiver, filter);
        }
      //  Toast.makeText(requireContext(),
             //   "Receiver registered", Toast.LENGTH_SHORT).show();
    }

    // עצירת מעקב אחר מיקום הנסיעה
    private void stopRideTracking() {
        // יצירת כוונה לעצירת שירות המיקום
        Intent serviceIntent = new Intent(requireContext(), LocationTrackingService.class);
        serviceIntent.setAction("STOP_TRACKING");
        requireContext().startService(serviceIntent);

        // ביטול הרשמה למקלט עדכוני המיקום
        try {
            requireContext().unregisterReceiver(locationReceiver);
        } catch (IllegalArgumentException e) {
            // טיפול במקרה שהמקלט כבר אינו רשום
            e.printStackTrace();
        }
        // שמירת צילום מפה של המסלול
        OmerUtils.saveMapSnapshot(googleMap, locationList, requireContext(), helperDB, rideID1);
    }

    // התחלת טיימר למדידת זמן הנסיעה
    private void startTimer() {
        // שמירת זמן ההתחלה
        startTime = System.currentTimeMillis();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    // הפעלת קוד עדכון הממשק בשרשרת הראשית של האפליקציה
                    getActivity().runOnUiThread(() -> {
                        // חישוב הזמן שחלף
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        long seconds = (elapsedTime / 1000) % 60;
                        long minutes = (elapsedTime / (1000 * 60)) % 60;
                        long hours = (elapsedTime / (1000 * 60 * 60)) % 24;
                        // עדכון תצוגת הזמן בפורמט שעות:דקות:שניות
                        Timertxt.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
                    });
                }
            }
        };
        // תזמון הפעלת המשימה כל שנייה
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    // עצירת טיימר מדידת הזמן
    private void stopTimer() {
        if (timer != null) {
            // ביטול הטיימר וניקוי המשתנים
            timer.cancel();
            timer = null;
            timerTask = null;
        }
    }

    // מופעל כאשר המפה מוכנה לשימוש
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        //Toast.makeText(requireContext(), "Map is ready", Toast.LENGTH_SHORT).show();

        // הגדרות ממשק המשתמש של המפה
        googleMap.getUiSettings().setMyLocationButtonEnabled(true); // הפעלת כפתור המיקום
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // הגדרת סוג המפה לרגילה

        try {
            // הפעלת שכבת המיקום הנוכחי על המפה
            googleMap.setMyLocationEnabled(true);
          //  Toast.makeText(requireContext(), "Location layer enabled", Toast.LENGTH_SHORT).show();

            // אם יש מיקומים קיימים ברשימה, מעבר למיקום האחרון
            if (!locationList.isEmpty()) {
                Location lastLocation = locationList.get(locationList.size() - 1);
                LatLng currentLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                // הזזת המפה למיקום האחרון עם אנימציה
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f));
              //  Toast.makeText(requireContext(), "Moving to last location: " +
                     //   lastLocation.getLatitude() + ", " + lastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            // טיפול במקרה של הרשאות חסרות
            Toast.makeText(requireContext(), "Please enable location permissions", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), MustUseGPS.class);
            startActivity(intent);
            e.printStackTrace();
        }
    }

    // מופעל כאשר הפרגמנט חוזר להיות מוצג
    @Override
    public void onResume() {
        super.onResume();

        // רישום מחדש של מקלט עדכוני המיקום בהתאם לגרסת אנדרואיד
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                    locationReceiver,
                    new IntentFilter("location_update"),
                    Context.RECEIVER_EXPORTED
            );
            // שחזור נקודות המסלול על המפה אם קיימות
            if (polyline != null) {
                points = polyline.getPoints();
                Toast.makeText(requireContext(), String.valueOf(points), Toast.LENGTH_SHORT).show();
                polyline.setPoints(points);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().registerReceiver(locationReceiver, new IntentFilter("location_update"), Context.RECEIVER_EXPORTED);
            }
        }
    }

    // מופעל כאשר הפרגמנט יוצא מהמסך
    @Override
    public void onPause() {
        super.onPause();
        // ביטול רישום מקלט עדכוני המיקום
        try {
            requireContext().unregisterReceiver(locationReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    // מופעל כאשר הפרגמנט מושמד
    @Override
    public void onDestroy() {
        super.onDestroy();
        // ניקוי משאבים - ביטול הטיימר אם פעיל
        if (timer != null) {
            timer.cancel();
        }
        // עצירת מעקב המיקום
        stopRideTracking();
    }

    // פתיחת פרטי הנסיעה האחרונה לאחר השהייה
    private void openLastRideDetailsWithDelay() {
        // בדיקה שהנסיעה נשמרה כראוי במסד הנתונים עם כל הנתונים הנדרשים
        SQLiteDatabase dbCheck = helperDB.getReadableDatabase();
        String checkQuery = "SELECT * FROM " + RIDES_TABLE + " WHERE " + RIDE_ID + " = ? AND " +
                RIDE_DISTANCE + " IS NOT NULL AND " +
                RIDE_DURATION + " IS NOT NULL AND " +
                RIDE_AVG_SPEED + " IS NOT NULL AND " +
                RIDE_END_LOCATION + " IS NOT NULL";

        Cursor checkCursor = dbCheck.rawQuery(checkQuery, new String[]{String.valueOf(rideID1)});

        // בדיקה אם הנסיעה קיימת וכל הנתונים הקריטיים קיימים
        if (checkCursor.moveToFirst()) {
            // הנתונים תקינים - הצגת דיאלוג ומעבר למסך פרטי הנסיעה
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Ride Completed")
                    .setMessage("Opening ride details in 5 seconds...")
                    .setCancelable(false)
                    .create();

            dialog.show();

            // השהייה של 5 שניות לפני מעבר למסך פרטי הנסיעה
            new Handler().postDelayed(() -> {
                if (isAdded()) {
                    // מחיקת קו המסלול מהמפה
                    if (polyline != null) {
                        polyline.remove();
                    }
                    dialog.dismiss();  // סגירת הדיאלוג

                    // שליפת פרטי הנסיעה ממסד הנתונים
                    SQLiteDatabase db = helperDB.getReadableDatabase();
                    String query = "SELECT * FROM " + RIDES_TABLE + " WHERE " + RIDE_ID + " = ?";
                    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(rideID1)});

                    if (cursor.moveToFirst()) {
                        // יצירת אובייקט נסיעה מהנתונים במסד הנתונים
                        @SuppressLint("Range") Ride currentRide = new Ride(
                                cursor.getString(cursor.getColumnIndex(RIDE_AVG_SPEED)),
                                cursor.getString(cursor.getColumnIndex(RIDE_DATE)),
                                cursor.getString(cursor.getColumnIndex(RIDE_DISTANCE)),
                                cursor.getString(cursor.getColumnIndex(RIDE_DURATION)),
                                cursor.getString(cursor.getColumnIndex(RIDE_END_LOCATION)),
                                cursor.getString(cursor.getColumnIndex(RIDE_TRUCK_IMG)),
                                cursor.getInt(cursor.getColumnIndex(RIDE_ID)),
                                cursor.getInt(cursor.getColumnIndex(RIDE_POINTS)),
                                cursor.getString(cursor.getColumnIndex(RIDE_START_LOCATION)),
                                cursor.getString(cursor.getColumnIndex(RIDE_TIME))
                        );

                        // הכנת פרגמנט פרטי נסיעה והעברת אובייקט הנסיעה אליו
                        RideDetailsFragment detailsFragment = new RideDetailsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("ride", currentRide);
                        detailsFragment.setArguments(bundle);

                        // החלפת הפרגמנט הנוכחי בפרגמנט פרטי הנסיעה
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, detailsFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                    // התחלת מעקב מיקום מחדש
                    startRideTracking();
                    cursor.close();
                    db.close();
                }
            }, 5000);
        } else {
            // במקרה של שגיאה בשמירת הנסיעה - הצגת דיאלוג שגיאה עם כפתור OK לסגירת האפליקציה
            new AlertDialog.Builder(requireContext())
                    .setTitle("An error occurred while saving the ride")
                    .setMessage("The application will restart. Please open it again and start a new ride.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // מחיקת נתוני הנסיעה השגויים
                        OmerUtils.deleteRide(requireContext(), rideID1);
                        // סגירת האפליקציה לגמרי
                        requireActivity().finishAffinity();
                        // במקרה הצורך, ניתן להוסיף גם את השורה הבאה לסגירה מוחלטת
                        System.exit(0);
                    })
                    .setCancelable(false)
                    .show();
        }
        checkCursor.close();
        dbCheck.close();
    }
}