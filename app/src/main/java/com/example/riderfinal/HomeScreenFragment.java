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

public class HomeScreenFragment extends Fragment implements OnMapReadyCallback {

    private ImageButton StartStop;
    private TextView Timertxt, DistanceTxt, SpeedTxt, PointsTxt;
    private Timer timer;
    private TimerTask timerTask;
    private long startTime = 0;
    private int rideID1;
    private HelperDB helperDB;
    private GoogleMap googleMap;
    private boolean isCameraFollowing = true;
    private boolean isPlaying = false;

    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("location_update")) {
                Location location = intent.getParcelableExtra("location");
                if (location != null) {
                    locationList.add(location);
                    OmerUtils.updateValuesInUi(locationList, isPlaying, DistanceTxt, SpeedTxt, PointsTxt);


                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                        // שימוש ב-animate במקום newLatLngZoom לתנועה חלקה יותר
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));

                        // עדכון ה-Polyline
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
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);
        StartStop = view.findViewById(R.id.StartStop);
        Timertxt = view.findViewById(R.id.Timertxt);
        DistanceTxt = view.findViewById(R.id.DistanceTxt);
        SpeedTxt = view.findViewById(R.id.SpeedTxt);
        PointsTxt = view.findViewById(R.id.PointsTxt);
        helperDB = new HelperDB(requireContext());
        FrameLayout frameLayout = requireActivity().findViewById(R.id.fragment_container);
            StartStop.setOnClickListener(v -> {
                if (isPlaying) {
                    StartStop.setBackgroundResource(R.drawable.startbut);
                    ImageButton profilebt = requireActivity().findViewById(R.id.ProfileStButton);
                    profilebt.setVisibility(View.VISIBLE);
                    stopRideTracking();
                    OmerUtils.updateRideDataInDatabase(requireContext(), helperDB, rideID1, locationList, startTime);
                    stopTimer();
                    OmerUtils.changeFragmentLayout(frameLayout, 2000);
                    openLastRideDetailsWithDelay();
                    OmerUtils.resetUi(Timertxt, DistanceTxt, SpeedTxt, PointsTxt);
                    locationList.clear();
                } else {
                    locationList.clear();
                    startRideTracking();
                    rideID1 = OmerUtils.getNextRideId(getContext());
                    startTimer();
                    new Handler().postDelayed(() -> {
                        if (isAdded()) {
                            OmerUtils.insertNewRideToDatabase(requireContext(), helperDB, rideID1, locationList);
                        }
                    }, 4000);

                    polyline = OmerUtils.initializePolyline(googleMap);
                    ImageButton profilebt = requireActivity().findViewById(R.id.ProfileStButton);
                    profilebt.setVisibility(View.INVISIBLE);
                    OmerUtils.changeFragmentLayout(frameLayout, LinearLayout.LayoutParams.MATCH_PARENT);
                    StartStop.setBackgroundResource(R.drawable.stopbut);
                }
                isPlaying = !isPlaying;
            });


        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    private void startRideTracking() {
        Toast.makeText(requireContext(), "Starting ride tracking", Toast.LENGTH_SHORT).show();
        isCameraFollowing = true;
        OmerUtils.resetPointsTracking();
        Intent serviceIntent = new Intent(requireContext(), LocationTrackingService.class);
        serviceIntent.setAction("START_TRACKING");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Toast.makeText(requireContext(),
                    "Starting foreground service", Toast.LENGTH_SHORT).show();
            requireContext().startForegroundService(serviceIntent);
        } else {
            Toast.makeText(requireContext(),
                    "Starting regular service", Toast.LENGTH_SHORT).show();
            requireContext().startService(serviceIntent);
        }

        IntentFilter filter = new IntentFilter("location_update");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(locationReceiver, filter,
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(locationReceiver, filter);
        }
        Toast.makeText(requireContext(),
                "Receiver registered", Toast.LENGTH_SHORT).show();
    }

    private void stopRideTracking() {
        Intent serviceIntent = new Intent(requireContext(), LocationTrackingService.class);
        serviceIntent.setAction("STOP_TRACKING");
        requireContext().startService(serviceIntent);

        try {
            requireContext().unregisterReceiver(locationReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        OmerUtils.saveMapSnapshot(googleMap, locationList, requireContext(), helperDB, rideID1);
    }



    private void startTimer() {
        startTime = System.currentTimeMillis();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        long seconds = (elapsedTime / 1000) % 60;
                        long minutes = (elapsedTime / (1000 * 60)) % 60;
                        long hours = (elapsedTime / (1000 * 60 * 60)) % 24;
                        Timertxt.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
                    });
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            timerTask = null;
        }
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        Toast.makeText(requireContext(), "Map is ready", Toast.LENGTH_SHORT).show();

        // הגדרות ממשק המשתמש של המפה
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);// אופציונלי - מוסיף כפתורי זום
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL ); // סוג המפה


        try {
            // אפשור שכבת המיקום
            googleMap.setMyLocationEnabled(true);
            Toast.makeText(requireContext(), "Location layer enabled", Toast.LENGTH_SHORT).show();

            // אם יש מיקומים קיימים ברשימה
            if (!locationList.isEmpty()) {
                Location lastLocation = locationList.get(locationList.size() - 1);
                LatLng currentLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());


                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f));
                Toast.makeText(requireContext(), "Moving to last location: " +
                        lastLocation.getLatitude() + ", " + lastLocation.getLongitude(), Toast.LENGTH_SHORT).show();

            }
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), "Please enable location permissions", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



    @Override
    public void onResume() {
        super.onResume();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(
                        locationReceiver,
                        new IntentFilter("location_update"),
                        Context.RECEIVER_EXPORTED
                );
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


    @Override
    public void onPause() {
        super.onPause();
        try {
            requireContext().unregisterReceiver(locationReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        stopRideTracking();
    }


    private void openLastRideDetailsWithDelay() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Ride Completed")
                .setMessage("Opening ride details in 5 seconds...")
                .setCancelable(false)
                .create();

        dialog.show();

        new Handler().postDelayed(() -> {
            if (isAdded()) {
                if (polyline != null) {
                    polyline.remove();
                }
                dialog.dismiss();  // סגירת הדיאלוג

                SQLiteDatabase db = helperDB.getReadableDatabase();
                String query = "SELECT * FROM " + RIDES_TABLE + " WHERE " + RIDE_ID + " = ?";
                Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(rideID1)});

                if (cursor.moveToFirst()) {
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

                    RideDetailsFragment detailsFragment = new RideDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("ride", currentRide);
                    detailsFragment.setArguments(bundle);

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, detailsFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    startRideTracking();
                }
                cursor.close();
                db.close();
            }
        }, 5000);
    }
}
