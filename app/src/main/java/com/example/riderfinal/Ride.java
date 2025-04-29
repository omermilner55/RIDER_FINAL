package com.example.riderfinal;

import java.io.Serializable;

public class Ride implements Serializable {
    private int rideId, ridePoints;
    private String date, time, distance, duration, avgSpeed, startLocation, endLocation, mapImagePath;

    public Ride(String avgSpeed, String date, String distance, String duration, String endLocation, String mapImagePath, int rideId, int ridePoints, String startLocation, String time) {
        this.avgSpeed = avgSpeed;
        this.date = date;
        this.distance = distance;
        this.duration = duration;
        this.endLocation = endLocation;
        this.mapImagePath = mapImagePath;
        this.rideId = rideId;
        this.ridePoints = ridePoints;
        this.startLocation = startLocation;
        this.time = time;
    }

    // Getters and Setters
    public int getRideId() {
        return rideId;
    }

    public String getDate() { return date;}

    public String getTime() {
        return "Ride Start Time: " + time;
    }

    public String getDistance() {
        return "Ride Distance: " + distance;
    }

    public String getDuration() {
        return "Ride Duration: " + duration;
    }

    public String getAvgSpeed() {
        return "Ride Average Speed: " + avgSpeed;
    }

    public String getStartLocation() {
        return "Ride Start Location: " + startLocation;
    }

    public String getEndLocation() {
        return "Ride End Location: " + endLocation;
    }

    public String getMapImagePath() {
        return mapImagePath;
    }

    public String getRidePoints() {
        return "Ride Points: " + ridePoints + " " + "pt";
    }

}