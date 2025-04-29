package com.example.riderfinal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class RewardsDetails extends Fragment {

    private Reward reward;
    private HelperDB helperDB;
    private User user;
    private String useremail;
    private TextView rewardPointsText;
    private TextView statusMessage;
    private Button purchaseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rewards_details, container, false);

        // Initialize database helper
        helperDB = new HelperDB(requireContext());

        // Get reward ID from arguments
        int rewardId = 1; // Default
        if (getArguments() != null) {
            rewardId = getArguments().getInt("reward_id", 1);
        }

        // Get current user from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        useremail = sharedPreferences.getString("useremail", "");
        user = OmerUtils.getUserByEmail(getContext(),useremail);

        // Get reward from database
        reward = OmerUtils.getRewardById(getContext(),rewardId);

        // Initialize UI elements
        TextView titleText = view.findViewById(R.id.title);
        TextView detailsText = view.findViewById(R.id.RewardDetails);
        TextView descText = view.findViewById(R.id.RewardDesc);
        TextView rewardCode = view.findViewById(R.id.RewardCode);
        TextView rewardCodeTitle = view.findViewById(R.id.RewardCodeTitle);
        rewardPointsText = view.findViewById(R.id.rewardPoints);
        statusMessage = view.findViewById(R.id.statusMessage);
        purchaseButton = view.findViewById(R.id.purchaseButton);

        // Populate UI with reward data
        if (reward != null) {
            // Set title
            titleText.setText(reward.getRewardName());

            // Set reward details - using reward name as subtitle
            detailsText.setText(reward.getRewardName());

            // Set reward description
            descText.setText(reward.getRewardDescription());

            // Set reward points
            rewardPointsText.setText("Amount of Points required: " + reward.getRewardPointsPrice());

            // Check if user already purchased this reward
            boolean alreadyPurchased = OmerUtils.hasUserPurchasedReward(getContext(), user.getUserName(), reward.getRewardId());

            if (alreadyPurchased) {
                // User already has this reward, show the code
                String savedCode = OmerUtils.getSavedRedemptionCode(getContext(), user.getUserName(),reward.getRewardId());
                rewardCode.setText(savedCode);
                rewardCodeTitle.setVisibility(View.VISIBLE);
                rewardCode.setVisibility(View.VISIBLE);
                purchaseButton.setVisibility(View.GONE);
                statusMessage.setText("You have already purchased this reward!");
                statusMessage.setVisibility(View.VISIBLE);
            } else {
                // User hasn't purchased this reward yet
                rewardCodeTitle.setVisibility(View.GONE);
                rewardCode.setVisibility(View.GONE);

                // Check if user has enough points
                if (user.getUserPoints() >= reward.getRewardPointsPrice()) {
                    purchaseButton.setEnabled(true);
                    statusMessage.setText("You have enough points to purchase this reward!");
                } else {
                    purchaseButton.setEnabled(false);
                    statusMessage.setText("Theres no enough points to purchase this reward");
                }
                statusMessage.setVisibility(View.VISIBLE);
                purchaseButton.setVisibility(View.VISIBLE);
            }

            // Set purchase button click listener
            purchaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    purchaseReward();
                }
            });

            // Load reward image - this depends on how you store images
            try {
                ImageView rewardImage = view.findViewById(R.id.RewardItem);

                // If rewardImg is stored as a drawable resource name
                if (reward.getRewardImg() != null && !reward.getRewardImg().isEmpty()) {
                    int resId = getResources().getIdentifier(
                            reward.getRewardImg(),
                            "drawable",
                            requireContext().getPackageName()
                    );

                    if (resId != 0) {
                        rewardImage.setImageResource(resId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle image loading error
            }
        }

        return view;
    }

    /**
     * Handle reward purchase
     */
    private void purchaseReward() {
        // Double-check if user has enough points
        if (user.getUserPoints() < reward.getRewardPointsPrice()) {
            Toast.makeText(requireContext(), "You don't have enough points!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Purchase the reward
        String redemptionCode = OmerUtils.purchaseReward(getContext(), useremail, reward.getRewardId());

        if (redemptionCode != null) {
            // Purchase successful
            // Update UI
            TextView rewardCode = requireView().findViewById(R.id.RewardCode);
            TextView rewardCodeTitle = requireView().findViewById(R.id.RewardCodeTitle);

            rewardCode.setText(redemptionCode);
            rewardCodeTitle.setVisibility(View.VISIBLE);
            rewardCode.setVisibility(View.VISIBLE);
            purchaseButton.setVisibility(View.GONE);

            // Update status message
            statusMessage.setText("You have successfully purchased the reward!");

            // Save the new user points locally
            user.setUserPoints(user.getUserPoints() - reward.getRewardPointsPrice());

            // Show success message
            Toast.makeText(requireContext(), "You have successfully purchased the reward!", Toast.LENGTH_SHORT).show();
        } else {
            // Purchase failed
            Toast.makeText(requireContext(), "An error occurred while purchasing the reward. try again", Toast.LENGTH_SHORT).show();
        }
    }
}