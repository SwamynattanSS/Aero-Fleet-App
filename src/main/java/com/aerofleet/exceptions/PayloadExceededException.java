package com.aerofleet.exceptions;

/**
 * Custom Exception thrown when package weight exceeds maximum drone payload capacity.
 * Unit III: User-defined Exception Handling.
 */
public class PayloadExceededException extends Exception {
    private final String droneId;
    private final double attemptedWeight;
    private final double maxCapacity;

    public PayloadExceededException(String droneId, double attemptedWeight, double maxCapacity) {
        super(String.format("PAYLOAD ERROR: Cannot assign package of %.1f kg to Drone %s (Max capacity: %.1f kg)!",
                attemptedWeight, droneId, maxCapacity));
        this.droneId = droneId;
        this.attemptedWeight = attemptedWeight;
        this.maxCapacity = maxCapacity;
    }

    public String getDroneId() {
        return droneId;
    }

    public double getAttemptedWeight() {
        return attemptedWeight;
    }

    public double getMaxCapacity() {
        return maxCapacity;
    }
}
