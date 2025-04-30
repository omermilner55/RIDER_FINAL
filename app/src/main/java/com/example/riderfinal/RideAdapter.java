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

    // מתאם להצגת רשימת רכיבות בתוך RecyclerView
    public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

        private ArrayList<Ride> rideList;            // רשימת הרכיבות להצגה
        private Context context;                      // הקשר האפליקציה
        private OnItemClickListener clickListener;    // מאזין ללחיצה על פריט
        private OnItemLongClickListener longClickListener; // מאזין ללחיצה ארוכה

        // בנאי
        public RideAdapter(ArrayList<Ride> rideList, Context context) {
            this.rideList = rideList;
            this.context = context;
        }

        @NonNull
        @Override
        public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // יצירת תצוגת פריט חדשה
            View view = LayoutInflater.from(context).inflate(R.layout.item_ride, parent, false);
            return new RideViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
            // קבלת הרכיבה הנוכחית לפי המיקום ברשימה
            Ride ride = rideList.get(position);

            // הגדרת נתונים לתצוגה
            holder.dateTextView.setText(ride.getDate());
            holder.distanceTextView.setText(ride.getDistance());
            holder.DurationTextView.setText(ride.getDuration());
            holder.TimeTextView.setText(ride.getTime());

            // טעינת תמונת המפה אם קיימת
            String mapImagePath = ride.getMapImagePath();
            if (mapImagePath != null && !mapImagePath.isEmpty()) {
                Bitmap bitmap = BitmapFactory.decodeFile(mapImagePath);
                holder.mapImageView1.setImageBitmap(bitmap);
            } else {
                holder.mapImageView1.setImageResource(android.R.drawable.ic_menu_camera); // תמונת ברירת מחדל
            }

            // הגדרת מאזין ללחיצה על פריט
            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(position);
                }
            });

            // הגדרת מאזין ללחיצה ארוכה על פריט
            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(position);
                }
                return true;
            });
        }

        @Override
        public int getItemCount() {
            // החזרת מספר הפריטים ברשימה
            return rideList.size();
        }

        // ממשק לטיפול בלחיצה על פריט
        public interface OnItemClickListener {
            void onItemClick(int position);
        }

        // הגדרת מאזין ללחיצה על פריט
        public void setOnItemClickListener(OnItemClickListener listener) {
            this.clickListener = listener;
        }

        // ממשק לטיפול בלחיצה ארוכה על פריט
        public interface OnItemLongClickListener {
            void onItemLongClick(int position);
        }

        // הגדרת מאזין ללחיצה ארוכה על פריט
        public void setOnItemLongClickListener(OnItemLongClickListener listener) {
            this.longClickListener = listener;
        }

        // מחזיק התצוגה (ViewHolder) - מחזיק את כל רכיבי הממשק לכל פריט
        public static class RideViewHolder extends RecyclerView.ViewHolder {
            TextView dateTextView, distanceTextView, DurationTextView, TimeTextView;
            ImageView mapImageView, mapImageView1;

            public RideViewHolder(@NonNull View itemView) {
                super(itemView);
                // קישור רכיבי הממשק
                dateTextView = itemView.findViewById(R.id.dateTextView);
                distanceTextView = itemView.findViewById(R.id.distanceTextView);
                DurationTextView = itemView.findViewById(R.id.DurationTextView);
                TimeTextView = itemView.findViewById(R.id.TimeTextView);
                mapImageView = itemView.findViewById(R.id.mapImageView);
                mapImageView1 = itemView.findViewById(R.id.mapImageView1);
            }
        }
    }