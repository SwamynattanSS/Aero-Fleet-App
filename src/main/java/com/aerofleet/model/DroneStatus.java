package com.aerofleet.model;

/**
 * Lifecycle states of an autonomous drone.
 */
public enum DroneStatus {
    IDLE("Idle"),
    EN_ROUTE_PICKUP("En Route to Pickup"),
    DELIVERING("In Transit (Delivering)"),
    RETURNING_TO_BASE("Returning to Base"),
    CHARGING("Charging at Hub"),
    WAITING_FOR_DOCK("Queued for Docking"),
    EMERGENCY_LANDED("Emergency Landed");

    private final String displayName;

    DroneStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
