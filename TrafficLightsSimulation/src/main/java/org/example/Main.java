package org.example;

import java.io.File;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        if ((args.length < 2 || args.length > 3) || (!Objects.equals(args[0], "-v") && args.length == 3)) {
            System.out.println("Usage: java -jar TrafficLightsSimulation.jar [options] [input json file path] [output json file path]");
            System.out.println("Simulate traffic on an intersection.");
            System.out.println("Options:");
            System.out.println("    -v              display a window with a visual representation of the simulation");
            return;
        }

        boolean enableVisualisation = false;
        String inputJsonPath = "";
        String outputJsonPath = "";

        if (args.length == 2) {
            inputJsonPath = args[0];
            outputJsonPath = args[1];
        } else {
            enableVisualisation = true;
            inputJsonPath = args[1];
            outputJsonPath = args[2];
        }

        File inputFile = new File(inputJsonPath);
        File outputFile = new File(outputJsonPath);

        // Check if the file exists
        if (!inputFile.exists()) {
            System.out.println("Input file does not exist.");
            return;
        }

        // Check if the file is readable
        if (!inputFile.canRead()) {
            System.out.println("Input file is not readable.");
            return;
        }

        Simulation simulation = new Simulation(enableVisualisation);
        try {
            simulation.SimulationLoop(inputFile, outputFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}