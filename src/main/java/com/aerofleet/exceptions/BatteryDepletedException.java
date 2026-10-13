package com.aerofleet.exceptions;

/**
 * Custom Exception thrown when a drone exhausts its battery mid-flight.
 * Unit III: User-defined Exception Handling.
 */
public class BatteryDepletedException extends Exception {
    private final String droneId;
    private final double lastX;
    private final double lastY;

    public BatteryDepletedException(String droneId, double lastX, double lastY) {
        super(String.format("CRITICAL: Drone %s depleted battery at coordinates (%.1f, %.1f)!", droneId, lastX, lastY));
        this.droneId = droneId;
        this.lastX = lastX;
        this.lastY = lastY;
    }

    public String getDroneId() {
        return droneId;
    }

    public double getLastX() {
        return lastX;
    }

    public double getLastY() {
        return lastY;
    }
}
