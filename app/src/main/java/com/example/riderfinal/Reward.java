package com.example.riderfinal;

import androidx.annotation.NonNull;

import java.io.Serializable;

// מחלקה המייצגת פרס/הטבה שהמשתמש יכול לקבל תמורת נקודות
public class Reward implements Serializable {
    // מאפייני הפרס
    private int rewardId;                // מזהה ייחודי של הפרס
    private String rewardName;           // שם הפרס
    private String rewardImg;            // נתיב לתמונת הפרס
    private int rewardPointsPrice;       // מחיר הפרס בנקודות
    private String rewardDescription;    // תיאור הפרס

    // בנאי המחלקה
    public Reward(int rewardId, String rewardName, String rewardImg, int rewardPointsPrice, String rewardDescription) {
        this.rewardId = rewardId;
        this.rewardName = rewardName;
        this.rewardImg = rewardImg;
        this.rewardPointsPrice = rewardPointsPrice;
        this.rewardDescription = rewardDescription;
    }

    // פונקציות גישה (Getters)
    public int getRewardId() {
        return rewardId;
    }

    // פונקציות עדכון (Setters)
    public void setRewardId(int rewardId) {
        this.rewardId = rewardId;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public String getRewardImg() {
        return rewardImg;
    }

    public void setRewardImg(String rewardImg) {
        this.rewardImg = rewardImg;
    }

    public int getRewardPointsPrice() {
        return rewardPointsPrice;
    }

    public void setRewardPointsPrice(int rewardPointsPrice) {
        this.rewardPointsPrice = rewardPointsPrice;
    }

    public String getRewardDescription() {
        return rewardDescription;
    }

    public void setRewardDescription(String rewardDescription) {
        this.rewardDescription = rewardDescription;
    }

    // מתודה המחזירה ייצוג טקסטואלי של האובייקט
    @NonNull
    @Override
    public String toString() {
        return "Reward{" +
                "rewardId=" + rewardId +
                ", rewardName='" + rewardName + '\'' +
                ", rewardImg='" + rewardImg + '\'' +
                ", rewardPointsPrice=" + rewardPointsPrice +
                ", rewardDescription='" + rewardDescription + '\'' +
                '}';
    }
}