package com.aerofleet.ui;

import com.aerofleet.concurrency.SimulationEngine;
import com.aerofleet.model.Drone;
import com.aerofleet.model.DroneStatus;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Sidebar panel displaying real-time telemetry data, battery meters, and fleet metrics.
 * Unit V: Swing components (JLabel, JProgressBar, JPanel, JButton, LayoutManagers).
 */
public class TelemetryPanel extends JPanel {

    private final SimulationEngine engine;
    private Drone currentDrone;

    // UI elements for selected drone
    private final JLabel lblId = new JLabel("None Selected");
    private final JLabel lblModel = new JLabel("-");
    private final JLabel lblType = new JLabel("-");
    private final JLabel lblStatus = new JLabel("IDLE");
    private final JLabel lblCoords = new JLabel("(0.0, 0.0)");
    private final JLabel lblPayload = new JLabel("-");
    private final JLabel lblCargo = new JLabel("None");
    private final JLabel lblCompleted = new JLabel("0");
    private final JLabel lblDistance = new JLabel("0.0 km");
    private final JProgressBar pbBattery = new JProgressBar(0, 100);

    // Fleet-wide summary labels
    private final JLabel lblFleetCount = new JLabel("0");
    private final JLabel lblFleetAvgBattery = new JLabel("0%");
    private final JLabel lblFleetDeliveries = new JLabel("0");
    private final JLabel lblFleetActive = new JLabel("0");

    private final JButton btnSendToCharge = new JButton("Send to Charging Hub");
    private final JButton btnReturnBase = new JButton("Recall to Base");

