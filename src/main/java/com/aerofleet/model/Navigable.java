package com.aerofleet.model;

/**
 * Interface defining autonomous navigation capabilities.
 * Unit II: Interfaces and Polymorphism.
 */
public interface Navigable {
    /**
     * Advances drone position toward the designated target coordinates.
     * @param target Target destination.
     * @param speedMultiplier Speed modification (e.g., from weather hazards).
     * @return true if target has been reached within threshold; false otherwise.
     */
    boolean moveToward(Location target, double speedMultiplier);

    /**
     * Gets the base cruising speed in grid units per tick.
     */
    double getCruiseSpeed();

    /**
     * Current heading angle in degrees (0 to 360).
     */
    double getHeadingAngle();
}
