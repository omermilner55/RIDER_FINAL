package com.example.riderfinal;
public class RidePointsCalculator {
    private static final float MIN_LEGAL_SPEED = 20.0f; // km/h
    private static final float MAX_LEGAL_SPEED = 25.0f; // km/h
    private static final float PENALTY_SPEED_THRESHOLD = 28.0f; // km/h
    private static final int POINTS_PER_KM = 5;
    private static final int PENALTY_POINTS = 2; // points deducted per 10 seconds
    private static final int BONUS_POINTS = 10;
    private static final long LEGAL_RIDE_BONUS_TIME = 300000; // 5 minutes in milliseconds

    private float lastSpeed = 0.0f;
    private long lastSpeedUpdateTime = 0;
    private long legalRideStartTime = 0;
    private boolean isRidingLegally = false;
    private int totalPoints = 0;
    private double totalDistance = 0.0;
    private long penaltyTimer = 0;

    public void updatePoints(float currentSpeedKmh, double distanceInMeters, long currentTimeMillis) {
        // Initialize time on first update
        if (lastSpeedUpdateTime == 0) {
            lastSpeedUpdateTime = currentTimeMillis;
            return;
        }

        // Calculate time difference
        long timeDiff = currentTimeMillis - lastSpeedUpdateTime;

        // Update total distance
        totalDistance = distanceInMeters;

        // Check if speed is within legal range
        if (currentSpeedKmh >= MIN_LEGAL_SPEED && currentSpeedKmh <= MAX_LEGAL_SPEED) {
            // Add points for legal distance covered
            double distanceDiff = (currentSpeedKmh * timeDiff / 3600000.0); // Convert to kilometers
            totalPoints += (int)(distanceDiff * POINTS_PER_KM);

            // Handle legal ride bonus
            if (!isRidingLegally) {
                isRidingLegally = true;
                legalRideStartTime = currentTimeMillis;
            } else if (currentTimeMillis - legalRideStartTime >= LEGAL_RIDE_BONUS_TIME) {
                totalPoints += BONUS_POINTS;
                // Reset timer for next bonus
                legalRideStartTime = currentTimeMillis;
            }
        } else {
            // Reset legal ride tracking if speed is outside legal range
            isRidingLegally = false;

            // Apply penalties if speed is above threshold
            if (currentSpeedKmh > PENALTY_SPEED_THRESHOLD) {
                penaltyTimer += timeDiff;
                if (penaltyTimer >= 10000) { // Every 10 seconds
                    totalPoints = Math.max(0, totalPoints - PENALTY_POINTS);
                    penaltyTimer = 0; // Reset penalty timer
                }
            }
        }

        // Update last speed and time
        lastSpeed = currentSpeedKmh;
        lastSpeedUpdateTime = currentTimeMillis;
    }

    public int getTotalPoints() {
        return Math.max(0, totalPoints); // Ensure points never go below 0
    }

    public boolean isCurrentlyRidingLegally() {
        return isRidingLegally;
    }

    public void reset() {
        totalPoints = 0;
        lastSpeed = 0.0f;
        lastSpeedUpdateTime = 0;
        legalRideStartTime = 0;
        isRidingLegally = false;
        totalDistance = 0.0;
        penaltyTimer = 0;
    }

    public String getSpeedStatus(float currentSpeedKmh) {
        if (currentSpeedKmh < MIN_LEGAL_SPEED) {
            return "מהירות נמוכה מדי";
        } else if (currentSpeedKmh <= MAX_LEGAL_SPEED) {
            return "מהירות חוקית";
        } else if (currentSpeedKmh <= PENALTY_SPEED_THRESHOLD) {
            return "מהירות גבוהה";
        } else {
            return "מהירות מסוכנת";
        }
    }
}