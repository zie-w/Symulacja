package org.example;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulation {
    public Intersection intersection;
    public int simSpeed = 500;
    public boolean isVisualisationEnabled;
    SimulationVisualisation intersectionVisualisation;

    public Simulation(boolean isVisualisationEnabled) {
        intersection = new Intersection();
        this.isVisualisationEnabled = isVisualisationEnabled;
        if (!isVisualisationEnabled)
            return;

        intersectionVisualisation = new SimulationVisualisation();
        intersectionVisualisation.DrawVisualisation();
        drawTrafficLights();
    }

    // Function to execute a command from provided JSON file
    public ArrayList<String> executeCommand(Map<String, String> commandMap) throws InterruptedException {
        ArrayList<String> leftVehicles = new ArrayList<>();

        switch (commandMap.get("type")) {
            case "addVehicle":
                leftVehicles = null;
                Vehicle vehicle = new Vehicle(commandMap.get("vehicleId"), commandMap.get("startRoad"), commandMap.get("endRoad"));
                commandAddVehicle(vehicle);
                break;
            case "step":
                commandStep(leftVehicles);
                break;
            default:
                break;
        }
        return leftVehicles;
    }

    public void commandAddVehicle(Vehicle vehicle) throws InterruptedException {
        if (isVisualisationEnabled) {
            intersection.setLights(Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW);
            drawTrafficLights();
        }

        intersection.addVehicleToRoad(vehicle);

        if (!isVisualisationEnabled)
            return;

        intersectionVisualisation.AddVehicleToIntersection(vehicle);
        Thread.sleep(simSpeed);
    }

    public void commandStep(ArrayList<String> leftVehicles) throws InterruptedException {
        if (isVisualisationEnabled)
            Thread.sleep(2L * simSpeed);

        // Choosing which roads should have their traffic lights set to green
        setTrafficLights();

        if (isVisualisationEnabled) {
            drawTrafficLights();
            Thread.sleep(simSpeed);
        }

        // Vehicles on each road that are closest to the middle of the crossing are leaving
        if (intersection.southTrafficLightColor == Color.green && !intersection.southVehicles.isEmpty()) {
            leftVehicles.add(intersection.southRoadRemoveFirstVehicle().vehicleId);
            if (isVisualisationEnabled)
                intersectionVisualisation.MoveRoadsFirstVehicle(intersectionVisualisation.southRoad);
        }
        if (intersection.westTrafficLightColor == Color.green && !intersection.westVehicles.isEmpty()) {
            leftVehicles.add(intersection.westRoadRemoveFirstVehicle().vehicleId);
            if (isVisualisationEnabled)
                intersectionVisualisation.MoveRoadsFirstVehicle(intersectionVisualisation.westRoad);
        }
        if (intersection.eastTrafficLightColor == Color.green && !intersection.eastVehicles.isEmpty()) {
            leftVehicles.add(intersection.eastRoadRemoveFirstVehicle().vehicleId);
            if (isVisualisationEnabled)
                intersectionVisualisation.MoveRoadsFirstVehicle(intersectionVisualisation.eastRoad);
        }
        if (intersection.northTrafficLightColor == Color.green && !intersection.northVehicles.isEmpty()) {
            leftVehicles.add(intersection.northRoadRemoveFirstVehicle().vehicleId);
            if (isVisualisationEnabled)
                intersectionVisualisation.MoveRoadsFirstVehicle(intersectionVisualisation.northRoad);
        }

        if (isVisualisationEnabled)
            Thread.sleep(simSpeed);
    }

    public void drawTrafficLights() {
        intersectionVisualisation.trafficLightE.setLight(intersection.eastTrafficLightColor);
        intersectionVisualisation.trafficLightW.setLight(intersection.westTrafficLightColor);
        intersectionVisualisation.trafficLightN.setLight(intersection.northTrafficLightColor);
        intersectionVisualisation.trafficLightS.setLight(intersection.southTrafficLightColor);
    }

    // Roads that have more vehicles will have their traffic lights turned green, and if the lights were unchanged for too long then their colors are reversed
    public void setTrafficLights() {
        if (Math.min(intersection.eastLightLastChangedTime, intersection.westLightLastChangedTime) >= intersection.maxWaitTime) {
            intersection.setLights(Color.GREEN, Color.RED, Color.GREEN, Color.RED);
            return;
        } else if (Math.min(intersection.northLightLastChangedTime, intersection.southLightLastChangedTime) >= intersection.maxWaitTime) {
            intersection.setLights(Color.RED, Color.GREEN, Color.RED, Color.GREEN);
            return;
        }

        if (intersection.southVehicles.size() + intersection.northVehicles.size() >= intersection.eastVehicles.size() + intersection.westVehicles.size()) {
            intersection.setLights(Color.RED, Color.GREEN, Color.RED, Color.GREEN);
        } else {
            intersection.setLights(Color.GREEN, Color.RED, Color.GREEN, Color.RED);
        }
    }

    // Main function for processing input JSON file, executing commands and saving results new JSON file
    public void SimulationLoop(File inputFile, File outputFile) throws Exception {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(inputFile);
        JsonGenerator generator = factory.createGenerator(outputFile, com.fasterxml.jackson.core.JsonEncoding.UTF8);

        generator.writeStartObject();

        while (!parser.isClosed()) {
            JsonToken token = parser.nextToken();

            if (token == null) break;

            if (JsonToken.FIELD_NAME.equals(token)) {
                parser.nextToken(); // Move to value

                if (JsonToken.START_ARRAY.equals(parser.currentToken())) {
                    generator.writeFieldName("stepStatuses");
                    generator.writeStartArray(); // Start of array

                    // Process the array of objects
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        if (JsonToken.START_OBJECT.equals(parser.currentToken())) {
                            Map<String, String> command = new HashMap<>();
                            // Process command object
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                String key = parser.getCurrentName();
                                parser.nextToken(); // Move to the value
                                String value = parser.getValueAsString();
                                command.put(key, value);
                            }
                            ArrayList<String> leftVehicles = executeCommand(command);
                            writeLeftVehiclesToJsonFile(generator, leftVehicles);
                        }
                    }
                    generator.writeEndArray();
                }
            }
        }
        generator.writeEndObject();
        parser.close();
        generator.close();

        if (!isVisualisationEnabled)
            return;

        Thread.sleep(simSpeed * 6L);
        intersectionVisualisation.CloseWindow();
    }

    public void writeLeftVehiclesToJsonFile(JsonGenerator generator, ArrayList<String> leftVehicles) throws IOException {
        if (leftVehicles == null) {
            return;
        }
        generator.writeStartObject();
        generator.writeFieldName("leftVehicles");
        generator.writeStartArray();
        for (String vehicle : leftVehicles) {
            generator.writeString(vehicle);
        }
        generator.writeEndArray();
        generator.writeEndObject();

    }

    public record Vehicle(String vehicleId, String startRoad, String endRoad) {
    }

    public static class Intersection {
        private final int maxWaitTime = 4;
        public List<Vehicle> eastVehicles;
        public List<Vehicle> northVehicles;
        public List<Vehicle> westVehicles;
        public List<Vehicle> southVehicles;
        public Color eastTrafficLightColor;
        public Color northTrafficLightColor;
        public Color westTrafficLightColor;
        public Color southTrafficLightColor;
        public int eastLightLastChangedTime;
        public int northLightLastChangedTime;
        public int westLightLastChangedTime;
        public int southLightLastChangedTime;
        private int eastVehiclesCount = 0;
        private int northVehiclesCount = 0;
        private int westVehiclesCount = 0;
        private int southVehiclesCount = 0;

        public Intersection() {
            eastVehicles = new ArrayList<>();
            northVehicles = new ArrayList<>();
            westVehicles = new ArrayList<>();
            southVehicles = new ArrayList<>();

            eastTrafficLightColor = Color.GREEN;
            northTrafficLightColor = Color.RED;
            westTrafficLightColor = Color.GREEN;
            southTrafficLightColor = Color.RED;
        }

        public void addVehicleToRoad(Vehicle vehicle) {
            switch (vehicle.startRoad) {
                case "east":
                    eastVehicles.add(vehicle);
                    eastVehiclesCount++;
                    break;
                case "north":
                    northVehicles.add(vehicle);
                    northVehiclesCount++;
                    break;
                case "west":
                    westVehicles.add(vehicle);
                    westVehiclesCount++;
                    break;
                case "south":
                    southVehicles.add(vehicle);
                    southVehiclesCount++;
                    break;
            }
        }

        public Vehicle eastRoadRemoveFirstVehicle() {
            Vehicle leftVehicle = eastVehicles.removeFirst();
            eastVehiclesCount--;

            return leftVehicle;
        }

        public Vehicle northRoadRemoveFirstVehicle() {
            Vehicle leftVehicle = northVehicles.removeFirst();
            northVehiclesCount--;

            return leftVehicle;
        }

        public Vehicle westRoadRemoveFirstVehicle() {
            Vehicle leftVehicle = westVehicles.removeFirst();
            westVehiclesCount--;

            return leftVehicle;
        }

        public Vehicle southRoadRemoveFirstVehicle() {
            Vehicle leftVehicle = southVehicles.removeFirst();
            southVehiclesCount--;

            return leftVehicle;
        }

        public void setLights(Color eastNewColor, Color northNewColor, Color westNewColor, Color southNewColor) {
            if (eastTrafficLightColor.equals(eastNewColor)) {
                eastLightLastChangedTime++;
            } else {
                eastLightLastChangedTime = 0;
            }
            eastTrafficLightColor = eastNewColor;

            if (northTrafficLightColor.equals(northNewColor)) {
                northLightLastChangedTime++;
            } else {
                northLightLastChangedTime = 0;
            }
            northTrafficLightColor = northNewColor;

            if (westTrafficLightColor.equals(westNewColor)) {
                westLightLastChangedTime++;
            } else {
                westLightLastChangedTime = 0;
            }
            westTrafficLightColor = westNewColor;

            if (southTrafficLightColor.equals(southNewColor)) {
                southLightLastChangedTime++;
            } else {
                southLightLastChangedTime = 0;
            }
            southTrafficLightColor = southNewColor;
        }
    }
}
