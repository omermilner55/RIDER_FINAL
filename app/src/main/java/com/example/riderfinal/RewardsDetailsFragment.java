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

// פרגמנט המציג פרטים מלאים של פרס ומאפשר רכישה שלו
public class RewardsDetailsFragment extends Fragment {

    // משתני המחלקה
    private Reward reward;               // אובייקט הפרס המוצג
    private HelperDB helperDB;           // עזר למסד הנתונים
    private User user;                   // המשתמש הנוכחי
    private String useremail;            // אימייל המשתמש הנוכחי
    private TextView rewardPointsText;   // טקסט נקודות הפרס
    private TextView statusMessage;      // הודעת סטטוס
    private Button purchaseButton;       // כפתור רכישה

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // ניפוח הפריסה לפרגמנט
        View view = inflater.inflate(R.layout.fragment_rewards_details, container, false);

        // אתחול עזר מסד הנתונים
        helperDB = new HelperDB(requireContext());

        // קבלת מזהה הפרס מהארגומנטים
        int rewardId = 1; // ברירת מחדל
        if (getArguments() != null) {
            rewardId = getArguments().getInt("reward_id", 1);
        }

        // קבלת המשתמש הנוכחי מה-SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        useremail = sharedPreferences.getString("useremail", "");
        user = OmerUtils.getUserByEmail(getContext(),useremail);

        // קבלת פרטי הפרס ממסד הנתונים
        reward = OmerUtils.getRewardById(getContext(),rewardId);

        // אתחול רכיבי ממשק המשתמש
        TextView titleText = view.findViewById(R.id.title);
        TextView detailsText = view.findViewById(R.id.RewardDetails);
        TextView descText = view.findViewById(R.id.RewardDesc);
        TextView rewardCode = view.findViewById(R.id.RewardCode);
        TextView rewardCodeTitle = view.findViewById(R.id.RewardCodeTitle);
        rewardPointsText = view.findViewById(R.id.rewardPoints);
        statusMessage = view.findViewById(R.id.statusMessage);
        purchaseButton = view.findViewById(R.id.purchaseButton);

        // מילוי ממשק המשתמש בנתוני הפרס
        if (reward != null) {
            // הגדרת כותרת
            titleText.setText(reward.getRewardName());

            // הגדרת פרטי הפרס - שימוש בשם הפרס ככותרת משנה
            detailsText.setText(reward.getRewardName());

            // הגדרת תיאור הפרס
            descText.setText(reward.getRewardDescription());

            // הגדרת נקודות הפרס
            rewardPointsText.setText("Required amount of points: " + reward.getRewardPointsPrice());

            // בדיקה אם המשתמש כבר רכש את הפרס הזה
            boolean alreadyPurchased = OmerUtils.hasUserPurchasedReward(getContext(), user.getUserName(), reward.getRewardId());

            if (alreadyPurchased) {
                // המשתמש כבר רכש את הפרס, הצגת הקוד
                String savedCode = OmerUtils.getSavedRedemptionCode(getContext(), user.getUserName(),reward.getRewardId());
                rewardCode.setText(savedCode);
                rewardCodeTitle.setVisibility(View.VISIBLE);
                rewardCode.setVisibility(View.VISIBLE);
                purchaseButton.setVisibility(View.GONE);
                statusMessage.setText("You have purchased this reward!");
                statusMessage.setVisibility(View.VISIBLE);
            } else {
                // המשתמש עדיין לא רכש את הפרס
                rewardCodeTitle.setVisibility(View.GONE);
                rewardCode.setVisibility(View.GONE);

                // בדיקה אם למשתמש יש מספיק נקודות
                if (user.getUserPoints() >= reward.getRewardPointsPrice()) {
                    purchaseButton.setEnabled(true);
                    statusMessage.setText("You have enough points to purchase this reward!");
                } else {
                    purchaseButton.setEnabled(false);
                    statusMessage.setText("You don't have enough points to purchase this reward.");
                }
                statusMessage.setVisibility(View.VISIBLE);
                purchaseButton.setVisibility(View.VISIBLE);
            }

            // הגדרת מאזין לחיצה לכפתור הרכישה
            purchaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    purchaseReward();
                }
            });

            // טעינת תמונת הפרס - תלוי באופן שמירת התמונות
            try {
                ImageView rewardImage = view.findViewById(R.id.RewardItem);

                // אם תמונת הפרס נשמרת כשם משאב drawable
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
                // טיפול בשגיאת טעינת תמונה
            }
        }

        return view;
    }

    /**
     * טיפול ברכישת פרס
     */
    private void purchaseReward() {
        // בדיקה כפולה אם למשתמש יש מספיק נקודות
        if (user.getUserPoints() < reward.getRewardPointsPrice()) {
            Toast.makeText(requireContext(), "You don't have enough points!", Toast.LENGTH_SHORT).show();
            return;
        }

        // רכישת הפרס
        String redemptionCode = OmerUtils.purchaseReward(getContext(), useremail, reward.getRewardId());

        if (redemptionCode != null) {
            // הרכישה הצליחה
            // עדכון ממשק המשתמש
            TextView rewardCode = requireView().findViewById(R.id.RewardCode);
            TextView rewardCodeTitle = requireView().findViewById(R.id.RewardCodeTitle);

            rewardCode.setText(redemptionCode);
            rewardCodeTitle.setVisibility(View.VISIBLE);
            rewardCode.setVisibility(View.VISIBLE);
            purchaseButton.setVisibility(View.GONE);

            // עדכון הודעת סטטוס
            statusMessage.setText("You have successfully purchased this reward!");

            // שמירת נקודות המשתמש החדשות מקומית
            user.setUserPoints(user.getUserPoints() - reward.getRewardPointsPrice());

            // הצגת הודעת הצלחה
            Toast.makeText(requireContext(), "You have successfully purchased this reward!", Toast.LENGTH_SHORT).show();
        } else {
            // הרכישה נכשלה
            Toast.makeText(requireContext(), "An error occurred while purchasing the reward. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}