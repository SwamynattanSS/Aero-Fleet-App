package com.aerofleet.ui;

import com.aerofleet.concurrency.SimulationEngine;
import com.aerofleet.model.*;
import com.aerofleet.persistence.DatabaseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

/**
 * Main application window for the AeroFleet simulator.
 * Unit V: Swing components, Layout Managers (BorderLayout, JSplitPane), Event Handling, Adapters.
 */
public class MainFrame extends JFrame {

    private final SimulationEngine engine;
    private final GridMapPanel mapPanel;
    private final TelemetryPanel telemetryPanel;
    private final MissionLogPanel logPanel;
    private final JLabel lblStatusBar = new JLabel("System Ready");
    private final Random random = new Random();

    public MainFrame() {
        super("AeroFleet: Autonomous Drone Delivery & Fleet Control Simulator");
        this.engine = new SimulationEngine();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 840);
        setMinimumSize(new Dimension(980, 720));
        setLocationRelativeTo(null);

        // Child components
        this.mapPanel = new GridMapPanel(engine);
        this.telemetryPanel = new TelemetryPanel(engine);
        this.logPanel = new MissionLogPanel();

        buildLayout();
        setupToolbar();
        setupSimulationLoop();
        setupWindowEvents();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());

        // Connect map selection to telemetry panel
        mapPanel.setOnDroneSelectedListener(telemetryPanel::displayDrone);

        // Split Pane: Left is Radar Map, Right is Telemetry Inspector
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, telemetryPanel);
        horizontalSplit.setResizeWeight(0.72);
        horizontalSplit.setDividerSize(6);
        horizontalSplit.setBorder(null);

        // Split Pane: Upper is Map + Sidebar, Lower is Flight Logs Table
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplit, logPanel);
        verticalSplit.setResizeWeight(0.75);
        verticalSplit.setDividerSize(6);
        verticalSplit.setBorder(null);

        add(verticalSplit, BorderLayout.CENTER);

        // Bottom status bar
        JPanel statusBarPanel = new JPanel(new BorderLayout());
        statusBarPanel.setBackground(new Color(20, 24, 33));
        statusBarPanel.setBorder(new EmptyBorder(4, 12, 4, 12));

        lblStatusBar.setForeground(new Color(0, 229, 255));
        lblStatusBar.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusBarPanel.add(lblStatusBar, BorderLayout.WEST);

        String dbStatus = DatabaseManager.getInstance().isConnected() ? "SQLite (Connected)" : "In-Memory Mode";
        JLabel lblDb = new JLabel("DB Storage: " + dbStatus + " | Engine: 30 FPS");
        lblDb.setForeground(new Color(150, 160, 180));
        lblDb.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusBarPanel.add(lblDb, BorderLayout.EAST);

        add(statusBarPanel, BorderLayout.SOUTH);
    }

    private void setupToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBackground(new Color(26, 31, 42));
        toolbar.setBorder(new EmptyBorder(6, 10, 6, 10));

        JButton btnDispatch = createToolbarButton("📦 Dispatch Delivery", new Color(0, 200, 83), Color.BLACK);
        btnDispatch.addActionListener(e -> {
            DispatchDialog dialog = new DispatchDialog(this, engine);
            dialog.setVisible(true);
        });

        JButton btnAddQuad = createToolbarButton("+ Add QuadCopter", new Color(55, 71, 79), Color.WHITE);
        btnAddQuad.addActionListener(e -> {
            int seq = engine.getFleet().size() + 1;
            engine.addDrone(new QuadCopter("QD-10" + seq, new Location(60 + random.nextInt(200), 60 + random.nextInt(200))));
            mapPanel.repaint();
        });

        JButton btnAddHexa = createToolbarButton("+ Add HexaHeavyLift", new Color(55, 71, 79), Color.WHITE);
        btnAddHexa.addActionListener(e -> {
            int seq = engine.getFleet().size() + 1;
            engine.addDrone(new HexaHeavyLift("HX-20" + seq, new Location(300 + random.nextInt(200), 300 + random.nextInt(200))));
            mapPanel.repaint();
        });

        JButton btnAddWing = createToolbarButton("+ Add FixedWing", new Color(55, 71, 79), Color.WHITE);
        btnAddWing.addActionListener(e -> {
            int seq = engine.getFleet().size() + 1;
            engine.addDrone(new LongRangeFixedWing("FW-30" + seq, new Location(100 + random.nextInt(200), 400 + random.nextInt(150))));
            mapPanel.repaint();
        });

        JButton btnSpawnStorm = createToolbarButton("⛈ Trigger Storm Hazard", new Color(138, 43, 226), Color.WHITE);
        btnSpawnStorm.addActionListener(e -> {
            int wx = 100 + random.nextInt(400);
            int wy = 100 + random.nextInt(400);
            engine.getWeatherZones().add(new WeatherZone("WZ-" + System.currentTimeMillis(),
                    new Location(wx, wy), 65.0, WeatherZone.HazardType.THUNDERSTORM));
            mapPanel.repaint();
        });

        JButton btnClearStorms = createToolbarButton("☀ Clear Storms", new Color(255, 152, 0), Color.BLACK);
        btnClearStorms.addActionListener(e -> {
            engine.getWeatherZones().clear();
            mapPanel.repaint();
        });

        toolbar.add(btnDispatch);
        toolbar.addSeparator(new Dimension(15, 0));
        toolbar.add(btnAddQuad);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(btnAddHexa);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(btnAddWing);
        toolbar.addSeparator(new Dimension(15, 0));
        toolbar.add(btnSpawnStorm);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(btnClearStorms);

        add(toolbar, BorderLayout.NORTH);
    }

    private JButton createToolbarButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        return b;
    }

    /**
     * Decoupled 30 FPS Render Timer running on Event Dispatch Thread (Unit V).
     */
    private void setupSimulationLoop() {
        Timer renderTimer = new Timer(33, e -> {
            mapPanel.repaint();
            telemetryPanel.updateFleetMetrics();

            lblStatusBar.setText(String.format("Fleet: %d Drones Active | In Flight: %d | Deliveries: %d | Charging Hub: %s",
                    engine.getFleet().size(),
                    engine.getActiveInTransitCount(),
                    engine.getTotalDeliveries(),
                    engine.getChargingHub().toString()
            ));
        });
        renderTimer.start();
    }

    private void setupWindowEvents() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                engine.shutdown();
            }
        });
    }
}
