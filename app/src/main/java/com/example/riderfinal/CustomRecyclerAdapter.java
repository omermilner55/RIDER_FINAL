package com.example.riderfinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {

    private final String[] items;
    private final int[] itemsImage;
    private OnRewardClickListener listener;

    // Interface for click event callback
    public interface OnRewardClickListener {
        void onRewardClick(int position);
    }

    // Original constructor
    public CustomRecyclerAdapter(String[] items, int[] itemsImage) {
        this.items = items;
        this.itemsImage = itemsImage;
        this.listener = null;
    }

    // New constructor with click listener
    public CustomRecyclerAdapter(String[] items, int[] itemsImage, OnRewardClickListener listener) {
        this.items = items;
        this.itemsImage = itemsImage;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(items[position]);
        holder.imageView.setImageResource(itemsImage[position]);

        // Set click listener
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onRewardClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.itemText);
            imageView = itemView.findViewById(R.id.itemImage);
        }
    }
}