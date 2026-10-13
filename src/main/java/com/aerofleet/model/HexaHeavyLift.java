package com.aerofleet.model;

/**
 * HexaHeavyLift - 6-rotor industrial cargo drone for heavy medical / industrial payloads.
 * Unit II: Inheritance, Polymorphism, Method Overriding.
 */
public class HexaHeavyLift extends Drone {
    private static final long serialVersionUID = 1L;

    public HexaHeavyLift(String id, Location spawnLocation) {
        // id, modelName, spawnLocation, maxPayload (kg), cruiseSpeed, baseDischargeRate
        super(id, "TitanHex H6", spawnLocation, 10.0, 3.0, 0.12);
    }

    @Override
    public String getDroneType() {
        return "HexaHeavyLift (Heavy Cargo)";
    }

    @Override
    public double calculateDischarge(double weatherFactor) {
        // High stability in wind due to six powerful rotors
        double stabilityMitigation = Math.max(1.0, weatherFactor * 0.85);
        return baseDischargeRate * stabilityMitigation;
    }

    @Override
    public double getRechargeRate() {
        return 0.8; // Larger battery packs take longer to recharge
    }
}
