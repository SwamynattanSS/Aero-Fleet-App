package com.aerofleet.concurrency;

import com.aerofleet.model.Drone;
import com.aerofleet.model.DroneStatus;
import com.aerofleet.model.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ChargingHub - Central shared infrastructure docking station.
 * Unit III: Multithreading, Thread Synchronization, Critical Section, Avoiding Deadlocks, wait() & notifyAll().
 */
public class ChargingHub {
    private final String id;
    private final Location location;
    private final int totalPads;
    private int occupiedPads;
    private final List<String> activeDronesCharging;
    private final List<String> waitingQueue;

    public ChargingHub(String id, Location location, int totalPads) {
        this.id = id;
        this.location = location;
        this.totalPads = totalPads;
        this.occupiedPads = 0;
        this.activeDronesCharging = Collections.synchronizedList(new ArrayList<>());
        this.waitingQueue = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Synchronized method: Drones request a docking pad.
     * If all pads are occupied, drone thread waits until notified.
     */
    public synchronized boolean requestDocking(Drone drone) throws InterruptedException {
        waitingQueue.add(drone.getId());
        drone.setStatus(DroneStatus.WAITING_FOR_DOCK);

        while (occupiedPads >= totalPads) {
            // Wait for another drone to finish charging and notify
            wait();
        }

        waitingQueue.remove(drone.getId());
        occupiedPads++;
        activeDronesCharging.add(drone.getId());
        drone.setStatus(DroneStatus.CHARGING);
        return true;
    }

    /**
     * Synchronized method: Releases the docking pad and notifies all waiting drone threads.
     */
    public synchronized void releaseDocking(Drone drone) {
        if (activeDronesCharging.remove(drone.getId())) {
            occupiedPads = Math.max(0, occupiedPads - 1);
            drone.setStatus(DroneStatus.IDLE);
            // Notify waiting threads to awaken and claim the free pad
            notifyAll();
        }
    }

    public String getId() { return id; }
    public Location getLocation() { return location; }
    public int getTotalPads() { return totalPads; }
    public synchronized int getOccupiedPads() { return occupiedPads; }
    public synchronized int getAvailablePads() { return totalPads - occupiedPads; }
    public List<String> getActiveDronesCharging() { return new ArrayList<>(activeDronesCharging); }
    public List<String> getWaitingQueue() { return new ArrayList<>(waitingQueue); }

    @Override
    public String toString() {
        return String.format("%s [Available: %d/%d | Waiting: %d]", id, getAvailablePads(), totalPads, waitingQueue.size());
    }
}
