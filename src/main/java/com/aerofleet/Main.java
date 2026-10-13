package com.aerofleet;

import com.aerofleet.ui.MainFrame;

import javax.swing.*;

/**
 * Main Application Launcher for AeroFleet Simulator.
 */
public class Main {
    public static void main(String[] args) {
        // Configure Look and Feel for modern rendering
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Fall back to system or default Look & Feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignoredAgain) {}
        }

        // Launch GUI strictly on Event Dispatch Thread (Unit V)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
