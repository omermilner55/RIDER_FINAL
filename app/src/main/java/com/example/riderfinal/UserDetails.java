package com.example.riderfinal;

public class UserDetails {

    private String userName;
    private String userEmail;
    private String userPwd;
    private String userRetype;
    private String userPhone;

    public UserDetails(String userName, String userEmail, String userPwd, String userRetype, String userPhone) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPwd = userPwd;
        this.userRetype = userRetype;
        this.userPhone = userPhone;
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

    @Override
    public String toString() {
        return "UserDetails{" +
                "userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPwd='" + userPwd + '\'' +
                ", userRetype='" + userRetype + '\'' +
                ", userPhone=" + userPhone +
                '}';
    }
}
