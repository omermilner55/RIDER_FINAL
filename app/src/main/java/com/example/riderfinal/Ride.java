package com.example.riderfinal;

import java.io.Serializable;

// מחלקה המייצגת רכיבת אופניים שהמשתמש ביצע
public class Ride implements Serializable {
    // מאפייני הרכיבה
    private int rideId, ridePoints;              // מזהה רכיבה ונקודות שנצברו
    private String date, time;                   // תאריך ושעת התחלה
    private String distance, duration, avgSpeed; // מרחק, משך זמן ומהירות ממוצעת
    private String startLocation, endLocation;   // מיקום התחלה וסיום
    private String mapImagePath;                 // נתיב לתמונת מפת המסלול

    // בנאי המחלקה
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

    // פונקציות גישה (Getters)
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