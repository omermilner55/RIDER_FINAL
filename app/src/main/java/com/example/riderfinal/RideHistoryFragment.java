package com.example.riderfinal;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

            helperDB = new HelperDB(getContext());
            SQLiteDatabase db = helperDB.getReadableDatabase();
            recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            TextView logo = view.findViewById(R.id.logo);

            rideList = OmerUtils.getAllRidesSortedByDate(getContext());
            Toast.makeText(getContext(), "Number of rides: " + rideList.size(), Toast.LENGTH_SHORT).show();
            adapter = new RideAdapter(rideList, requireContext());
            recyclerView.setAdapter(adapter);

    // For regular click
        adapter.setOnItemClickListener(position -> {
            if (position >= 0 && position < rideList.size()) {
                Ride ride = rideList.get(position);

                RideDetailsFragment detailsFragment = new RideDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("ride", ride);
                detailsFragment.setArguments(bundle);

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, detailsFragment);
                transaction.addToBackStack(null);
                transaction.commit();


            }
        });

        adapter.setOnItemLongClickListener(position -> {
            if (position >= 0 && position < rideList.size()) {
                Ride ride = rideList.get(position);
                showDeleteConfirmationDialog(ride, position);
            }

        });

            return view;
        }
    private void showDeleteConfirmationDialog(Ride ride, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Ride")
                .setMessage("Are you sure you want to delete this ride?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // First delete from database
                    OmerUtils.deleteRide(getContext(),ride.getRideId());

                    // Then remove from list and update adapter
                    rideList.remove(position);
                    adapter.notifyDataSetChanged(); // Use this instead of notifyItemRemoved
                })
                .setNegativeButton("No", null)
                .show();
    }
}