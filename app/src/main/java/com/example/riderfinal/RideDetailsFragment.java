package com.example.riderfinal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class RideDetailsFragment extends Fragment {

    private TextView ridePointsTextView, dateTextView, timeTextView, distanceTextView, durationTextView, avgSpeedTextView, startLocationTextView, endLocationTextView;
    private ImageView mapImageView;
    private Ride ride;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_details, container, false);

        // קישור רכיבי UI
        dateTextView = view.findViewById(R.id.dateTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        distanceTextView = view.findViewById(R.id.distanceTextView);
        durationTextView = view.findViewById(R.id.durationTextView);
        avgSpeedTextView = view.findViewById(R.id.avgSpeedTextView);
        startLocationTextView = view.findViewById(R.id.startLocationTextView);
        endLocationTextView = view.findViewById(R.id.endLocationTextView);
        ridePointsTextView = view.findViewById(R.id.ridePointsTextView);
        mapImageView = view.findViewById(R.id.mapImageView);

        // קבלת הנתונים
        if (getArguments() != null) {
            ride = (Ride) getArguments().getSerializable("ride");
            populateRideDetails();
        }

        return view;
    }

    private void populateRideDetails() {
        dateTextView.setText(ride.getDate());
        timeTextView.setText(ride.getTime());
        distanceTextView.setText(ride.getDistance());
        durationTextView.setText(ride.getDuration());
        avgSpeedTextView.setText(ride.getAvgSpeed());
        startLocationTextView.setText(ride.getStartLocation());
        endLocationTextView.setText(ride.getEndLocation());
        ridePointsTextView.setText(ride.getRidePoints());

        // טעינת תמונת המסלול
        String mapImagePath = ride.getMapImagePath();
        if (mapImagePath != null && !mapImagePath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(mapImagePath);
            mapImageView.setImageBitmap(bitmap);
        } else {
            mapImageView.setImageResource(android.R.drawable.ic_menu_camera); // תמונה ברירת מחדל
        }
    }
}