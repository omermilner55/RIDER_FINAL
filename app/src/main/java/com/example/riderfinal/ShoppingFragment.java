package com.example.riderfinal;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShoppingFragment extends Fragment implements CustomRecyclerAdapter.OnRewardClickListener {

    private User user;
    private int[] rewardIds = {1, 2, 3, 4, 5}; // Store reward IDs for reference

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        TextView yourpoints = view.findViewById(R.id.yourpoints);
        RecyclerView recyclerView = view.findViewById(R.id.itemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get current user from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String useremail = sharedPreferences.getString("useremail", "");

        user = OmerUtils.getUserByEmail(getContext(),useremail);
        yourpoints.setText("Your Points: " + user.getUserPoints() + " pt");

        // Get reward names for display
        String[] items = {
                OmerUtils.getRewardById(getContext(), rewardIds[0]).getRewardName(),
                OmerUtils.getRewardById(getContext(), rewardIds[1]).getRewardName(),
                OmerUtils.getRewardById(getContext(), rewardIds[2]).getRewardName(),
                OmerUtils.getRewardById(getContext(), rewardIds[3]).getRewardName(),
                OmerUtils.getRewardById(getContext(), rewardIds[4]).getRewardName()
        };

        // Fixed array size to match items array
        int[] itemsImage = {
                R.drawable.alien1234,
                R.drawable.healthshake,
                R.drawable.sports1234,
                R.drawable.hf1,
                R.drawable.electric1234
        };

        // Set adapter with click listener
        CustomRecyclerAdapter adapter = new CustomRecyclerAdapter(items, itemsImage, this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Click listener implementation - handle reward selection
    @Override
    public void onRewardClick(int position) {
        // Create new instance of RewardsDetails with reward ID
        RewardsDetails rewardsDetails = new RewardsDetails();

        // Pass reward ID as an argument
        Bundle args = new Bundle();
        args.putInt("reward_id", rewardIds[position]);
        rewardsDetails.setArguments(args);

        // Navigate to reward details fragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, rewardsDetails)
                .addToBackStack(null)
                .commit();
    }
}