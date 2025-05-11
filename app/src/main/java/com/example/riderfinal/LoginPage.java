package com.example.riderfinal;
import static java.security.AccessController.getContext;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// מסך התחברות המטפל באימות משתמשים
public class LoginPage extends AppCompatActivity {

    // הצהרה על רכיבי ממשק המשתמש
    Button regibutton;       // כפתור למעבר למסך ההרשמה
    Button Continue;         // כפתור לשליחת פרטי ההתחברות
    EditText Email;          // שדה קלט לדוא"ל
    EditText Pwd;            // שדה קלט לסיסמה
    CheckBox PasswordCheckbox;   // תיבת סימון להצגת/הסתרת הסיסמה
    HelperDB helperDb;       // מחלקת עזר למסד הנתונים

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // הגדרת הפריסה למסך זה
        setContentView(R.layout.activity_login_page);

        // אתחול מחלקת העזר למסד הנתונים
        helperDb = new HelperDB(this);

        // הגדרת כפתור ההרשמה ומאזין הלחיצות שלו
        regibutton = findViewById(R.id.regibutton);
        regibutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מעבר למסך ההרשמה
                Intent intent = new Intent(LoginPage.this, Register.class);
                startActivity(intent);
                // סגירת המסך הנוכחי כדי שהמשתמש לא יוכל לחזור עם כפתור חזרה
                finish();
            }
        });

        // איתור ואתחול שדות הקלט
        Email = findViewById(R.id.editlog);
        Pwd = findViewById(R.id.editlog1);

        // הגדרת כפתור ההתחברות ומאזין הלחיצות שלו
        Continue = findViewById(R.id.continue1);
        Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // קבלת הקלט מהמשתמש
                String email = Email.getText().toString();
                String password = Pwd.getText().toString();

                // בדיקת תקינות הקלט
                if (TextUtils.isEmpty(email)) {
                    Email.setError("Email required");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Email.setError("Invalid email format");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Pwd.setError("Password required");
                    return;
                }
                else {
                    // אימות הפרטים מול מסד הנתונים
                    boolean checkuser = OmerUtils.checkUser(LoginPage.this, email, password);
                    if (checkuser == true) {
                        // יצירת SharedPreferences לשמירת נתוני הפעלה של המשתמש
                        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        // שמירת פרטי המשתמש בהעדפות
                        editor.putString("useremail", email);
                        editor.putString("password", password);

                        // שמירת השינויים בהעדפות
                        editor.commit();

                        // התחברות מוצלחת, מעבר למסך הבית
                        Intent intent = new Intent(LoginPage.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(LoginPage.this, "Connection successful", Toast.LENGTH_SHORT).show();
                    } else {
                        // הצגת הודעת שגיאה עבור פרטים שגויים
                        Toast.makeText(LoginPage.this, "Incorrect password or email!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // הגדרת תיבת הסימון להצגת/הסתרת הסיסמה
        PasswordCheckbox = findViewById(R.id.ShowpwdCheckbox);
        PasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // הצגת הסיסמה כטקסט רגיל
                Pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // הסתרת הסיסמה עם נקודות
                Pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }
}