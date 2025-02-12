package com.example.riderfinal;

import android.location.Location;

import java.util.List;

public class SpeedPointsCalculator {
    // קונסטנטות למהירות וחישוב נקודות
    private static final float MIN_SPEED_KMH = 20.0f;
    private static final float MAX_SPEED_KMH = 27.0f;
    private static final float SEGMENT_LENGTH = 50.0f;
    private static final int POINTS_PER_SEGMENT = 5;

    // קונסטנטות לבדיקות תקינות
    private static final float MIN_DISTANCE = 5.0f;  // מרחק מינימלי בין נקודות במטרים
    private static final long MIN_TIME_DIFF = 3000;  // זמן מינימלי בין נקודות במילישניות
    private static final long MAX_TIME_DIFF = 10000; // זמן מקסימלי בין נקודות במילישניות
    private static final float MIN_ACCURACY = 15.0f; // דיוק GPS מינימלי במטרים

    private Location lastValidLocation = null;
    private float distanceSinceLastPoint = 0.0f;
    private long lastPointsTime = 0;
    private int consecutiveValidSpeedCount = 0;
    private static final int REQUIRED_VALID_SPEEDS = 3; // כמה פעמים רצוף צריך להיות במהירות הנכונה

    public int calculateSpeedPoints(List<Location> locationList) {
        if (locationList.size() < 2) {
            android.util.Log.d("SpeedPoints", "Not enough locations");
            return 0;
        }

        int totalPoints = 0;

        for (int i = 1; i < locationList.size(); i++) {
            Location prevLocation = locationList.get(i-1);
            Location currentLocation = locationList.get(i);

            // בדיקות תקינות בסיסיות
            if (!isValidLocationPair(prevLocation, currentLocation)) {
                resetProgress("Invalid location pair");
                continue;
            }

            float distance = prevLocation.distanceTo(currentLocation);
            float timeDiffSeconds = (currentLocation.getTime() - prevLocation.getTime()) / 1000f;
            float speedKmh = (distance / timeDiffSeconds) * 3.6f;

            android.util.Log.d("SpeedPoints", String.format(
                    "Speed: %.2f km/h, Distance: %.2f m, Time: %.2f s, Accuracy: %.2f m",
                    speedKmh, distance, timeDiffSeconds, currentLocation.getAccuracy()));

            if (isSpeedInValidRange(speedKmh)) {
                consecutiveValidSpeedCount++;
                distanceSinceLastPoint += distance;

                if (consecutiveValidSpeedCount >= REQUIRED_VALID_SPEEDS &&
                        distanceSinceLastPoint >= SEGMENT_LENGTH &&
                        (currentLocation.getTime() - lastPointsTime) >= MIN_TIME_DIFF) {

                    totalPoints += POINTS_PER_SEGMENT;
                    lastPointsTime = currentLocation.getTime();
                    distanceSinceLastPoint = 0;

                    android.util.Log.d("SpeedPoints",
                            "Added points! Total: " + totalPoints +
                                    " ConsecutiveValid: " + consecutiveValidSpeedCount);
                }
            } else {
                resetProgress("Speed out of range: " + speedKmh);
            }
        }

        return totalPoints;
    }

    private boolean isValidLocationPair(Location prev, Location current) {
        // בדיקת דיוק GPS
        if (prev.getAccuracy() > MIN_ACCURACY || current.getAccuracy() > MIN_ACCURACY) {
            android.util.Log.d("SpeedPoints", "Poor GPS accuracy");
            return false;
        }

        float distance = prev.distanceTo(current);
        long timeDiff = current.getTime() - prev.getTime();

        // בדיקת מרחק מינימלי
        if (distance < MIN_DISTANCE) {
            android.util.Log.d("SpeedPoints", "Distance too small: " + distance);
            return false;
        }

        // בדיקת הפרשי זמן
        if (timeDiff < MIN_TIME_DIFF || timeDiff > MAX_TIME_DIFF) {
            android.util.Log.d("SpeedPoints", "Invalid time difference: " + timeDiff);
            return false;
        }

        return true;
    }

    private boolean isSpeedInValidRange(float speedKmh) {
        return speedKmh >= MIN_SPEED_KMH && speedKmh <= MAX_SPEED_KMH;
    }

    private void resetProgress(String reason) {
        android.util.Log.d("SpeedPoints", "Resetting progress: " + reason);
        consecutiveValidSpeedCount = 0;
        distanceSinceLastPoint = 0;
    }

    public void reset() {
        lastValidLocation = null;
        distanceSinceLastPoint = 0;
        lastPointsTime = 0;
        consecutiveValidSpeedCount = 0;
    }
}