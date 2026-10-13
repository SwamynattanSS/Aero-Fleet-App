package com.aerofleet.model;

/**
 * LongRangeFixedWing - Aerodynamic fixed-wing VTOL hybrid for long-distance rapid transit.
 * Unit II: Inheritance, Polymorphism, Method Overriding.
 */
public class LongRangeFixedWing extends Drone {
    private static final long serialVersionUID = 1L;

    public LongRangeFixedWing(String id, Location spawnLocation) {
        // id, modelName, spawnLocation, maxPayload (kg), cruiseSpeed, baseDischargeRate
        super(id, "AeroWing V2", spawnLocation, 5.0, 6.0, 0.06);
    }

    @Override
    public String getDroneType() {
        return "FixedWing (Long Range Cruiser)";
    }

    @Override
    public double calculateDischarge(double weatherFactor) {
        // Gliding wing design has low energy draw per distance unit
        return baseDischargeRate * (0.9 + weatherFactor * 0.1);
    }

    @Override
    public double getRechargeRate() {
        return 1.0;
    }
}
