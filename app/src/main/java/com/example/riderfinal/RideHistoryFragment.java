package com.example.riderfinal;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RideHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private RideAdapter adapter;
    private ArrayList<Ride> rideList;
    private HelperDB helperDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        helperDB = new HelperDB(getContext());
        rideList = helperDB.getAllRidesSortedByDate();

        adapter = new RideAdapter(rideList, getContext());
        recyclerView.setAdapter(adapter);

        // שלב 4: הגדרת קליק לפריט ב-RecyclerView
        adapter.setOnItemClickListener(position -> {
            Ride ride = rideList.get(position);

            RideDetailsFragment detailsFragment = new RideDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("ride", ride); // העברת הרכיבה כפרמטר
            detailsFragment.setArguments(bundle);

            // החלפת פרגמנט
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, detailsFragment);
            transaction.addToBackStack(null); // מאפשר חזרה לפרגמנט הקודם
            transaction.commit();
        });

        // Handle long click to delete
        adapter.setOnItemLongClickListener(position -> {
            Ride ride = rideList.get(position);
            showDeleteConfirmationDialog(ride, position);
        });

        return view;
    }

    private void showDeleteConfirmationDialog(Ride ride, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Ride")
                .setMessage("Are you sure you want to delete this ride?")
                .setPositiveButton("Yes", (dialog, which) -> {;
                    rideList.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton("No", null)
                .show();
    }
}