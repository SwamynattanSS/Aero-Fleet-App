package com.aerofleet.persistence;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FlightLog - Represents a blackbox mission telemetry recording.
 * Unit IV: Object Serialization and Generic Collections.
 */
public class FlightLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String logId;
    private final String droneId;
    private final String droneType;
    private final String cargoTrackingId;
    private final double cargoWeight;
    private final double startX;
    private final double startY;
    private final double destinationX;
    private final double destinationY;
    private final double batteryRemaining;
    private final double distanceTraveled;
    private final String outcomeStatus;
    private final LocalDateTime timestamp;

    public FlightLog(String logId, String droneId, String droneType, String cargoTrackingId,
                     double cargoWeight, double startX, double startY, double destinationX,
                     double destinationY, double batteryRemaining, double distanceTraveled,
                     String outcomeStatus) {
        this.logId = logId;
        this.droneId = droneId;
        this.droneType = droneType;
        this.cargoTrackingId = cargoTrackingId;
        this.cargoWeight = cargoWeight;
        this.startX = startX;
        this.startY = startY;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.batteryRemaining = batteryRemaining;
        this.distanceTraveled = distanceTraveled;
        this.outcomeStatus = outcomeStatus;
        this.timestamp = LocalDateTime.now();
    }

    public String getLogId() { return logId; }
    public String getDroneId() { return droneId; }
    public String getDroneType() { return droneType; }
    public String getCargoTrackingId() { return cargoTrackingId; }
    public double getCargoWeight() { return cargoWeight; }
    public double getStartX() { return startX; }
    public double getStartY() { return startY; }
    public double getDestinationX() { return destinationX; }
    public double getDestinationY() { return destinationY; }
    public double getBatteryRemaining() { return batteryRemaining; }
    public double getDistanceTraveled() { return distanceTraveled; }
    public String getOutcomeStatus() { return outcomeStatus; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] Drone: %s | Cargo: %s | Distance: %.1f km | Battery: %.1f%% | Status: %s",
                logId, droneId, cargoTrackingId, distanceTraveled, batteryRemaining, outcomeStatus);
    }
}
