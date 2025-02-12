package com.example.riderfinal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private ArrayList<Ride> rideList;
    private Context context;
    private OnItemClickListener clickListener; // Listener ללחיצה על פריט
    private OnItemLongClickListener longClickListener; // Listener ללחיצה ארוכה

    // Constructor
    public RideAdapter(ArrayList<Ride> rideList, Context context) {
        this.rideList = rideList;
        this.context = context;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);

        // הגדרת נתונים לתצוגה
        holder.dateTextView.setText(ride.getDate());
        holder.distanceTextView.setText(ride.getDistance());
        holder.DurationTextView.setText(ride.getDuration());
        holder.TimeTextView.setText(ride.getTime());
        String mapImagePath = ride.getMapImagePath();
        if (mapImagePath != null && !mapImagePath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(mapImagePath);
            holder.mapImageView1.setImageBitmap(bitmap);
        } else {
            holder.mapImageView1.setImageResource(android.R.drawable.ic_menu_camera); // תמונה ברירת מחדל
        }

        // Listener ללחיצה על פריט
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(position);
            }
        });

        // Listener ללחיצה ארוכה על פריט
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    // Interface לטיפול בלחיצה על פריט
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    // Interface לטיפול בלחיצה ארוכה על פריט
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    // ViewHolder
    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, distanceTextView, DurationTextView, TimeTextView;
        ImageView mapImageView, mapImageView1;
        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            DurationTextView = itemView.findViewById(R.id.DurationTextView);
            TimeTextView = itemView.findViewById(R.id.TimeTextView);
            mapImageView = itemView.findViewById(R.id.mapImageView);
            mapImageView1 = itemView.findViewById(R.id.mapImageView1);
        }
    }
}