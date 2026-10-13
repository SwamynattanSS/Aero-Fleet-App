# AeroFleet: Autonomous Drone Delivery & Fleet Control Simulator

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Swing GUI](https://img.shields.io/badge/GUI-Java%20Swing%2FAWT-5382a1?style=for-the-badge)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![Database](https://img.shields.io/badge/Database-SQLite%20JDBC-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

**AeroFleet** is an interactive, multi-threaded desktop simulation engine where you control an autonomous delivery drone fleet navigating across a simulated 2D city grid. The system handles real-time flight physics, battery drain, weather hazard evasion, concurrent docking synchronization, package dispatching, and persistent flight telemetry logging.

This project was engineered to comprehensively demonstrate all 5 units of the **CS23304 Java Programming** syllabus.

---

## 🗺 System Architecture

```text
+----------------------------------------------------------------------------------+
|                            AeroFleet Swing GUI Dashboard                         |
|  +---------------------------------------------+-------------------------------+ |
|  |             GridMapPanel (2D Canvas)        |      TelemetryPanel           | |
|  |  * Custom paintComponent rendering         |  * Live Battery JProgressBar  | |
|  |  * City grid, storm zones, charging hubs    |  * Coordinates, Status Badge  | |
|  |  * Real-time animated drone positions       |  * Streams Fleet Statistics   | |
|  +---------------------------------------------+-------------------------------+ |
|  |           MissionLogPanel (JTable of Flight Telemetry & Blackbox Records)   | |
+--+-------------------------------------------------------------------------------+--+
                                        ▲
                                        │  30 FPS Repaint Timer & Event Listeners
                                        ▼
+----------------------------------------------------------------------------------+
|                            Simulation Engine (Concurrency)                       |
|  * ConcurrentHashMap<String, Drone> Fleet Registry                               |
|  * Independent Drone Threads (Runnable physics loop at 10 ticks/sec)             |
|  * ChargingHub: synchronized docking pads with wait() and notifyAll()            |
|  * Streams API: Nearest-neighbor drone filtering & capacity checks               |
+----------------------------------------------------------------------------------+
          ▲                                                    ▲
          │ Object Serialization                               │ JDBC Driver
          ▼                                                    ▼
+-----------------------------+               +------------------------------------+
|  aerofleet_blackbox.dat     |               |  aerofleet.db (SQLite)             |
|  (FlightLogSerializer)      |               |  (DatabaseManager PreparedStatement|
+-----------------------------+               +------------------------------------+
```

---

## 📚 Complete Syllabus Integration (CS23304)

### Unit I: Introduction to Java & OOP
* **Classes & Encapsulation:** Private properties, getters, setters, and constructors in `Location`, `PackageCargo`, and `Drone`.
* **Date & Time API:** Uses `java.time.LocalDateTime` and `DateTimeFormatter` for tracking delivery launch and arrival times.
* **Static Members & Constants:** Atomic static counter `ID_GENERATOR` for incremental tracking IDs; class constants like `LOW_BATTERY_WARNING = 25.0`.
* **`this` Keyword & Math API:** Coordinate distance calculations using `Math.sqrt()` and trigonometry `Math.atan2()`.

### Unit II: Polymorphism & Inheritance
* **Abstract Base Class:** `Drone` defines common behaviors (`moveToward()`, `recharge()`, `tickPhysics()`).
* **Subclasses & Overriding:**
  * `QuadCopter`: High agility, lighter payload ($3.0\text{ kg}$).
  * `HexaHeavyLift`: Industrial payload ($10.0\text{ kg}$), wind-resistant dual-battery mechanics.
  * `LongRangeFixedWing`: Aerodynamic cruising model for rapid long-range transport.
* **Interfaces:** `Navigable` (navigation & heading mechanics) and `ChargerCompatible` (battery charging rates).
* **`instanceof` & Downcasting:** Used in dispatcher checks to inspect specialized drone subclasses before allocating critical payloads.

### Unit III: Exception Handling & Multithreading
* **Custom User-Defined Exceptions:**
  * `BatteryDepletedException`: Thrown if a drone runs out of battery before reaching a hub.
  * `PayloadExceededException`: Thrown when a cargo exceeds the drone's rated payload capacity.
  * `NoRouteFoundException`: Thrown when no idle drone has sufficient battery or range.
* **Concurrent Flight Threads:** Every active drone implements `Runnable` and executes concurrently in an `ExecutorService` thread pool.
* **Thread Synchronization:**
  * `ChargingHub` implements `synchronized` docking pads.
  * When both pads are occupied, arriving drones invoke `wait()` to pause their threads until an undocking drone calls `notifyAll()`, completely avoiding deadlocks and race conditions.

### Unit IV: File Streams, Databases & Modern Java Features
* **Java Streams API & Lambdas:**
  * Filters available drones by status, battery level, and payload capacity using `.filter()`.
  * Computes fleet metrics with `.mapToDouble()`, `.average()`, and `.mapToInt().sum()`.
  * Selects closest eligible drone using `.min(Comparator.comparingDouble(...))`.
* **Regular Expressions:** `PackageCargo.PHONE_REGEX` validates recipient contact numbers (e.g., `^[+]?[0-9]{10,13}$`).
* **Object Serialization:** Binary serialization and deserialization of `FlightLog` instances to `aerofleet_flight_blackbox.dat` via `ObjectOutputStream` and `ObjectInputStream`.
* **Database Connectivity (JDBC):** Uses SQLite via `DatabaseManager` to persist completed missions and telemetry using `PreparedStatement` and query with `ResultSet`.
* **File I/O Streams:** Exports live mission logs directly to a `.csv` file using `BufferedWriter` and `FileWriter`.

### Unit V: GUI Development & Frameworks (AWT / Swing)
* **Custom 2D Graphics:** `GridMapPanel` overrides `paintComponent(Graphics g)` to render animated drones, radar grids, rotating blade indicators, and semi-transparent weather zones.
* **Decoupled 30 FPS Render Loop:** A dedicated `javax.swing.Timer` repaints the UI at 30 FPS on the Event Dispatch Thread (EDT) without freezing the background drone physics threads.
* **Swing Components & Layouts:** `JFrame`, `JSplitPane`, `JTable`, `JProgressBar`, `JToolBar`, `JSpinner`, `JComboBox`, `TitledBorder`.
* **Event Handling:** `MouseAdapter` click listeners to select drones on the canvas, `ActionListener` lambdas, and modal `JDialog` forms.

---

## 🚀 How to Run the Project

### Prerequisites
* **Java Development Kit (JDK) 17 or higher**
* (Optional) **Apache Maven** (if running via command line)

### Option 1: Run with Maven (Recommended)
```bash
# Clone the repository
git clone https://github.com/SwamynattanSS/Aero-Fleet-App.git
cd Aero-Fleet-App

# Compile and launch the simulator
mvn clean compile exec:java
```

### Option 2: Open in any IDE
* **IntelliJ IDEA:** Open folder $\rightarrow$ Select `pom.xml` as project $\rightarrow$ Run `Main.java`.
* **Eclipse:** File $\rightarrow$ Import $\rightarrow$ Existing Maven Projects $\rightarrow$ Run as Java Application.
* **NetBeans:** File $\rightarrow$ Open Project $\rightarrow$ Run.
* **VS Code:** Open folder with the Java Extension Pack installed $\rightarrow$ Click **Run** on `Main.java`.

### Option 3: Compile and Run via `javac` (Zero Dependencies)
```bash
# Compile all source files
javac -d bin src/main/java/com/aerofleet/model/*.java \
             src/main/java/com/aerofleet/exceptions/*.java \
             src/main/java/com/aerofleet/concurrency/*.java \
             src/main/java/com/aerofleet/persistence/*.java \
             src/main/java/com/aerofleet/ui/*.java \
             src/main/java/com/aerofleet/Main.java

# Run the simulator
java -cp bin com.aerofleet.Main
```

---

## 🎯 Viva & Defense Q&A Cheatsheet

| Question | Code Reference & Answer |
| :--- | :--- |
| **Q1. How do you prevent UI freezing when drones are flying?** | "We decoupled rendering from physics. Drones run in background threads (`Runnable` in `ExecutorService`), while the GUI is updated at 30 FPS using `javax.swing.Timer` on the Event Dispatch Thread (EDT)." |
| **Q2. How is thread synchronization implemented?** | "`ChargingHub` has limited pads. When all pads are occupied, drone threads invoke `wait()`. Once a drone finishes recharging, it releases the pad and triggers `notifyAll()` to wake up waiting drones without race conditions or deadlocks." |
| **Q3. Where is the Streams API used?** | "In `SimulationEngine.dispatchPackage()`, where we use `.filter()` to check battery and payload, and `.min(Comparator.comparingDouble(...))` to find the nearest drone using a lambda expression." |
| **Q4. What is the difference between File I/O and Serialization here?** | "We use Object Serialization (`ObjectOutputStream`) for saving and restoring full binary `FlightLog` state objects, while standard `BufferedWriter` is used for exporting CSV spreadsheets." |
| **Q5. How does JDBC work without a separate SQL server?** | "We use SQLite (`sqlite-jdbc`). It stores data inside a portable local file (`aerofleet.db`) using `PreparedStatement` and `ResultSet` without requiring a standalone MySQL server." |

---

## 📜 License
This project is open-source and released under the MIT License.
