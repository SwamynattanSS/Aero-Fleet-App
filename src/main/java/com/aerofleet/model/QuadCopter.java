package com.aerofleet.model;

/**
 * QuadCopter - Lightweight, agile multi-rotor for urban point-to-point delivery.
 * Unit II: Inheritance, Polymorphism, Method Overriding.
 */
public class QuadCopter extends Drone {
    private static final long serialVersionUID = 1L;

    public QuadCopter(String id, Location spawnLocation) {
        // id, modelName, spawnLocation, maxPayload (kg), cruiseSpeed, baseDischargeRate
        super(id, "AeroQuad X4", spawnLocation, 3.0, 4.5, 0.08);
    }

    @Override
    public String getDroneType() {
        return "QuadCopter (Agile Courier)";
    }

    @Override
    public double calculateDischarge(double weatherFactor) {
        // Quadcopters are more susceptible to headwinds
        return baseDischargeRate * weatherFactor;
    }

    @Override
    public double getRechargeRate() {
        return 1.2; // 1.2% per tick in charging station
    }
}
