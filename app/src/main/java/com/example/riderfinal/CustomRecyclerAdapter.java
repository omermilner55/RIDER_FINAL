package com.example.riderfinal;

// ייבוא של הספריות הנדרשות
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// מחלקת מתאם מותאמת אישית עבור RecyclerView
public class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {

    // מאגר נתונים פרטי למתאם
    private final String[] items;         // מערך של מחרוזות לתצוגה
    private final int[] itemsImage;       // מערך של מזהי תמונות לתצוגה
    private OnRewardClickListener listener; // מאזין לאירועי לחיצה

    // ממשק להעברת אירועי לחיצה חזרה לפעילות המארחת
    public interface OnRewardClickListener {
        void onRewardClick(int position); // מתודה שתופעל בעת לחיצה על פריט
    }

    // בנאי מקורי ללא תמיכה באירועי לחיצה
    public CustomRecyclerAdapter(String[] items, int[] itemsImage) {
        this.items = items;       // אתחול מערך המחרוזות
        this.itemsImage = itemsImage; // אתחול מערך התמונות
        this.listener = null;     // אין מאזין לאירועים
    }

    // בנאי חדש עם תמיכה באירועי לחיצה
    public CustomRecyclerAdapter(String[] items, int[] itemsImage, OnRewardClickListener listener) {
        this.items = items;       // אתחול מערך המחרוזות
        this.itemsImage = itemsImage; // אתחול מערך התמונות
        this.listener = listener; // הגדרת המאזין לאירועים
    }

    // יצירת מחזיק תצוגה חדש כאשר נדרש (כאשר מנהל הפריסה זקוק לו)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ניפוח פריסת הפריט מקובץ XML
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        // החזרת מחזיק תצוגה חדש שעוטף את תצוגת הפריט
        return new ViewHolder(view);
    }

    // קישור נתונים למחזיק תצוגה בעמדה מסוימת
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // הגדרת הטקסט עבור הפריט הנוכחי
        holder.textView.setText(items[position]);
        // הגדרת התמונה עבור הפריט הנוכחי
        holder.imageView.setImageResource(itemsImage[position]);

        // הגדרת מאזין לחיצה אם הוא קיים
        if (listener != null) {
            // הגדרת אירוע לחיצה שיפעיל את המאזין עם מיקום הפריט
            holder.itemView.setOnClickListener(v -> listener.onRewardClick(position));
        }
    }

    // החזרת הגודל הכולל של מערך הנתונים
    @Override
    public int getItemCount() {
        return items.length; // מספר הפריטים במערך
    }

    // מחלקה פנימית סטטית שמחזיקה את רכיבי התצוגה של כל פריט
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;   // רכיב להצגת טקסט
        ImageView imageView; // רכיב להצגת תמונה

        // בנאי למחזיק התצוגה
        public ViewHolder(@NonNull View itemView) {
            super(itemView); // קריאה לבנאי של המחלקה האב
            // מציאת רכיבי התצוגה בפריסת הפריט לפי מזהה
            textView = itemView.findViewById(R.id.itemText);
            imageView = itemView.findViewById(R.id.itemImage);
        }
    }
}