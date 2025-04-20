package com.example.riderfinal;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Reward implements Serializable {
    private int rewardId;
    private String rewardName;
    private String rewardImg;
    private int rewardPointsPrice;
    private String rewardDescription;

    public Reward(int rewardId, String rewardName, String rewardImg, int rewardPointsPrice, String rewardDescription) {
        this.rewardId = rewardId;
        this.rewardName = rewardName;
        this.rewardImg = rewardImg;
        this.rewardPointsPrice = rewardPointsPrice;
        this.rewardDescription = rewardDescription;
    }

    public int getRewardId() {
        return rewardId;
    }

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