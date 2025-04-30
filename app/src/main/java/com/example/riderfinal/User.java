package com.example.riderfinal;

import androidx.annotation.NonNull;

import java.io.Serializable;

// מחלקה המייצגת משתמש באפליקציה
public class User implements Serializable {

    // מאפייני המשתמש
    private String userName;      // שם המשתמש
    private String userEmail;     // כתובת דוא"ל
    private String userPwd;       // סיסמה
    private String userRetype;    // אימות סיסמה (משמש בעיקר בתהליך הרשמה)
    private String userPhone;     // מספר טלפון
    private int userPoints;       // נקודות שצבר המשתמש ברכיבות

    // בנאי המחלקה
    public User(String userName, String userEmail, String userPwd, String userRetype, String userPhone, int userPoints) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPwd = userPwd;
        this.userRetype = userRetype;
        this.userPhone = userPhone;
        this.userPoints = userPoints;
    }

    // פונקציות גישה (Getters) ועדכון (Setters) למאפייני המשתמש

    // קבלת שם המשתמש
    public String getUserName() {
        return userName;
    }

    // עדכון שם המשתמש
    public void setUserName(String userName) {
        this.userName = userName;
    }

    // קבלת כתובת הדוא"ל
    public String getUserEmail() {
        return userEmail;
    }

    // עדכון כתובת הדוא"ל
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // קבלת הסיסמה
    public String getUserPwd() {
        return userPwd;
    }

    // עדכון הסיסמה
    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    // קבלת אימות הסיסמה
    public String getUserRetype() {
        return userRetype;
    }

    // עדכון אימות הסיסמה
    public void setUserRetype(String userRetype) {
        this.userRetype = userRetype;
    }

    // קבלת מספר הטלפון
    public String getUserPhone() {
        return userPhone;
    }

    // עדכון מספר הטלפון
    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    // קבלת מספר הנקודות
    public int getUserPoints() {
        return userPoints;
    }

    // עדכון מספר הנקודות
    public void setUserPoints(int userPoints) {
        this.userPoints = userPoints;
    }

    // דריסת מתודת toString לייצוג מחרוזתי של האובייקט
    @NonNull
    @Override
    public String toString() {
        return "Users{" +
                "userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPwd='" + userPwd + '\'' +
                ", userRetype='" + userRetype + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userPoints=" + userPoints +
                '}';
    }
}