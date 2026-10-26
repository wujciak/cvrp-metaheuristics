package com.om.utils;

import java.util.Map;

/**
 * Representation of CVRP problem parameters.
 */
public class Instance {
    private final String name;
    private final int optimum; // best known solution from the instance header, 0 if absent
    private final int dimension;
    private final int capacity;
    private final Map<Integer, int[]> coordMap;
    private final Map<Integer, Integer> demandMap;
    private final int depotId;
    private final double[][] distanceMatrix;

    public Instance(String name, int optimum, int dimension, int capacity, Map<Integer, int[]> coordMap, Map<Integer, Integer> demandMap, int depotId) {
        this.name = name;
        this.optimum = optimum;
        this.dimension = dimension;
        this.capacity = capacity;
        this.coordMap = coordMap;
        this.demandMap = demandMap;
        this.depotId = depotId;
        this.distanceMatrix = calculateDistanceMatrix();
    }

    public double[][] calculateDistanceMatrix() {
        double[][] distanceMatrix = new double[dimension + 1][dimension + 1]; // Indexes in file start from 1

        for (int i = 1; i <= dimension; i++) {
            for (int j = 1; j <= dimension; j++) {
                double distance = calcDistanceTwoPoints(coordMap.get(i), coordMap.get(j));
                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance; // The matrix should be symmetric
            }
        }

        return distanceMatrix;
    }

    public double calcDistanceTwoPoints(int[] point1, int[] point2) {
        return Math.sqrt(Math.pow(point2[0] - point1[0], 2) + Math.pow(point2[1] - point1[1], 2));
    }

    public String getName() {
        return name;
    }

    public int getOptimum() {
        return optimum;
    }

    public int getDimension() {
        return dimension;
    }

    public int getCapacity() {
        return capacity;
    }

    public Map<Integer, int[]> getCoordMap() {
        return coordMap;
    }

    public Map<Integer, Integer> getDemandMap() {
        return demandMap;
    }

    public int getDepotId() {
        return depotId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Instance [dimension=").append(dimension)
                .append(", capacity=").append(capacity)
                .append(", depotId=").append(depotId)
                .append(", coordMap={");

        for (Map.Entry<Integer, int[]> entry : coordMap.entrySet()) {
            sb.append(entry.getKey()).append("=(")
                    .append(entry.getValue()[0]).append(", ")
                    .append(entry.getValue()[1]).append("), ");
        }
        sb.append("}, demandMap={");

        for (Map.Entry<Integer, Integer> entry : demandMap.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
        }
        sb.append("}]");

        return sb.toString();
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }
}
