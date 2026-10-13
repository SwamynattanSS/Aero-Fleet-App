package com.aerofleet.model;

import com.aerofleet.exceptions.BatteryDepletedException;
import com.aerofleet.exceptions.PayloadExceededException;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base class for all autonomous drones.
 * Units I & II: Object-Oriented Design, Inheritance, Abstract Class, Polymorphism.
 * Unit III: Multithreading (implements Runnable).
 */
public abstract class Drone implements Navigable, ChargerCompatible, Runnable, Serializable {
    private static final long serialVersionUID = 1L;

    public static final double LOW_BATTERY_WARNING = 25.0;
    public static final double CRITICAL_BATTERY_LEVEL = 10.0;

    protected final String id;
    protected final String modelName;
    protected final double maxPayloadKg;
    protected final double cruiseSpeed;      // Base speed in grid points per tick
    protected final double baseDischargeRate; // Battery % depleted per tick

    protected Location currentLocation;
    protected Location targetLocation;
    protected Location homeBaseLocation;
    protected double batteryLevel;           // 0.0 to 100.0%
    protected double headingAngle;           // In degrees (0 - 360)
    protected DroneStatus status;
    protected PackageCargo currentCargo;
    protected double totalDistanceTraveled;
    protected int completedMissions;

    protected final AtomicBoolean running = new AtomicBoolean(true);
    protected final AtomicBoolean paused = new AtomicBoolean(false);

    // Speed multiplier affected by weather (updated by simulation engine)
    protected volatile double weatherSpeedFactor = 1.0;
    protected volatile double weatherDischargeFactor = 1.0;

    public Drone(String id, String modelName, Location spawnLocation, double maxPayloadKg, double cruiseSpeed, double baseDischargeRate) {
        this.id = id;
        this.modelName = modelName;
        this.currentLocation = new Location(spawnLocation.getX(), spawnLocation.getY());
        this.homeBaseLocation = new Location(spawnLocation.getX(), spawnLocation.getY());
        this.maxPayloadKg = maxPayloadKg;
        this.cruiseSpeed = cruiseSpeed;
        this.baseDischargeRate = baseDischargeRate;
        this.batteryLevel = 100.0;
        this.headingAngle = 0.0;
        this.status = DroneStatus.IDLE;
        this.totalDistanceTraveled = 0.0;
        this.completedMissions = 0;
    }

    // Abstract methods to be overridden by specialized subclasses (Unit II Polymorphism)
    public abstract String getDroneType();
    public abstract double calculateDischarge(double weatherFactor);

    @Override
    public boolean moveToward(Location target, double speedMultiplier) {
        if (target == null) return true;

        double distance = currentLocation.distanceTo(target);
        double step = cruiseSpeed * speedMultiplier;

        if (distance <= step || distance < 1.0) {
            currentLocation.setX(target.getX());
            currentLocation.setY(target.getY());
            totalDistanceTraveled += distance;
            return true; // Reached target
        }

        // Trigonometric movement toward target
        double dx = target.getX() - currentLocation.getX();
        double dy = target.getY() - currentLocation.getY();
        double angleRad = Math.atan2(dy, dx);

        this.headingAngle = Math.toDegrees(angleRad);
        if (this.headingAngle < 0) this.headingAngle += 360;

        double nextX = currentLocation.getX() + Math.cos(angleRad) * step;
        double nextY = currentLocation.getY() + Math.sin(angleRad) * step;

        currentLocation.setX(nextX);
        currentLocation.setY(nextY);
        totalDistanceTraveled += step;
        return false;
    }

    /**
     * Assigns cargo to this drone after verifying payload capacity.
     * Throws custom PayloadExceededException if cargo is too heavy.
     */
    public synchronized void assignCargo(PackageCargo cargo) throws PayloadExceededException {
        if (cargo.getWeightKg() > maxPayloadKg) {
            throw new PayloadExceededException(id, cargo.getWeightKg(), maxPayloadKg);
        }
        this.currentCargo = cargo;
        this.targetLocation = cargo.getPickupLocation();
        this.status = DroneStatus.EN_ROUTE_PICKUP;
    }