    public TelemetryPanel(SimulationEngine engine) {
        this.engine = engine;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(320, 650));
        setBackground(new Color(30, 34, 45));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();
        setupListeners();
    }

    private void buildUI() {
        // --- 1. SELECTED DRONE SECTION ---
        JPanel pnlSelected = createTitledPanel("SELECTED DRONE TELEMETRY");
        pnlSelected.setLayout(new GridLayout(0, 2, 6, 8));

        addLabelPair(pnlSelected, "Drone ID:", lblId, true);
        addLabelPair(pnlSelected, "Model:", lblModel, false);
        addLabelPair(pnlSelected, "Category:", lblType, false);
        addLabelPair(pnlSelected, "Status:", lblStatus, true);
        addLabelPair(pnlSelected, "Position:", lblCoords, false);
        addLabelPair(pnlSelected, "Max Capacity:", lblPayload, false);
        addLabelPair(pnlSelected, "Current Cargo:", lblCargo, false);
        addLabelPair(pnlSelected, "Deliveries:", lblCompleted, false);
        addLabelPair(pnlSelected, "Flight Distance:", lblDistance, false);

        // Battery Meter
        pbBattery.setStringPainted(true);
        pbBattery.setValue(100);
        pbBattery.setForeground(new Color(0, 200, 83));
        pbBattery.setBackground(new Color(50, 50, 60));

        JPanel pnlBattery = new JPanel(new BorderLayout(5, 5));
        pnlBattery.setOpaque(false);
        JLabel lblBatTitle = new JLabel("Battery State:");
        lblBatTitle.setForeground(Color.WHITE);
        pnlBattery.add(lblBatTitle, BorderLayout.NORTH);
        pnlBattery.add(pbBattery, BorderLayout.CENTER);

        // Quick Action Buttons
        JPanel pnlActions = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlActions.setOpaque(false);
        btnSendToCharge.setEnabled(false);
        btnReturnBase.setEnabled(false);
        pnlActions.add(btnSendToCharge);
        pnlActions.add(btnReturnBase);

        // --- 2. FLEET OVERVIEW SECTION (Unit IV Streams metrics) ---
        JPanel pnlFleet = createTitledPanel("FLEET METRICS & STREAMS");
        pnlFleet.setLayout(new GridLayout(4, 2, 6, 8));
        addLabelPair(pnlFleet, "Total Fleet Size:", lblFleetCount, false);
        addLabelPair(pnlFleet, "Avg Battery (Streams):", lblFleetAvgBattery, false);
        addLabelPair(pnlFleet, "Total Deliveries:", lblFleetDeliveries, false);
        addLabelPair(pnlFleet, "In Transit Currently:", lblFleetActive, true);

        // Assemble
        add(pnlSelected);
        add(Box.createVerticalStrut(10));
        add(pnlBattery);
        add(Box.createVerticalStrut(10));
        add(pnlActions);
        add(Box.createVerticalStrut(15));
        add(pnlFleet);
        add(Box.createVerticalGlue());
    }

    private void addLabelPair(JPanel panel, String title, JLabel valueLabel, boolean highlight) {
        JLabel t = new JLabel(title);
        t.setForeground(new Color(170, 180, 195));
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(t);

        valueLabel.setForeground(highlight ? new Color(0, 229, 255) : Color.WHITE);
        valueLabel.setFont(new Font("SansSerif", highlight ? Font.BOLD : Font.PLAIN, 12));
        panel.add(valueLabel);
    }

    private JPanel createTitledPanel(String title) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        TitledBorder tb = new TitledBorder(new LineBorder(new Color(60, 70, 90), 1), title);
        tb.setTitleColor(new Color(0, 229, 255));
        tb.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        p.setBorder(new CompoundBorder(tb, new EmptyBorder(8, 8, 8, 8)));
        return p;
    }

    private void setupListeners() {
        btnSendToCharge.addActionListener(e -> {
            if (currentDrone != null && currentDrone.getStatus() == DroneStatus.IDLE) {
                currentDrone.setStatus(DroneStatus.WAITING_FOR_DOCK);
                currentDrone.setTargetLocation(engine.getChargingHub().getLocation());
            }
        });

        btnReturnBase.addActionListener(e -> {
            if (currentDrone != null && currentDrone.getStatus() == DroneStatus.IDLE) {
                currentDrone.setStatus(DroneStatus.RETURNING_TO_BASE);
                currentDrone.setTargetLocation(currentDrone.getHomeBaseLocation());
            }
        });
    }

    public void displayDrone(Drone drone) {
        this.currentDrone = drone;
        if (drone == null) {
            lblId.setText("None Selected");
            lblModel.setText("-");
            lblType.setText("-");
            lblStatus.setText("IDLE");
            lblCoords.setText("-");
            lblPayload.setText("-");
            lblCargo.setText("None");
            lblCompleted.setText("0");
            lblDistance.setText("0.0 km");
            pbBattery.setValue(0);
            pbBattery.setString("0%");
            btnSendToCharge.setEnabled(false);
            btnReturnBase.setEnabled(false);
            return;
        }

        lblId.setText(drone.getId());
        lblModel.setText(drone.getModelName());
        lblType.setText(drone.getDroneType());
        lblStatus.setText(drone.getStatus().toString());
        lblCoords.setText(drone.getCurrentLocation().toString());
        lblPayload.setText(String.format("%.1f kg", drone.getMaxPayloadKg()));
        lblCargo.setText(drone.getCurrentCargo() != null ? drone.getCurrentCargo().getTrackingId() : "None");
        lblCompleted.setText(String.valueOf(drone.getCompletedMissions()));
        lblDistance.setText(String.format("%.1f km", drone.getTotalDistanceTraveled()));

        int bat = (int) drone.getBatteryLevel();
        pbBattery.setValue(bat);
        pbBattery.setString(bat + "%");
        if (bat > 50) pbBattery.setForeground(new Color(0, 200, 83));
        else if (bat > 25) pbBattery.setForeground(new Color(255, 171, 0));
        else pbBattery.setForeground(new Color(255, 23, 68));

        btnSendToCharge.setEnabled(drone.getStatus() == DroneStatus.IDLE);
        btnReturnBase.setEnabled(drone.getStatus() == DroneStatus.IDLE);
    }

    public void updateFleetMetrics() {
        lblFleetCount.setText(String.valueOf(engine.getFleet().size()));
        lblFleetAvgBattery.setText(String.format("%.1f%%", engine.getAverageBattery()));
        lblFleetDeliveries.setText(String.valueOf(engine.getTotalDeliveries()));
        lblFleetActive.setText(String.valueOf(engine.getActiveInTransitCount()));

        // Also refresh selected drone live coordinates & battery
        if (currentDrone != null) {
            displayDrone(currentDrone);
        }
    }
}
