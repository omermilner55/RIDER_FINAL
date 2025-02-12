package com.example.riderfinal;

import androidx.annotation.NonNull;


public class UsersRides {

    private String rideId;
    private String rideDate;
    private String rideTime;
    private String rideStartLocation;
    private String rideEndLocation;
    private String rideDistance;
    private String rideDuration;
    private String rideAvgSpeed;
    private String rideImage;

    public UsersRides(String rideAvgSpeed, String rideDate, String rideDistance, String rideDuration, String rideEndLocation, String rideId, String rideStartLocation, String rideTime, String rideImage) {
        this.rideAvgSpeed = rideAvgSpeed;
        this.rideDate = rideDate;
        this.rideDistance = rideDistance;
        this.rideDuration = rideDuration;
        this.rideEndLocation = rideEndLocation;
        this.rideId = rideId;
        this.rideStartLocation = rideStartLocation;
        this.rideTime = rideTime;
        this.rideImage = rideImage;
    }

    public String getRideAvgSpeed() {
        return rideAvgSpeed;
    }

    public void setRideAvgSpeed(String rideAvgSpeed) {
        this.rideAvgSpeed = rideAvgSpeed;
    }

    public String getRideDate() {
        return rideDate;
    }

    public void setRideDate(String rideDate) {
        this.rideDate = rideDate;
    }

    public String getRideDistance() {
        return rideDistance;
    }

    public void setRideDistance(String rideDistance) {
        this.rideDistance = rideDistance;
    }

    public String getRideDuration() {
        return rideDuration;
    }

    public void setRideDuration(String rideDuration) {
        this.rideDuration = rideDuration;
    }

    public String getRideEndLocation() {
        return rideEndLocation;
    }

    public void setRideEndLocation(String rideEndLocation) {
        this.rideEndLocation = rideEndLocation;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getRideStartLocation() {
        return rideStartLocation;
    }

    public void setRideStartLocation(String rideStartLocation) {
        this.rideStartLocation = rideStartLocation;
    }

    public String getRideTime() {
        return rideTime;
    }

    public void setRideTime(String rideTime) {
        this.rideTime = rideTime;
    }

    public String getRideImage() {
        return rideImage;
    }

    public void setRideImage(String rideImage) {
        this.rideImage = rideImage;
    }

    @NonNull
    @Override
    public String toString() {
        return "UsersRides{" +
                "rideAvgSpeed=" + rideAvgSpeed +
                ", rideId='" + rideId + '\'' +
                ", rideDate=" + rideDate +
                ", rideTime='" + rideTime + '\'' +
                ", rideStartLocation='" + rideStartLocation + '\'' +
                ", rideEndLocation='" + rideEndLocation + '\'' +
                ", rideDistance=" + rideDistance +
                ", rideDuration=" + rideDuration +
                ", rideImage=" + rideImage +
                '}';
    }
}
