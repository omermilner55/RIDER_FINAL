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

    // פרגמנט המציג היסטוריית רכיבות של המשתמש
    public class RideHistoryFragment extends Fragment {

        // רכיבי ממשק
        private RecyclerView recyclerView;            // רכיב תצוגת הרשימה
        private RideAdapter adapter;                  // מתאם הרשימה
        private ArrayList<Ride> rideList;             // רשימת הרכיבות להצגה

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // ניפוח פריסת הפרגמנט
            View view = inflater.inflate(R.layout.fragment_ride_history, container, false);

            // אתחול והגדרת הרסייקלר-ויו
            recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            // קבלת רשימת הרכיבות ממסד הנתונים
            rideList = OmerUtils.getAllRidesSortedByDate(getContext());
     //       Toast.makeText(getContext(), "מספר רכיבות: " + rideList.size(), Toast.LENGTH_SHORT).show();

            // יצירת המתאם והצמדתו לרסייקלר-ויו
            adapter = new RideAdapter(rideList, requireContext());
            recyclerView.setAdapter(adapter);

            // הגדרת מאזין ללחיצה רגילה על פריט
            adapter.setOnItemClickListener(position -> {
                if (position >= 0 && position < rideList.size()) {
                    Ride ride = rideList.get(position);

                    // יצירת פרגמנט פרטי רכיבה והעברת פרטי הרכיבה אליו
                    RideDetailsFragment detailsFragment = new RideDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("ride", ride);
                    detailsFragment.setArguments(bundle);

                    // החלפת הפרגמנט הנוכחי בפרגמנט פרטי הרכיבה
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, detailsFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });

            // הגדרת מאזין ללחיצה ארוכה על פריט (למחיקה)
            adapter.setOnItemLongClickListener(position -> {
                if (position >= 0 && position < rideList.size()) {
                    Ride ride = rideList.get(position);
                    showDeleteConfirmationDialog(ride, position);
                }
            });

            return view;
        }

        // הצגת דיאלוג אישור מחיקת רכיבה
        private void showDeleteConfirmationDialog(Ride ride, int position) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Ride")
                    .setMessage("Are you sure you want to delete this ride?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // קודם מחיקה ממסד הנתונים
                        OmerUtils.deleteRide(getContext(),ride.getRideId());

                        // לאחר מכן הסרה מהרשימה ועדכון המתאם
                        rideList.remove(position);
                        adapter.notifyDataSetChanged(); // שימוש בזה במקום notifyItemRemoved
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }