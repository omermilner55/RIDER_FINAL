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

// פרגמנט המציג את חנות הפרסים/הטבות שהמשתמש יכול לרכוש עם הנקודות שצבר
public class ShoppingFragment extends Fragment implements CustomRecyclerAdapter.OnRewardClickListener {

    private User user;                          // המשתמש הנוכחי
    private int[] rewardIds = {1, 2, 3, 4, 5};  // מערך מזהי הפרסים לשימוש

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ניפוח הפריסה לפרגמנט זה
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        TextView yourpoints = view.findViewById(R.id.yourpoints);
        RecyclerView recyclerView = view.findViewById(R.id.itemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // קבלת המשתמש הנוכחי מה-SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String useremail = sharedPreferences.getString("useremail", "");

        user = OmerUtils.getUserByEmail(getContext(),useremail);
        yourpoints.setText("Your points: " + user.getUserPoints() + " " + "pt");

        // קבלת שמות הפרסים להצגה
        String[] items = {
                OmerUtils.getRewardById(getContext(), rewardIds[0]).getRewardName(),
                OmerUtils.getRewardById(getContext(), rewardIds[1]).getRewardName(),
                OmerUtils.getRewardById(getContext(), rewardIds[2]).getRewardName(),
                OmerUtils.getRewardById(getContext(), rewardIds[3]).getRewardName(),
                OmerUtils.getRewardById(getContext(), rewardIds[4]).getRewardName()
        };

        // מערך תמונות קבוע שתואם את מערך הפריטים
        int[] itemsImage = {
                R.drawable.alien1234,
                R.drawable.healthshake,
                R.drawable.sports1234,
                R.drawable.hf1,
                R.drawable.electric1234
        };

        // הגדרת המתאם עם מאזין לחיצה
        CustomRecyclerAdapter adapter = new CustomRecyclerAdapter(items, itemsImage, this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // מימוש מאזין לחיצה - טיפול בבחירת פרס
    @Override
    public void onRewardClick(int position) {
        // יצירת מופע חדש של RewardsDetails עם מזהה הפרס
        RewardsDetailsFragment rewardsDetails = new RewardsDetailsFragment();

        // העברת מזהה הפרס כארגומנט
        Bundle args = new Bundle();
        args.putInt("reward_id", rewardIds[position]);
        rewardsDetails.setArguments(args);

        // ניווט לפרגמנט פרטי הפרס
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, rewardsDetails)
                .addToBackStack(null)
                .commit();
    }
}