package com.aerofleet.ui;

import com.aerofleet.concurrency.ChargingHub;
import com.aerofleet.concurrency.SimulationEngine;
import com.aerofleet.model.Drone;
import com.aerofleet.model.Location;
import com.aerofleet.model.WeatherZone;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * Custom 2D Grid Canvas rendering the city map, weather zones, charging hubs, and drones.
 * Unit V: AWT/Swing Graphics, paintComponent, Event Listeners & Adapters.
 */
public class GridMapPanel extends JPanel {

    private final SimulationEngine engine;
    private Drone selectedDrone = null;
    private Consumer<Drone> onDroneSelectedListener;

    public GridMapPanel(SimulationEngine engine) {
        this.engine = engine;
        setBackground(new Color(24, 28, 36)); // Dark radar/tactical background
        setPreferredSize(new Dimension(650, 650));

        // Unit V: Event Handling with MouseAdapter
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMapClick(e.getX(), e.getY());
            }
        });
    }

    public void setOnDroneSelectedListener(Consumer<Drone> listener) {
        this.onDroneSelectedListener = listener;
    }

    public Drone getSelectedDrone() {
        return selectedDrone;
    }

    public void setSelectedDrone(Drone drone) {
        this.selectedDrone = drone;
        repaint();
    }

    private void handleMapClick(int clickX, int clickY) {
        Location clickLoc = new Location(clickX, clickY);
        Drone closest = null;
        double minDistance = 35.0; // Click tolerance radius in pixels

        for (Drone d : engine.getFleet().values()) {
            double dist = d.getCurrentLocation().distanceTo(clickLoc);
            if (dist < minDistance) {
                closest = d;
                minDistance = dist;
            }
        }

        this.selectedDrone = closest;
        if (onDroneSelectedListener != null) {
            onDroneSelectedListener.accept(closest);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // High quality anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawCityGrid(g2);
        drawWeatherZones(g2);
        drawChargingHub(g2);
        drawDrones(g2);
    }

    private void drawCityGrid(Graphics2D g2) {
        g2.setColor(new Color(40, 48, 64));
        int step = 50;
        int w = getWidth();
        int h = getHeight();

        for (int x = 0; x < w; x += step) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += step) {
            g2.drawLine(0, y, w, y);
        }

        // Sector markers
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(90, 105, 128));
        g2.drawString("SECTOR A-01", 10, 20);
        g2.drawString("CENTRAL CITY AIRSPACE", w - 160, 20);
    }

    private void drawWeatherZones(Graphics2D g2) {
        for (WeatherZone wz : engine.getWeatherZones()) {
            Location c = wz.getCenter();
            double r = wz.getRadius();
            double diameter = r * 2;

            // Fill transparent storm area
            g2.setColor(wz.getHazardType().getOverlayColor());
            g2.fill(new Ellipse2D.Double(c.getX() - r, c.getY() - r, diameter, diameter));

            // Hazard border
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
            g2.setColor(new Color(255, 140, 0, 180));
            g2.draw(new Ellipse2D.Double(c.getX() - r, c.getY() - r, diameter, diameter));

            // Label
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.setColor(Color.WHITE);
            g2.drawString("⚠ " + wz.getHazardType().getLabel(), (int) (c.getX() - 35), (int) c.getY());
        }
    }

    private void drawChargingHub(Graphics2D g2) {
        ChargingHub hub = engine.getChargingHub();
        Location loc = hub.getLocation();
        int size = 46;
        int x = (int) loc.getX() - size / 2;
        int y = (int) loc.getY() - size / 2;

        // Hub building icon
        g2.setColor(new Color(33, 150, 243, 160));
        g2.fill(new RoundRectangle2D.Double(x, y, size, size, 12, 12));
        g2.setColor(new Color(100, 181, 246));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(x, y, size, size, 12, 12));

        // Icon text
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("HUB", x + 10, y + 20);

        // Slots status
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.setColor(hub.getAvailablePads() > 0 ? Color.GREEN : Color.RED);
        g2.drawString(hub.getAvailablePads() + "/" + hub.getTotalPads() + " Free", x + 5, y + 36);
    }

    private void drawDrones(Graphics2D g2) {
        for (Drone d : engine.getFleet().values()) {
            Location loc = d.getCurrentLocation();
            int x = (int) loc.getX();
            int y = (int) loc.getY();

            // Destination flight path line if moving
            if (d.getTargetLocation() != null) {
                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0));
                g2.setColor(new Color(0, 230, 118, 140));
                g2.drawLine(x, y, (int) d.getTargetLocation().getX(), (int) d.getTargetLocation().getY());

                // Target flag
                g2.setColor(new Color(255, 64, 129));
                g2.fillRect((int) d.getTargetLocation().getX() - 3, (int) d.getTargetLocation().getY() - 3, 6, 6);
            }

            // Selection pulse ring
            if (selectedDrone != null && selectedDrone.getId().equals(d.getId())) {
                g2.setColor(new Color(0, 229, 255, 180));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x - 22, y - 22, 44, 44);
            }

            // Drone shape & color based on model/status
            Color droneColor = getDroneColor(d);
            g2.setColor(droneColor);
            g2.fillOval(x - 10, y - 10, 20, 20);

            // Drone rotor arms
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x - 14, y - 14, x + 14, y + 14);
            g2.drawLine(x - 14, y + 14, x + 14, y - 14);

            // Rotors
            g2.setColor(Color.WHITE);
            g2.drawOval(x - 16, y - 16, 5, 5);
            g2.drawOval(x + 11, y - 16, 5, 5);
            g2.drawOval(x - 16, y + 11, 5, 5);
            g2.drawOval(x + 11, y + 11, 5, 5);

            // Drone ID label
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.setColor(Color.WHITE);
            g2.drawString(d.getId(), x - 18, y - 18);

            // Mini battery indicator bar
            int barWidth = 24;
            int barHeight = 4;
            int barX = x - barWidth / 2;
            int barY = y + 15;

            g2.setColor(new Color(30, 30, 30));
            g2.fillRect(barX, barY, barWidth, barHeight);

            Color batColor = (d.getBatteryLevel() > 50) ? Color.GREEN :
                    (d.getBatteryLevel() > 25) ? Color.ORANGE : Color.RED;
            g2.setColor(batColor);
            int fillW = (int) ((d.getBatteryLevel() / 100.0) * barWidth);
            g2.fillRect(barX, barY, fillW, barHeight);
        }
    }

    private Color getDroneColor(Drone d) {
        switch (d.getStatus()) {
            case DELIVERING:
                return new Color(0, 230, 118); // Bright Green
            case EN_ROUTE_PICKUP:
                return new Color(255, 214, 0); // Yellow
            case CHARGING:
                return new Color(41, 121, 255); // Blue
            case WAITING_FOR_DOCK:
                return new Color(170, 0, 255); // Purple
            case EMERGENCY_LANDED:
                return new Color(255, 23, 68); // Red
            default:
                return new Color(158, 158, 158); // Gray for idle
        }
    }
}
