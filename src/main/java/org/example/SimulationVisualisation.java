package org.example;

import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

class SimulationVisualisation {
    private final int windowSize = 600;
    private final int roadWidth = 100;
    private final int laneLineWidth = 30;
    private final int vehicleWidth = 25;
    private final int padding = 5;
    private final JFrame mainFrame;
    private final JLayeredPane layeredPane;
    private final Color roadColor = new Color(26, 26, 26);
    private final int[] northEndPos;
    private final int[] eastEndPos;
    private final int[] southEndPos;
    private final int[] westEndPos;
    private final int maxVehiclesToDisplay;
    public RoadPanel westRoad;
    public RoadPanel southRoad;
    public RoadPanel eastRoad;
    public RoadPanel northRoad;
    public TrafficLightPanel trafficLightN;
    public TrafficLightPanel trafficLightS;
    public TrafficLightPanel trafficLightW;
    public TrafficLightPanel trafficLightE;

    public SimulationVisualisation() {
        mainFrame = new JFrame("Intersection Visualisation");
        mainFrame.setSize(windowSize, windowSize);
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        layeredPane = new JLayeredPane();
        mainFrame.add(layeredPane);
        maxVehiclesToDisplay = Math.min((windowSize - roadWidth) / 2 / (vehicleWidth + padding) + 1, 8);

        // Final position of a vehicle panel, after going through intersection
        northEndPos = new int[]{windowSize / 2 + (roadWidth / 2 - vehicleWidth) / 2 + padding / 2, -vehicleWidth};
        eastEndPos = new int[]{windowSize + vehicleWidth, windowSize / 2 + padding / 2 + ((roadWidth - padding) / 2 - vehicleWidth) / 2};
        southEndPos = new int[]{(windowSize - roadWidth) / 2 + ((roadWidth - padding) / 2 - vehicleWidth) / 2, windowSize + vehicleWidth};
        westEndPos = new int[]{-vehicleWidth, (windowSize - roadWidth) / 2 + ((roadWidth - padding) / 2 - vehicleWidth) / 2};

        Rectangle westRoadBounds = new Rectangle(0, (windowSize - roadWidth) / 2, (windowSize - roadWidth) / 2, roadWidth);
        Rectangle eastRoadBounds = new Rectangle((windowSize + roadWidth) / 2, (windowSize - roadWidth) / 2, (windowSize - roadWidth) / 2, roadWidth);
        Rectangle northRoadBounds = new Rectangle((windowSize - roadWidth) / 2, 0, roadWidth, (windowSize - roadWidth) / 2);
        Rectangle southRoadBounds = new Rectangle((windowSize - roadWidth) / 2, (windowSize + roadWidth) / 2, roadWidth, (windowSize - roadWidth) / 2);

        westRoad = new RoadPanel(laneLineWidth, laneLineWidth, vehicleWidth, 'w', westRoadBounds, maxVehiclesToDisplay);
        southRoad = new RoadPanel(laneLineWidth, laneLineWidth, vehicleWidth, 's', southRoadBounds, maxVehiclesToDisplay);
        eastRoad = new RoadPanel(laneLineWidth, laneLineWidth, vehicleWidth, 'e', eastRoadBounds, maxVehiclesToDisplay);
        northRoad = new RoadPanel(laneLineWidth, laneLineWidth, vehicleWidth, 'n', northRoadBounds, maxVehiclesToDisplay);
        JPanel roadsIntersection = new JPanel();
        roadsIntersection.setBounds((windowSize - roadWidth) / 2, (windowSize - roadWidth) / 2, roadWidth, roadWidth);

        trafficLightN = new TrafficLightPanel('n', (windowSize - roadWidth) / 2 - padding - 40, (windowSize - roadWidth) / 2 - 4 * padding - 100);
        trafficLightS = new TrafficLightPanel('s', (windowSize + roadWidth) / 2 + padding, (windowSize + roadWidth) / 2 + 4 * padding);
        trafficLightW = new TrafficLightPanel('w', (windowSize + roadWidth) / 2 + 4 * padding, (windowSize - roadWidth) / 2 - padding - 40);
        trafficLightE = new TrafficLightPanel('e', (windowSize - roadWidth) / 2 - 4 * padding - 100, (windowSize + roadWidth) / 2 + padding);

        westRoad.setBackground(roadColor);
        southRoad.setBackground(roadColor);
        eastRoad.setBackground(roadColor);
        northRoad.setBackground(roadColor);
        roadsIntersection.setBackground(roadColor);

        layeredPane.add(westRoad, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(southRoad, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(eastRoad, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(northRoad, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(roadsIntersection, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(trafficLightN, 10);
        layeredPane.add(trafficLightS, 10);
        layeredPane.add(trafficLightW, 10);
        layeredPane.add(trafficLightE, 10);
    }

    public void CloseWindow() {
        mainFrame.dispose();
    }

    public void AddVehicleToIntersection(Simulation.Vehicle vehicle) {
        switch (vehicle.startRoad()) {
            case "north":
                northRoad.AddVehicleToRoad(vehicle, true);
                break;
            case "south":
                southRoad.AddVehicleToRoad(vehicle, true);
                break;
            case "west":
                westRoad.AddVehicleToRoad(vehicle, true);
                break;
            case "east":
                eastRoad.AddVehicleToRoad(vehicle, true);
                break;
            default:
                break;
        }
    }

    // Road's first vehicle exits intersection and all the others move forward
    public void MoveRoadsFirstVehicle(RoadPanel roadPanel) throws InterruptedException {
        VehiclePanel firstVehicle = roadPanel.vehiclePanels.removeFirst();
        MoveVehicleToDestination(firstVehicle);
        roadPanel.UpdateNextNewVehiclePosition(-1);
        if (!roadPanel.vehiclesToAdd.isEmpty()) {
            roadPanel.AddVehicleToRoad(roadPanel.vehiclesToAdd.removeFirst(), false);
            Thread.sleep(500);
        }
        Thread.sleep(500);
        roadPanel.MoveVehiclesToIntersection();
        firstVehicle.vehicleClass = null;
        mainFrame.remove(firstVehicle);
        firstVehicle = null;
    }

    // Moving through the intersection in an L shape
    public void MoveVehicleToDestination(VehiclePanel vehiclePanel) {

        final int[] finalPos = switch (vehiclePanel.vehicleClass.endRoad()) {
            case "north" -> northEndPos;
            case "south" -> southEndPos;
            case "east" -> eastEndPos;
            case "west" -> westEndPos;
            default -> new int[]{0, 0};
        };
        final int[] firstTurn = switch (vehiclePanel.vehicleClass.startRoad()) {
            case "north", "south" -> new int[]{vehiclePanel.lastPosition[0], finalPos[1]};
            case "east", "west" -> new int[]{finalPos[0], vehiclePanel.lastPosition[1]};
            default -> new int[]{0, 0};
        };

        vehiclePanel.moveToFinalPosition(firstTurn[0], firstTurn[1]);
        vehiclePanel.moveToFinalPosition(finalPos[0], finalPos[1]);
    }

    public void DrawVisualisation() {
        mainFrame.setVisible(true);
    }
}

class RoadPanel extends JPanel {
    public final ArrayList<VehiclePanel> vehiclePanels;
    public final ArrayList<Simulation.Vehicle> vehiclesToAdd;
    private final int lineWidth;
    private final int lineSpace;
    private final int[] vehicleSpawnPos;
    private final int[] nextAddedVehiclePosDelta;
    private final int padding;
    private final int[] nextAddedVehiclePos;
    private final int vehicleSize;
    private final int maxVehiclesToDisplay;

    public RoadPanel(int lineWidth, int lineSpace, int vehicleSize, char dir, Rectangle dimensions, int maxVehiclesToDisplay) {
        this.maxVehiclesToDisplay = maxVehiclesToDisplay;
        this.lineWidth = lineWidth;
        this.lineSpace = lineSpace;
        this.padding = 5;
        this.vehicleSize = vehicleSize;
        this.setBounds(dimensions);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.vehiclePanels = new ArrayList<>();
        this.vehiclesToAdd = new ArrayList<>();

        // First position of a vehicle panel before animating it
        this.vehicleSpawnPos = switch (dir) {
            case 's' ->
                    new int[]{dimensions.x + dimensions.width / 2 + padding / 2 + ((dimensions.width - padding) / 2 - vehicleSize) / 2, dimensions.y + dimensions.height + vehicleSize};
            case 'n' -> new int[]{dimensions.x + ((dimensions.width - padding) / 2 - vehicleSize) / 2, -vehicleSize};
            case 'w' ->
                    new int[]{-vehicleSize, dimensions.y + dimensions.height / 2 + padding / 2 + ((dimensions.height - padding) / 2 - vehicleSize) / 2};
            case 'e' ->
                    new int[]{dimensions.x + dimensions.width + vehicleSize, dimensions.y + ((dimensions.height - padding) / 2 - vehicleSize) / 2};
            default -> new int[]{0, 0};
        };

        // For updating the position for next waiting vehicle
        this.nextAddedVehiclePosDelta = switch (dir) {
            case 's' -> new int[]{0, vehicleSize + padding};
            case 'w' -> new int[]{-vehicleSize - padding, 0};
            case 'n' -> new int[]{0, -vehicleSize - padding};
            case 'e' -> new int[]{vehicleSize + padding, 0};
            default -> new int[]{0, 0};
        };

        // For setting the position for next waiting vehicle
        this.nextAddedVehiclePos = switch (dir) {
            case 's' ->
                    new int[]{dimensions.x + dimensions.width / 2 + padding / 2 + ((dimensions.width - padding) / 2 - vehicleSize) / 2, dimensions.y + padding};
            case 'n' ->
                    new int[]{dimensions.x + ((dimensions.width - padding) / 2 - vehicleSize) / 2, dimensions.height - vehicleSize - padding};
            case 'w' ->
                    new int[]{dimensions.width - padding - vehicleSize, dimensions.y + dimensions.height / 2 + padding / 2 + ((dimensions.height - padding) / 2 - vehicleSize) / 2};
            case 'e' ->
                    new int[]{dimensions.x + padding, dimensions.y + ((dimensions.height - padding) / 2 - vehicleSize) / 2};
            default -> new int[]{0, 0};
        };
    }

    // For choosing unique color for a vehicle based on its vehicleId
    public static Color getColorFromString(String input) {
        try {
            // Use SHA-256 to hash the string
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());

            // Extract RGB components from the hash
            int red = Byte.toUnsignedInt(hashBytes[0]); // First byte for red
            int green = Byte.toUnsignedInt(hashBytes[1]); // Second byte for green
            int blue = Byte.toUnsignedInt(hashBytes[2]); // Third byte for blue

            return new Color(red, green, blue);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // Adding a new vehicle to the intersection, with an option to animate it
    public void AddVehicleToRoad(Simulation.Vehicle vehicle, boolean animate) {
        if (vehiclePanels.size() >= maxVehiclesToDisplay) {
            vehiclesToAdd.add(vehicle);
            return;
        }
        VehiclePanel vehiclePanel = new VehiclePanel(vehicle);
        vehiclePanel.setSize(vehicleSize, vehicleSize);
        vehiclePanel.setBackground(getColorFromString(vehiclePanel.vehicleClass.vehicleId()));
        if (animate) {
            vehiclePanel.setLocation(this.vehicleSpawnPos[0], this.vehicleSpawnPos[1]);
            vehiclePanel.lastPosition = Arrays.copyOf(this.vehicleSpawnPos, 2);
            MoveVehicleToStartPosition(vehiclePanel);
            UpdateNextNewVehiclePosition(1);
        } else {
            UpdateNextNewVehiclePosition(1);
            vehiclePanel.setLocation(nextAddedVehiclePos[0], nextAddedVehiclePos[1]);
            vehiclePanel.lastPosition = new int[]{nextAddedVehiclePos[0], nextAddedVehiclePos[1]};
        }
        getParent().add(vehiclePanel, JLayeredPane.DRAG_LAYER);
        vehiclePanel.repaint();
        vehiclePanels.add(vehiclePanel);
    }

    public void UpdateNextNewVehiclePosition(int i) {
        // If i == 1 then next vehicle position in moved backwards and if i == -1 then it is moved towards the intersection
        nextAddedVehiclePos[0] += i * nextAddedVehiclePosDelta[0];
        nextAddedVehiclePos[1] += i * nextAddedVehiclePosDelta[1];
    }

    public void MoveVehicleToStartPosition(VehiclePanel vehiclePanel) {
        final int[] finalPos = {nextAddedVehiclePos[0], nextAddedVehiclePos[1]};
        vehiclePanel.moveToFinalPosition(finalPos[0], finalPos[1]);
    }

    public void MoveVehiclesToIntersection() {
        for (VehiclePanel vehiclePanel : vehiclePanels) {
            int[] position = vehiclePanel.lastPosition; // [x, y]
            final int[] finalPos = {position[0] + (-1) * nextAddedVehiclePosDelta[0], position[1] + (-1) * nextAddedVehiclePosDelta[1]};
            vehiclePanel.moveToFinalPosition(finalPos[0], finalPos[1]);
        }
    }

    // Adding dashed line through the middle of the road
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Define the dashed stroke
        float[] dashPattern = {lineWidth, lineSpace}; // Dash length and space between dashes
        Stroke dashedStroke = new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0);
        g2d.setStroke(dashedStroke);

        // Set the color for the line
        g2d.setColor(Color.WHITE);

        // Calculate the middle of the panel
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int x1 = panelHeight >= panelWidth ? panelWidth / 2 : 0;
        int y1 = panelHeight >= panelWidth ? 0 : panelHeight / 2;
        int x2 = panelHeight >= panelWidth ? panelWidth / 2 : panelWidth;
        int y2 = panelHeight >= panelWidth ? panelHeight : panelHeight / 2;

        // Draw the dashed line
        g2d.drawLine(x1, y1, x2, y2);
    }
}

class TrafficLightPanel extends JPanel {
    private final int trafficLightWidth = 40;
    private final int trafficLightHeight = 100;
    CircularPanel lightTop;
    CircularPanel lightMiddle;
    CircularPanel lightBottom;

    public TrafficLightPanel(char dir, int xPos, int yPos) {
        // Set the dimensions of the traffic light panel
        int width = dir == 'e' || dir == 'w' ? trafficLightHeight : trafficLightWidth;
        int height = dir == 'n' || dir == 's' ? trafficLightHeight : trafficLightWidth;

        int lightRadius = 26;
        Rectangle topLightBounds = switch (dir) {
            case 'n' -> new Rectangle(7, 7, lightRadius, lightRadius);
            case 's' -> new Rectangle(7, 67, lightRadius, lightRadius);
            case 'e' -> new Rectangle(7, 7, lightRadius, lightRadius);
            case 'w' -> new Rectangle(67, 7, lightRadius, lightRadius);
            default -> new Rectangle(0, 0, 0, 0);
        };
        Rectangle middleLightBounds = switch (dir) {
            case 'n' -> new Rectangle(7, 37, lightRadius, lightRadius);
            case 's' -> new Rectangle(7, 37, lightRadius, lightRadius);
            case 'e' -> new Rectangle(37, 7, lightRadius, lightRadius);
            case 'w' -> new Rectangle(37, 7, lightRadius, lightRadius);
            default -> new Rectangle(0, 0, 0, 0);
        };
        Rectangle bottomLightBounds = switch (dir) {
            case 'n' -> new Rectangle(7, 67, lightRadius, lightRadius);
            case 's' -> new Rectangle(7, 7, lightRadius, lightRadius);
            case 'e' -> new Rectangle(67, 7, lightRadius, lightRadius);
            case 'w' -> new Rectangle(7, 7, lightRadius, lightRadius);
            default -> new Rectangle(0, 0, 0, 0);
        };


        setBounds(xPos, yPos, width, height);
        setBackground(Color.BLACK);
        setLayout(null); // Disable layout manager for manual positioning

        // Add the colored lights (squares in this case)
        lightTop = new CircularPanel(Color.gray);
        lightMiddle = new CircularPanel(Color.gray);
        lightBottom = new CircularPanel(Color.gray);

        // Position each light inside the traffic light panel
        lightTop.setBounds(topLightBounds);       // Top light
        lightMiddle.setBounds(middleLightBounds);   // Middle light
        lightBottom.setBounds(bottomLightBounds);   // Bottom light

        // Add the lights to the traffic light panel
        add(lightTop);
        add(lightMiddle);
        add(lightBottom);
    }

    public void setLight(Color color) {
        lightTop.lightColor = color == Color.green ? Color.green : Color.gray;
        lightMiddle.lightColor = color == Color.yellow ? Color.yellow : Color.gray;
        lightBottom.lightColor = color == Color.red ? Color.red : Color.gray;

        // Repaint all lights
        lightTop.repaint();
        lightMiddle.repaint();
        lightBottom.repaint();
        this.repaint();
    }

    // Inner class for individual light panels for making light circular
    static class CircularPanel extends JPanel {
        private Color lightColor;

        public CircularPanel(Color color) {
            this.lightColor = color;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            // Enable antialiasing for smooth edges
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw the square
            g2d.setColor(lightColor);
            g2d.fillOval(0, 0, getWidth(), getHeight());
        }
    }
}


class VehiclePanel extends JPanel {
    private final int positionDelta = 8;
    public Simulation.Vehicle vehicleClass;
    public int[] lastPosition;
    public Timer activeTimer;

    public VehiclePanel(Simulation.Vehicle vehicleClass) {
        this.vehicleClass = vehicleClass;
        activeTimer = new Timer(0, null);
    }

    public void moveToFinalPosition(int finalPosX, int finalPosY) {
        // Wait for any current movement to finish
        if (activeTimer != null && activeTimer.isRunning()) {
            while (activeTimer.isRunning()) {
                continue;
            }
        }

        int[] currentPos = new int[]{lastPosition[0], lastPosition[1]};

        // Create a new timer for movement
        activeTimer = new Timer(8, _ -> {
            // Calculate movement step
            int deltaX = finalPosX - currentPos[0];
            int deltaY = finalPosY - currentPos[1];

            currentPos[0] = currentPos[0] + (int) Math.signum(deltaX) * Math.min(Math.abs(deltaX), positionDelta);
            currentPos[1] = currentPos[1] + (int) Math.signum(deltaY) * Math.min(Math.abs(deltaY), positionDelta);

            if (deltaX == 0 && deltaY == 0) {
                // Stop the timer if the final position is reached
                lastPosition = new int[]{finalPosX, finalPosY};
                activeTimer.stop();
            } else {
                // Move the panel closer to the final position
                setLocation(currentPos[0], currentPos[1]);
            }
        });

        // Start the timer
        activeTimer.start();
    }
}