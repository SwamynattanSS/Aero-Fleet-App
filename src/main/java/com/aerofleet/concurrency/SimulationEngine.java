package com.aerofleet.concurrency;

import com.aerofleet.exceptions.NoRouteFoundException;
import com.aerofleet.exceptions.PayloadExceededException;
import com.aerofleet.model.*;
import com.aerofleet.persistence.DatabaseManager;
import com.aerofleet.persistence.FlightLog;
import com.aerofleet.persistence.FlightLogSerializer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central simulation coordinator managing active drone threads, weather conditions,
 * and dispatch decisions using the Java Streams API.
 * Unit III: Multithreading, Thread Pools, and Concurrency.
 * Unit IV: Concurrent Collections, Streams API & Lambdas.
 */
public class SimulationEngine {

    private final Map<String, Drone> fleet = new ConcurrentHashMap<>();
    private final List<WeatherZone> weatherZones = new CopyOnWriteArrayList<>();
    private final ChargingHub chargingHub;
    private final ExecutorService droneThreadPool;
    private final ScheduledExecutorService engineScheduler;
    private final List<Runnable> telemetryListeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger logSequence = new AtomicInteger(100);

    private volatile boolean isRunning = true;

    public SimulationEngine() {
        // Shared charging hub located near city center
        this.chargingHub = new ChargingHub("HUB-CENTRAL", new Location(250, 250), 2);
        this.droneThreadPool = Executors.newCachedThreadPool();
        this.engineScheduler = Executors.newSingleThreadScheduledExecutor();

        seedDefaultFleet();
        seedDefaultHazards();
        startCoordinatorLoop();
    }

    private void seedDefaultFleet() {
        // Diverse drone fleet demonstrating Unit II inheritance
        addDrone(new QuadCopter("QD-101", new Location(100, 150)));
        addDrone(new QuadCopter("QD-102", new Location(400, 180)));
        addDrone(new HexaHeavyLift("HX-201", new Location(200, 380)));
        addDrone(new HexaHeavyLift("HX-202", new Location(500, 320)));
        addDrone(new LongRangeFixedWing("FW-301", new Location(150, 500)));
    }

    private void seedDefaultHazards() {
        weatherZones.add(new WeatherZone("WZ-STORM-1", new Location(320, 220), 75.0, WeatherZone.HazardType.THUNDERSTORM));
        weatherZones.add(new WeatherZone("WZ-WIND-2", new Location(180, 360), 60.0, WeatherZone.HazardType.HIGH_WIND));
    }

    public synchronized void addDrone(Drone drone) {
        fleet.put(drone.getId(), drone);
        droneThreadPool.submit(drone);
    }

    /**
     * Dispatch logic using Java Streams API & Lambdas (Unit IV).
     * Filters eligible drones, checks payload compatibility, and finds the closest drone.
     */
    public synchronized Drone dispatchPackage(PackageCargo cargo) throws NoRouteFoundException, PayloadExceededException {
        // Unit IV: Java Streams API with filter, sorting, and lambda expressions
        Optional<Drone> bestCandidate = fleet.values().stream()
                .filter(d -> d.getStatus() == DroneStatus.IDLE)
                .filter(d -> d.getBatteryLevel() >= Drone.LOW_BATTERY_WARNING)
                .filter(d -> d.getMaxPayloadKg() >= cargo.getWeightKg())
                .min(Comparator.comparingDouble(d -> d.getCurrentLocation().distanceTo(cargo.getPickupLocation())));

        if (bestCandidate.isEmpty()) {
            // Check if failure is due to payload constraint
            boolean heavyCargo = fleet.values().stream()
                    .noneMatch(d -> d.getMaxPayloadKg() >= cargo.getWeightKg());

            if (heavyCargo) {
                throw new PayloadExceededException("FLEET-DISPATCH", cargo.getWeightKg(), 10.0);
            } else {
                throw new NoRouteFoundException("No idle drone with sufficient battery available for this mission!");
            }
        }

        Drone assigned = bestCandidate.get();
        assigned.assignCargo(cargo);

        // Record mission launch
        notifyListeners();
        return assigned;
    }

