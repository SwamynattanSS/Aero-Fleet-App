package com.aerofleet.model;

import java.awt.Color;
import java.io.Serializable;

/**
 * Represents a dynamic atmospheric hazard zone (e.g., thunderstorms, gale winds).
 */
public class WeatherZone implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum HazardType {
        THUNDERSTORM("Thunderstorm", new Color(138, 43, 226, 70), 2.2, 0.4),
        HIGH_WIND("Gale Winds", new Color(30, 144, 255, 60), 1.5, 0.7),
        HEAVY_RAIN("Heavy Rain", new Color(46, 139, 87, 60), 1.3, 0.85);

        private final String label;
        private final Color overlayColor;
        private final double dischargeFactor;
        private final double speedFactor;

        HazardType(String label, Color overlayColor, double dischargeFactor, double speedFactor) {
            this.label = label;
            this.overlayColor = overlayColor;
            this.dischargeFactor = dischargeFactor;
            this.speedFactor = speedFactor;
        }

        public String getLabel() { return label; }
        public Color getOverlayColor() { return overlayColor; }
        public double getDischargeFactor() { return dischargeFactor; }
        public double getSpeedFactor() { return speedFactor; }
    }

    private final String id;
    private final Location center;
    private final double radius;
    private final HazardType hazardType;

    public WeatherZone(String id, Location center, double radius, HazardType hazardType) {
        this.id = id;
        this.center = center;
        this.radius = radius;
        this.hazardType = hazardType;
    }

    public String getId() { return id; }
    public Location getCenter() { return center; }
    public double getRadius() { return radius; }
    public HazardType getHazardType() { return hazardType; }

    /**
     * Checks if a point is within this storm hazard zone.
     */
    public boolean contains(Location loc) {
        if (loc == null) return false;
        return center.distanceTo(loc) <= radius;
    }
}
