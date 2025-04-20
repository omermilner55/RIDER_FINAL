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

public class LoginPage extends AppCompatActivity {

    Button regibutton;
    Button Continue;
    EditText Email;
    EditText Pwd;
    CheckBox PasswordCheckbox;
    HelperDB helperDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        helperDb = new HelperDB(this);

        regibutton = findViewById(R.id.regibutton);
        regibutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPage.this, Register.class);
                startActivity(intent);
                finish();
            }
        });

        Email = findViewById(R.id.editlog);
        Pwd = findViewById(R.id.editlog1);

        Continue = findViewById(R.id.continue1);
        Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Email.getText().toString();
                String password = Pwd.getText().toString();

                // Basic input validation
                if (TextUtils.isEmpty(email)) {
                    Email.setError("Email is required");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Email.setError("Invalid email format");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Pwd.setError("Password is required");
                    return;
                }
                else {
                    boolean checkuser = OmerUtils.checkUser(LoginPage.this,email, password);
                    if (checkuser == true) {
                        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        // שמירת פרטי המשתמש
                        editor.putString("useremail", email);
                        editor.putString("password", password);

                        // שמירת הנתונים
                        editor.commit();
                        Intent intent = new Intent(LoginPage.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(LoginPage.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginPage.this, "Password or Email is incorrect!", Toast.LENGTH_SHORT).show();
                    }
                }



            }
        });

        PasswordCheckbox = findViewById(R.id.ShowpwdCheckbox);

        PasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                Pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            }
        });

    }

}


