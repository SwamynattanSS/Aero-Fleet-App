package com.aerofleet.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Represents a cargo delivery order.
 * Unit I: Demonstrates Date & Time API, static counters, regex validation.
 */
public class PackageCargo implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1001);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Regular expression for validating 10-digit Indian/international phone numbers (Unit IV)
    public static final Pattern PHONE_REGEX = Pattern.compile("^[+]?[0-9]{10,13}$");

    private final String trackingId;
    private final String description;
    private final double weightKg;
    private final Location pickupLocation;
    private final Location dropoffLocation;
    private final String recipientContact;
    private final LocalDateTime createdTime;
    private LocalDateTime deliveredTime;

    public PackageCargo(String description, double weightKg, Location pickup, Location dropoff, String recipientContact) {
        this.trackingId = "PKG-" + ID_GENERATOR.getAndIncrement();
        this.description = description;
        this.weightKg = weightKg;
        this.pickupLocation = pickup;
        this.dropoffLocation = dropoff;
        this.recipientContact = recipientContact;
        this.createdTime = LocalDateTime.now();
    }

    public static boolean isValidContact(String phone) {
        if (phone == null) return false;
        return PHONE_REGEX.matcher(phone.trim()).matches();
    }

    public String getTrackingId() { return trackingId; }
    public String getDescription() { return description; }
    public double getWeightKg() { return weightKg; }
    public Location getPickupLocation() { return pickupLocation; }
    public Location getDropoffLocation() { return dropoffLocation; }
    public String getRecipientContact() { return recipientContact; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public LocalDateTime getDeliveredTime() { return deliveredTime; }

    public void markDelivered() {
        this.deliveredTime = LocalDateTime.now();
    }

    public String getFormattedCreatedTime() {
        return createdTime.format(TIME_FORMATTER);
    }

    public String getFormattedDeliveredTime() {
        return deliveredTime != null ? deliveredTime.format(TIME_FORMATTER) : "Pending";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%.1f kg) -> %s", trackingId, description, weightKg, dropoffLocation);
    }
}