    @Override
    public void recharge(double amount) {
        this.batteryLevel = Math.min(100.0, this.batteryLevel + amount);
        if (this.batteryLevel >= 99.0) {
            this.batteryLevel = 100.0;
            if (this.status == DroneStatus.CHARGING) {
                this.status = DroneStatus.IDLE;
            }
        }
    }

    @Override
    public void rechargeFull() {
        this.batteryLevel = 100.0;
        if (this.status == DroneStatus.CHARGING) {
            this.status = DroneStatus.IDLE;
        }
    }

    /**
     * Main background thread execution loop (Unit III Multithreading).
     */
    @Override
    public void run() {
        while (running.get()) {
            try {
                if (!paused.get()) {
                    tickPhysics();
                }
                Thread.sleep(100); // 10 ticks per second for responsive simulation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (BatteryDepletedException e) {
                this.status = DroneStatus.EMERGENCY_LANDED;
                System.err.println(e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected drone error: " + e.getMessage());
            }
        }
    }

    /**
     * Executes one physics and state machine tick.
     */
    protected synchronized void tickPhysics() throws BatteryDepletedException {
        if (status == DroneStatus.CHARGING || status == DroneStatus.EMERGENCY_LANDED) {
            return;
        }

        // Battery depletion calculation
        if (status != DroneStatus.IDLE) {
            double discharge = calculateDischarge(weatherDischargeFactor);
            if (currentCargo != null) {
                // Heavier cargo causes slightly higher battery drain
                discharge *= (1.0 + (currentCargo.getWeightKg() / maxPayloadKg) * 0.3);
            }
            batteryLevel -= discharge;

            if (batteryLevel <= 0.0) {
                batteryLevel = 0.0;
                throw new BatteryDepletedException(id, currentLocation.getX(), currentLocation.getY());
            }
        }

        // State Machine
        switch (status) {
            case EN_ROUTE_PICKUP:
                if (moveToward(targetLocation, weatherSpeedFactor)) {
                    // Picked up cargo
                    this.status = DroneStatus.DELIVERING;
                    this.targetLocation = currentCargo.getDropoffLocation();
                }
                break;

            case DELIVERING:
                if (moveToward(targetLocation, weatherSpeedFactor)) {
                    // Reached dropoff
                    currentCargo.markDelivered();
                    completedMissions++;
                    currentCargo = null;
                    // Return home
                    this.status = DroneStatus.RETURNING_TO_BASE;
                    this.targetLocation = homeBaseLocation;
                }
                break;

            case RETURNING_TO_BASE:
                if (moveToward(targetLocation, weatherSpeedFactor)) {
                    this.status = DroneStatus.IDLE;
                    this.targetLocation = null;
                }
                break;

            default:
                break;
        }
    }

    public void stopSimulation() {
        running.set(false);
    }

    public void setPaused(boolean p) {
        paused.set(p);
    }

    public void setWeatherFactors(double speedFactor, double dischargeFactor) {
        this.weatherSpeedFactor = speedFactor;
        this.weatherDischargeFactor = dischargeFactor;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getModelName() { return modelName; }
    public double getMaxPayloadKg() { return maxPayloadKg; }
    public double getCruiseSpeed() { return cruiseSpeed; }
    public Location getCurrentLocation() { return currentLocation; }
    public Location getTargetLocation() { return targetLocation; }
    public Location getHomeBaseLocation() { return homeBaseLocation; }
    public double getBatteryLevel() { return batteryLevel; }
    public double getHeadingAngle() { return headingAngle; }
    public DroneStatus getStatus() { return status; }
    public void setStatus(DroneStatus status) { this.status = status; }
    public PackageCargo getCurrentCargo() { return currentCargo; }
    public double getTotalDistanceTraveled() { return totalDistanceTraveled; }
    public int getCompletedMissions() { return completedMissions; }
    public void setTargetLocation(Location target) { this.targetLocation = target; }

    public boolean isAvailableForMission() {
        return status == DroneStatus.IDLE && batteryLevel > LOW_BATTERY_WARNING;
    }
}
