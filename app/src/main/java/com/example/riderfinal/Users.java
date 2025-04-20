package com.example.riderfinal;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Users implements Serializable {

    private String userName;
    private String userEmail;
    private String userPwd;
    private String userRetype;
    private String userPhone;
    private int userPoints;
    private HelperDB helperDB;

    public Users(String userName, String userEmail, String userPwd, String userRetype, String userPhone, int userPoints) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPwd = userPwd;
        this.userRetype = userRetype;
        this.userPhone = userPhone;
        this.userPoints = userPoints;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getUserRetype() {
        return userRetype;
    }

    public void setUserRetype(String userRetype) {
        this.userRetype = userRetype;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public int getUserPoints() {
        return userPoints;
    }

    public void setUserPoints(int userPoints) {
        this.userPoints = userPoints;
    }

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