    /**
     * Recurring coordinator loop checking weather collisions, battery levels, and docking queues.
     */
    private void startCoordinatorLoop() {
        engineScheduler.scheduleAtFixedRate(() -> {
            if (!isRunning) return;

            try {
                // 1. Weather Impact on Active Drones
                for (Drone drone : fleet.values()) {
                    Location loc = drone.getCurrentLocation();
                    double speedMod = 1.0;
                    double dischargeMod = 1.0;

                    for (WeatherZone wz : weatherZones) {
                        if (wz.contains(loc)) {
                            speedMod = Math.min(speedMod, wz.getHazardType().getSpeedFactor());
                            dischargeMod = Math.max(dischargeMod, wz.getHazardType().getDischargeFactor());
                        }
                    }
                    drone.setWeatherFactors(speedMod, dischargeMod);

                    // 2. Battery Monitoring: Auto-divert low battery drones to Charging Hub
                    if (drone.getStatus() == DroneStatus.IDLE && drone.getBatteryLevel() < Drone.LOW_BATTERY_WARNING) {
                        initiateRechargeSequence(drone);
                    }

                    // 3. Charging Progress
                    if (drone.getStatus() == DroneStatus.CHARGING) {
                        drone.recharge(drone.getRechargeRate());
                        if (drone.getBatteryLevel() >= 100.0) {
                            chargingHub.releaseDocking(drone);
                        }
                    }
                }

                notifyListeners();
            } catch (Exception e) {
                System.err.println("Coordinator loop error: " + e.getMessage());
            }
        }, 500, 500, TimeUnit.MILLISECONDS);
    }

    private void initiateRechargeSequence(Drone drone) {
        droneThreadPool.submit(() -> {
            try {
                drone.setStatus(DroneStatus.WAITING_FOR_DOCK);
                drone.setTargetLocation(chargingHub.getLocation());

                // Move toward hub
                while (drone.getCurrentLocation().distanceTo(chargingHub.getLocation()) > 10.0) {
                    Thread.sleep(200);
                }

                // Request docking pad with synchronized wait()
                if (chargingHub.requestDocking(drone)) {
                    // Docked; will charge during periodic ticks
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Records a completed delivery or event to both SQLite (JDBC) and Binary Serialization.
     */
    public void recordMissionLog(Drone drone, String status) {
        String logId = "LOG-" + logSequence.incrementAndGet();
        FlightLog log = new FlightLog(
                logId,
                drone.getId(),
                drone.getDroneType(),
                drone.getCurrentCargo() != null ? drone.getCurrentCargo().getTrackingId() : "N/A",
                drone.getCurrentCargo() != null ? drone.getCurrentCargo().getWeightKg() : 0.0,
                drone.getHomeBaseLocation().getX(), drone.getHomeBaseLocation().getY(),
                drone.getCurrentLocation().getX(), drone.getCurrentLocation().getY(),
                drone.getBatteryLevel(),
                drone.getTotalDistanceTraveled(),
                status
        );

        // Unit IV: Object Serialization
        FlightLogSerializer.appendLog(log);

        // Unit IV: SQLite JDBC persistence
        DatabaseManager.getInstance().recordMission(log);
    }

    // Stream-based metrics (Unit IV)
    public int getTotalDeliveries() {
        return fleet.values().stream().mapToInt(Drone::getCompletedMissions).sum();
    }

    public double getAverageBattery() {
        return fleet.values().stream().mapToDouble(Drone::getBatteryLevel).average().orElse(0.0);
    }

    public long getActiveInTransitCount() {
        return fleet.values().stream()
                .filter(d -> d.getStatus() == DroneStatus.DELIVERING || d.getStatus() == DroneStatus.EN_ROUTE_PICKUP)
                .count();
    }

    public void addTelemetryListener(Runnable listener) {
        telemetryListeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable r : telemetryListeners) {
            r.run();
        }
    }

    public void shutdown() {
        isRunning = false;
        fleet.values().forEach(Drone::stopSimulation);
        droneThreadPool.shutdownNow();
        engineScheduler.shutdownNow();
    }

    public Map<String, Drone> getFleet() { return fleet; }
    public List<WeatherZone> getWeatherZones() { return weatherZones; }
    public ChargingHub getChargingHub() { return chargingHub; }
}
