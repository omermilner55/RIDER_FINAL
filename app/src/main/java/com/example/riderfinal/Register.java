package com.example.riderfinal;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Register extends AppCompatActivity {

    private Button Continue, Back;
    private EditText Username, Email, Pwd, ReType, Phonenum;
    private HelperDB helperDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        helperDB = new HelperDB(this);

        // Initialize views
        Username = findViewById(R.id.editregi1);
        Email = findViewById(R.id.editregi2);
        Pwd = findViewById(R.id.editregi3);
        ReType = findViewById(R.id.editregi4);
        Phonenum = findViewById(R.id.editregi5);
        Continue = findViewById(R.id.continue2);
        Back = findViewById(R.id.backregi);

        // Continue button click listener
        Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });

        // Back button click listener
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, LoginPage.class);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void handleRegistration() {
        String username = Username.getText().toString().trim();
        String email = Email.getText().toString().trim();
        String pwd = Pwd.getText().toString();
        String retype = ReType.getText().toString();
        String phonenum = Phonenum.getText().toString().trim();

        // Validate input
        if (!validateInput(username, email, pwd, retype, phonenum)) {
            return;
        }

        // Check if email, username, or phone already exists
        if (isFieldExists(HelperDB.USER_EMAIL, email)) {
            Email.setError("Email already exists!");
            Email.requestFocus();
            return;
        }
        if (isFieldExists(HelperDB.USER_PHONE, phonenum)) {
            Phonenum.setError("Phone Number already exists!");
            Phonenum.requestFocus();
            return;
        }
        if (isFieldExists(HelperDB.USER_NAME, username)) {
            Username.setError("Username already exists!");
            Username.requestFocus();
            return;
        }

        // Insert new user into the database
        if (registerUser(username, email, pwd, phonenum)) {
            Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
            // Store credentials for auto login
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", username);
            editor.putString("password", pwd);
            editor.putString("useremail", email);
            editor.apply();
            Intent intent = new Intent(Register.this, LoginPage.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(Register.this, "Registration failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String username, String email, String pwd, String retype, String phonenum) {
        if (TextUtils.isEmpty(username)) {
            Username.setError("Username is required");
            Username.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            Email.setError("Email is required");
            Email.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Email.setError("Invalid email format");
            Email.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(pwd)) {
            Pwd.setError("Password is required");
            Pwd.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(retype)) {
            ReType.setError("Retype Password is required");
            ReType.requestFocus();
            return false;
        }
        if (!pwd.equals(retype)) {
            ReType.setError("Passwords do not match");
            ReType.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(phonenum)) {
            Phonenum.setError("Phone Number is required");
            Phonenum.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isFieldExists(String column, String value) {
        SQLiteDatabase db = helperDB.getReadableDatabase();
        String query = "SELECT * FROM " + HelperDB.USERS_TABLE + " WHERE " + column + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{value});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    private boolean registerUser(String username, String email, String pwd, String phonenum) {
        SQLiteDatabase db = helperDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HelperDB.USER_NAME, username);
        values.put(HelperDB.USER_EMAIL, email);
        values.put(HelperDB.USER_PWD, pwd);
        values.put(HelperDB.USER_PHONE, phonenum);

        long newRowId = db.insert(HelperDB.USERS_TABLE, null, values);
        db.close();
        return newRowId != -1;
    }
}