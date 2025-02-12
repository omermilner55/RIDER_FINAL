package com.example.riderfinal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShoppingFragment extends Fragment {

    String[] items = {"20% discount for a subscription to the Alien Network", "ReShake drink size L", "25% discount in the Sports Hall", "JBJ headphones as a gift worth NIS 99", "15% discount at the electric king"};
    int[] itemsImage = {R.drawable.alien1234, R.drawable.sports1234, R.drawable.sports1234, R.drawable.hf1, R.drawable.electric1234};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.itemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set adapter
        CustomRecyclerAdapter adapter = new CustomRecyclerAdapter(items, itemsImage);
        recyclerView.setAdapter(adapter);

        return view;
    }
}