package com.example.riderfinal;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// מחלקה לטיפול בהרשמת משתמשים חדשים
public class Register extends AppCompatActivity {

    // רכיבי ממשק המשתמש
    private Button Continue, Back;  // כפתורי המשך וחזרה
    private EditText Username, Email, Pwd, ReType, Phonenum;  // שדות הקלט
    private CheckBox PasswordCheckbox;  // תיבת סימון להצגת הסיסמה
    private HelperDB helperDB;  // מחלקת עזר למסד הנתונים

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // הפעלת תצוגת קצה לקצה (ללא שוליים)
        EdgeToEdge.enable(this);
        // הגדרת היסה למסך זה
        setContentView(R.layout.activity_register);

        // אתחול מסד הנתונים
        helperDB = new HelperDB(this);

        // אתחול רכיבי הממשק
        Username = findViewById(R.id.editregi1);
        Email = findViewById(R.id.editregi2);
        Pwd = findViewById(R.id.editregi3);
        ReType = findViewById(R.id.editregi4);
        Phonenum = findViewById(R.id.editregi5);
        Continue = findViewById(R.id.continue2);
        Back = findViewById(R.id.backregi);
        PasswordCheckbox = findViewById(R.id.ShowpwdCheckbox);

        // הגדרת מאזין לחיצה לכפתור המשך
        Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();  // טיפול בתהליך ההרשמה
            }
        });

        // הגדרת מאזין לחיצה לכפתור חזרה
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // חזרה למסך ההתחברות
                Intent intent = new Intent(Register.this, LoginPage.class);
                startActivity(intent);
                finish();  // סגירת המסך הנוכחי
            }
        });

        // הגדרת מאזין לתיבת הסימון להצגת/הסתרת הסיסמה
        PasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // הצגת הסיסמה כטקסט רגיל
                Pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ReType.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // הסתרת הסיסמה עם נקודות
                Pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ReType.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        // טיפול בשוליים של המסך
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // טיפול בתהליך ההרשמה
    private void handleRegistration() {
        // קבלת ערכי הקלט מהמשתמש
        String username = Username.getText().toString().trim();
        String email = Email.getText().toString().trim();
        String pwd = Pwd.getText().toString();
        String retype = ReType.getText().toString();
        String phonenum = Phonenum.getText().toString().trim();

        // בדיקת תקינות הקלט
        if (!validateInput(username, email, pwd, retype, phonenum)) {
            return;  // אם הקלט לא תקין, עצור את התהליך
        }

        // בדיקה אם האימייל, שם המשתמש או מספר הטלפון כבר קיימים במערכת
        if (isFieldExists(HelperDB.USER_EMAIL, email)) {
            Email.setError("This email address already exists!");
            Email.requestFocus();
            return;
        }
        if (isFieldExists(HelperDB.USER_PHONE, phonenum)) {
            Phonenum.setError("This phone number already exists!");
            Phonenum.requestFocus();
            return;
        }
        if (isFieldExists(HelperDB.USER_NAME, username)) {
            Username.setError("This username already exists!");
            Username.requestFocus();
            return;
        }

        // הוספת המשתמש החדש למסד הנתונים
        if (registerUser(username, email, pwd, phonenum)) {
            Toast.makeText(Register.this, "Registration was successful!", Toast.LENGTH_SHORT).show();
            // שמירת פרטי המשתמש להתחברות אוטומטית
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", username);
            editor.putString("password", pwd);
            editor.putString("useremail", email);
            editor.apply();
            // מעבר למסך ההתחברות
            Intent intent = new Intent(Register.this, LoginPage.class);
            startActivity(intent);
            finish();  // סגירת המסך הנוכחי
        } else {
            Toast.makeText(Register.this, "Registration failed!", Toast.LENGTH_SHORT).show();
        }
    }

    // בדיקת תקינות הקלט של המשתמש
    private boolean validateInput(String username, String email, String pwd, String retype, String phonenum) {
        if (TextUtils.isEmpty(username)) {
            Username.setError("Username required");
            Username.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            Email.setError("Email required");
            Email.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Email.setError("The email format is incorrect");
            Email.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(pwd)) {
            Pwd.setError("Password required");
            Pwd.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(retype)) {
            ReType.setError("Password verification required");
            ReType.requestFocus();
            return false;
        }
        if (!pwd.equals(retype)) {
            ReType.setError("Passwords do not match");
            ReType.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(phonenum)) {
            Phonenum.setError("Phone Number required");
            Phonenum.requestFocus();
            return false;
        }
        return true;  // הקלט תקין
    }

    // בדיקה אם ערך מסוים כבר קיים במסד הנתונים
    private boolean isFieldExists(String column, String value) {
        SQLiteDatabase db = helperDB.getReadableDatabase();
        String query = "SELECT * FROM " + HelperDB.USERS_TABLE + " WHERE " + column + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{value});
        boolean exists = cursor.getCount() > 0;  // בדיקה אם נמצאו תוצאות
        cursor.close();
        db.close();
        return exists;
    }

    // רישום משתמש חדש במסד הנתונים
    private boolean registerUser(String username, String email, String pwd, String phonenum) {
        SQLiteDatabase db = helperDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        // הכנת הערכים להוספה
        values.put(HelperDB.USER_NAME, username);
        values.put(HelperDB.USER_EMAIL, email);
        values.put(HelperDB.USER_PWD, pwd);
        values.put(HelperDB.USER_PHONE, phonenum);

        // הוספת המשתמש למסד הנתונים
        long newRowId = db.insert(HelperDB.USERS_TABLE, null, values);
        db.close();
        return newRowId != -1;  // החזרת אמת אם ההוספה הצליחה
    }
}