package com.aerofleet.ui;

import com.aerofleet.persistence.DatabaseManager;
import com.aerofleet.persistence.FlightLog;
import com.aerofleet.persistence.FlightLogSerializer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Bottom panel showing a JTable of completed delivery logs and blackbox telemetry records.
 * Unit IV: File I/O Streams, Object Serialization, JDBC.
 * Unit V: Swing JTable, JScrollPane, Buttons, Event Handling.
 */
public class MissionLogPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton btnRefreshDb = new JButton("Fetch from SQLite (JDBC)");
    private final JButton btnLoadBinary = new JButton("Load Binary Logs (.dat)");
    private final JButton btnExportCsv = new JButton("Export to CSV (File I/O)");

    public MissionLogPanel() {
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(950, 180));
        setBackground(new Color(25, 29, 38));
        setBorder(new EmptyBorder(6, 6, 6, 6));

        // Setup JTable model
        String[] columns = {"Log ID", "Drone ID", "Drone Category", "Cargo ID", "Weight (kg)", "Distance (km)", "Battery %", "Status", "Timestamp"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only
            }
        };

        table = new JTable(tableModel);
        table.setBackground(new Color(32, 38, 50));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(50, 60, 80));
        table.setSelectionBackground(new Color(0, 150, 136));
        table.setRowHeight(22);
        table.getTableHeader().setBackground(new Color(45, 55, 72));
        table.getTableHeader().setForeground(new Color(0, 229, 255));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(32, 38, 50));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 90)));

        // Toolbar for data actions
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        toolbar.add(new JLabel("<html><b style='color:#00E5FF'>FLIGHT LOGS:</b></html>"));
        toolbar.add(btnRefreshDb);
        toolbar.add(btnLoadBinary);
        toolbar.add(btnExportCsv);

        add(toolbar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setupListeners();
        loadInitialData();
    }

    private void setupListeners() {
        btnRefreshDb.addActionListener(e -> {
            loadFromDatabase();
            JOptionPane.showMessageDialog(this, "Loaded latest records from SQLite database via JDBC!", "JDBC Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnLoadBinary.addActionListener(e -> {
            loadFromBinarySerialization();
            JOptionPane.showMessageDialog(this, "Loaded blackbox flight logs via Java Object Deserialization (.dat)!", "Serialization Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnExportCsv.addActionListener(e -> exportToCsv());
    }

    public synchronized void addLogEntry(FlightLog log) {
        SwingUtilities.invokeLater(() -> {
            tableModel.insertRow(0, new Object[]{
                    log.getLogId(),
                    log.getDroneId(),
                    log.getDroneType(),
                    log.getCargoTrackingId(),
                    String.format("%.1f", log.getCargoWeight()),
                    String.format("%.1f", log.getDistanceTraveled()),
                    String.format("%.1f%%", log.getBatteryRemaining()),
                    log.getOutcomeStatus(),
                    log.getFormattedTimestamp()
            });
        });
    }

    private void loadInitialData() {
        // Try loading binary logs first
        List<FlightLog> logs = FlightLogSerializer.loadLogs(null);
        if (logs.isEmpty()) {
            loadFromDatabase();
        } else {
            populateTable(logs);
        }
    }

    private void loadFromDatabase() {
        List<FlightLog> dbLogs = DatabaseManager.getInstance().getRecentMissions(30);
        populateTable(dbLogs);
    }

    private void loadFromBinarySerialization() {
        List<FlightLog> binaryLogs = FlightLogSerializer.loadLogs(null);
        populateTable(binaryLogs);
    }

    private void populateTable(List<FlightLog> logs) {
        tableModel.setRowCount(0);
        for (FlightLog log : logs) {
            tableModel.addRow(new Object[]{
                    log.getLogId(),
                    log.getDroneId(),
                    log.getDroneType(),
                    log.getCargoTrackingId(),
                    String.format("%.1f", log.getCargoWeight()),
                    String.format("%.1f", log.getDistanceTraveled()),
                    String.format("%.1f%%", log.getBatteryRemaining()),
                    log.getOutcomeStatus(),
                    log.getFormattedTimestamp()
            });
        }
    }

    /**
     * File I/O: Exports currently displayed table rows to CSV file (Unit IV File I/O).
     */
    private void exportToCsv() {
        String filename = "aerofleet_mission_report.csv";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // Write Header
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                bw.write(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) bw.write(",");
            }
            bw.newLine();

            // Write Rows
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object val = tableModel.getValueAt(row, col);
                    bw.write(val != null ? val.toString() : "");
                    if (col < tableModel.getColumnCount() - 1) bw.write(",");
                }
                bw.newLine();
            }

            JOptionPane.showMessageDialog(this,
                    "Successfully exported mission logs to: " + filename,
                    "File I/O Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to export CSV: " + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
