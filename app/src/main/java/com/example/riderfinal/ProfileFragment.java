package com.example.riderfinal;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private ImageButton AddPhoto;
    private ImageView ProfileImg;
    private String[] UserListView = {"Name", "Change UserName", "Change Password", "Phone Number", "Themes", "Log Out", "Delete Account"};
    private HelperDB helperDB;
    private ListView ListView;
    private TextView UserName;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize database and UI components
        helperDB = new HelperDB(requireContext());
        AddPhoto = view.findViewById(R.id.AddPhoto);
        ProfileImg = view.findViewById(R.id.ProfileImg);
        ListView = view.findViewById(R.id.UserListView);
        UserName = view.findViewById(R.id.Username);
        // Retrieve username from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String profileimg = sharedPreferences.getString("UserImageUri", "");
        UserName.setText(username);


        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Username not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return view; // Exit if username is missing
        }

        // Initialize camera and gallery launchers
        initLaunchers(username);

        // AddPhoto button click listener
        AddPhoto.setOnClickListener(v -> showImageSourceDialog());

        // Set up the user list view
        CustomListAdapter adapter = new CustomListAdapter(requireContext(), UserListView);
        ListView.setAdapter(adapter);


        return view;
    }

    private void initLaunchers(String username) {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                        if (imageBitmap != null) {
                            Uri imageUri = saveImageToGallery(imageBitmap);
                            if (imageUri != null) {
                                updateUserImageInDatabase(username, imageUri.toString());
                                ProfileImg.setImageURI(imageUri);
                            } else {
                                Toast.makeText(requireContext(), "Failed to save image.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            updateUserImageInDatabase(username, imageUri.toString());
                            ProfileImg.setImageURI(imageUri);
                        }
                    }
                });
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Choose Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Camera
                        if (checkPermission(Manifest.permission.CAMERA)) {
                            launchCamera();
                        } else {
                            requestPermission(Manifest.permission.CAMERA);
                        }
                    } else { // Gallery
                        launchGallery();
                    }
                })
                .show();
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission) {
        requestPermissions(new String[]{permission}, 1000);
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private Uri saveImageToGallery(Bitmap bitmap) {
        try {
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    requireContext().getContentResolver(),
                    bitmap,
                    "Profile Image",
                    "Image of profile"
            );
            return Uri.parse(savedImageURL);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateUserImageInDatabase(String username, String imageUri) {
        SQLiteDatabase db = helperDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HelperDB.USER_IMAGE_URI, imageUri);
        int rowsUpdated = db.update(HelperDB.USERS_TABLE, values, HelperDB.USER_NAME + " = ?", new String[]{username});

        if (rowsUpdated > 0) {
            Toast.makeText(requireContext(), "Profile image updated successfully.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to update profile image.", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }
}