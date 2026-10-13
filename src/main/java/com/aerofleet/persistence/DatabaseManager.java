package com.aerofleet.persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database persistence using JDBC.
 * Unit IV: Databases with JDBC, Connection, Statement, PreparedStatement, ResultSet.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:aerofleet.db";
    private static DatabaseManager instance;
    private boolean isConnected = false;

    private DatabaseManager() {
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initDatabase() {
        try {
            // Test if driver is loaded
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement()) {

                String sqlMissions = "CREATE TABLE IF NOT EXISTS missions (" +
                        "id TEXT PRIMARY KEY, " +
                        "drone_id TEXT NOT NULL, " +
                        "drone_type TEXT NOT NULL, " +
                        "cargo_tracking_id TEXT, " +
                        "cargo_weight REAL, " +
                        "distance REAL, " +
                        "battery_remaining REAL, " +
                        "outcome_status TEXT, " +
                        "timestamp TEXT" +
                        ");";
                stmt.execute(sqlMissions);

                String sqlDroneStats = "CREATE TABLE IF NOT EXISTS drone_telemetry (" +
                        "drone_id TEXT PRIMARY KEY, " +
                        "model_name TEXT, " +
                        "battery_level REAL, " +
                        "total_distance REAL, " +
                        "completed_missions INTEGER, " +
                        "last_updated TEXT" +
                        ");";
                stmt.execute(sqlDroneStats);

                isConnected = true;
                System.out.println("JDBC: SQLite database initialized successfully at " + DB_URL);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Warning: SQLite driver not found on classpath. Operating in offline/memory mode.");
            isConnected = false;
        } catch (SQLException e) {
            System.err.println("JDBC Error initializing database: " + e.getMessage());
            isConnected = false;
        }
    }

    /**
     * Inserts a completed flight mission record into the database using PreparedStatement.
     */
    public synchronized boolean recordMission(FlightLog log) {
        if (!isConnected) return false;

        String sql = "INSERT OR REPLACE INTO missions " +
                "(id, drone_id, drone_type, cargo_tracking_id, cargo_weight, distance, battery_remaining, outcome_status, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, log.getLogId());
            pstmt.setString(2, log.getDroneId());
            pstmt.setString(3, log.getDroneType());
            pstmt.setString(4, log.getCargoTrackingId());
            pstmt.setDouble(5, log.getCargoWeight());
            pstmt.setDouble(6, log.getDistanceTraveled());
            pstmt.setDouble(7, log.getBatteryRemaining());
            pstmt.setString(8, log.getOutcomeStatus());
            pstmt.setString(9, log.getFormattedTimestamp());

            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("JDBC Insert Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves recent mission logs from the database using ResultSet.
     */
    public synchronized List<FlightLog> getRecentMissions(int limit) {
        List<FlightLog> list = new ArrayList<>();
        if (!isConnected) return list;

        String sql = "SELECT * FROM missions ORDER BY timestamp DESC LIMIT ?;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FlightLog log = new FlightLog(
                            rs.getString("id"),
                            rs.getString("drone_id"),
                            rs.getString("drone_type"),
                            rs.getString("cargo_tracking_id"),
                            rs.getDouble("cargo_weight"),
                            0, 0, 0, 0,
                            rs.getDouble("battery_remaining"),
                            rs.getDouble("distance"),
                            rs.getString("outcome_status")
                    );
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("JDBC Query Error: " + e.getMessage());
        }
        return list;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
