package com.aerofleet.persistence;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles binary serialization and deserialization of flight logs.
 * Unit IV: Java I/O Streams API, File I/O, Object Serialization.
 */
public class FlightLogSerializer {

    private static final String DEFAULT_LOG_FILE = "aerofleet_flight_blackbox.dat";

    /**
     * Serializes a list of flight logs directly to a binary file.
     */
    public static synchronized void saveLogs(List<FlightLog> logs, String filePath) throws IOException {
        String target = (filePath != null && !filePath.trim().isEmpty()) ? filePath : DEFAULT_LOG_FILE;
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(target)))) {
            oos.writeObject(logs);
        }
    }

    /**
     * Appends or adds a single log by reading existing logs and writing back.
     */
    public static synchronized void appendLog(FlightLog log) {
        List<FlightLog> existing = loadLogs(DEFAULT_LOG_FILE);
        existing.add(log);
        try {
            saveLogs(existing, DEFAULT_LOG_FILE);
        } catch (IOException e) {
            System.err.println("Failed to serialize flight log: " + e.getMessage());
        }
    }

    /**
     * Deserializes and reads flight logs from binary file.
     */
    @SuppressWarnings("unchecked")
    public static synchronized List<FlightLog> loadLogs(String filePath) {
        String target = (filePath != null && !filePath.trim().isEmpty()) ? filePath : DEFAULT_LOG_FILE;
        File file = new File(target);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                return (List<FlightLog>) obj;
            }
        } catch (FileNotFoundException e) {
            // New file will be created on save
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Warning: Could not read serialized logs, starting fresh: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}
