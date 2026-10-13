package com.aerofleet.ui;

import com.aerofleet.concurrency.SimulationEngine;
import com.aerofleet.exceptions.NoRouteFoundException;
import com.aerofleet.exceptions.PayloadExceededException;
import com.aerofleet.model.Drone;
import com.aerofleet.model.Location;
import com.aerofleet.model.PackageCargo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for booking and dispatching drone deliveries.
 * Demonstrates Unit IV Regex validation, Unit III Custom Exception handling, and Unit V Swing Forms.
 */
public class DispatchDialog extends JDialog {

    private final SimulationEngine engine;

    private final JTextField txtDescription = new JTextField("Emergency Medical Kit", 18);
    private final JSpinner spinWeight = new JSpinner(new SpinnerNumberModel(2.5, 0.5, 12.0, 0.5));
    private final JComboBox<String> comboPickup = new JComboBox<>(new String[]{
            "Central Depot (100, 120)",
            "North Logistics Hub (450, 100)",
            "South Station (150, 500)",
            "Custom Coordinates"
    });
    private final JComboBox<String> comboDropoff = new JComboBox<>(new String[]{
            "Metro General Hospital (480, 480)",
            "University Research Park (160, 380)",
            "Downtown Tech Center (380, 280)",
            "Suburban Clinic (520, 520)",
            "Custom Coordinates"
    });
    private final JTextField txtPhone = new JTextField("+919876543210", 15);
    private final JButton btnDispatch = new JButton("Launch Mission");
    private final JButton btnCancel = new JButton("Cancel");

    public DispatchDialog(Frame parent, SimulationEngine engine) {
        super(parent, "Dispatch New Drone Delivery", true);
        this.engine = engine;

        setSize(460, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(new Color(32, 38, 50));

        buildForm();
        setupListeners();
    }

    private void buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(form, gbc, row++, "Cargo Description:", txtDescription);
        addFormRow(form, gbc, row++, "Weight (kg):", spinWeight);
        addFormRow(form, gbc, row++, "Pickup Location:", comboPickup);
        addFormRow(form, gbc, row++, "Destination / Dropoff:", comboDropoff);
        addFormRow(form, gbc, row++, "Recipient Phone (Regex):", txtPhone);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnDispatch.setBackground(new Color(0, 200, 83));
        btnDispatch.setForeground(Color.BLACK);
        btnDispatch.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCancel.setBackground(new Color(80, 90, 105));
        btnCancel.setForeground(Color.WHITE);

        btnPanel.add(btnCancel);
        btnPanel.add(btnDispatch);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        form.add(btnPanel, gbc);

        add(form, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.35;

        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(180, 195, 215));
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void setupListeners() {
        btnCancel.addActionListener(e -> dispose());

        btnDispatch.addActionListener(e -> {
            String phone = txtPhone.getText().trim();
            // Unit IV: Regular Expression validation check
            if (!PackageCargo.isValidContact(phone)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid recipient contact phone number!\nMust be 10-13 digits matching regex: " + PackageCargo.PHONE_REGEX.pattern(),
                        "Regex Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double weight = ((Number) spinWeight.getValue()).doubleValue();
            Location pickup = parseLocation((String) comboPickup.getSelectedItem(), 100, 120);
            Location dropoff = parseLocation((String) comboDropoff.getSelectedItem(), 480, 480);

            PackageCargo cargo = new PackageCargo(
                    txtDescription.getText().trim(),
                    weight,
                    pickup,
                    dropoff,
                    phone
            );

            try {
                // Dispatch through simulation engine
                Drone assigned = engine.dispatchPackage(cargo);
                JOptionPane.showMessageDialog(this,
                        String.format("Mission Launched!\nAssigned Drone: %s (%s)\nTracking ID: %s",
                                assigned.getId(), assigned.getDroneType(), cargo.getTrackingId()),
                        "Mission Dispatched Successfully", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (PayloadExceededException ex) {
                // Unit III: Custom Exception catching
                JOptionPane.showMessageDialog(this,
                        ex.getMessage() + "\nTip: Deploy a HexaHeavyLift drone for weights exceeding 5kg.",
                        "Payload Capacity Exceeded", JOptionPane.ERROR_MESSAGE);
            } catch (NoRouteFoundException ex) {
                // Unit III: Custom Exception catching
                JOptionPane.showMessageDialog(this,
                        ex.getMessage() + "\nWait for an idle drone to recharge or return to base.",
                        "No Drone Available", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private Location parseLocation(String selection, double defX, double defY) {
        if (selection.contains("100, 120")) return new Location(100, 120);
        if (selection.contains("450, 100")) return new Location(450, 100);
        if (selection.contains("150, 500")) return new Location(150, 500);
        if (selection.contains("480, 480")) return new Location(480, 480);
        if (selection.contains("160, 380")) return new Location(160, 380);
        if (selection.contains("380, 280")) return new Location(380, 280);
        if (selection.contains("520, 520")) return new Location(520, 520);
        return new Location(defX, defY);
    }
}
