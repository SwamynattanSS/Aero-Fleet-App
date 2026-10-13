package com.aerofleet.model;

/**
 * Interface defining battery charging mechanics and rates.
 * Unit II: Interfaces and Polymorphism.
 */
public interface ChargerCompatible {
    /**
     * Charges the drone battery by a specific amount.
     * @param amount Percentage to increase battery level.
     */
    void recharge(double amount);

    /**
     * Replenishes battery back to 100%.
     */
    void rechargeFull();

    /**
     * The rate at which this drone model accepts charge (percentage per second).
     */
    double getRechargeRate();
}
