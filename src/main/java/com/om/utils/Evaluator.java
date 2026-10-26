package com.om.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Evaluator {

    /**
     * Method for checking that every customer is visited exactly once and no route exceeds capacity
     */
    public static boolean isFeasible(List<Route> routes, Instance instance) {
        Set<Integer> visited = new HashSet<>();
        for (Route route : routes) {
            int load = 0;
            for (int city : route.getCities()) {
                if (city == instance.getDepotId() || !visited.add(city)) {
                    return false;
                }
                load += instance.getDemandMap().get(city);
            }
            if (load > instance.getCapacity()) {
                return false;
            }
        }
        return visited.size() == instance.getDimension() - 1;
    }

    public static double evaluate(List<Route> routes, double[][] distanceMatrix) {
        double totalDistance = 0.0;

        for (Route route : routes) {
            totalDistance += calculateRouteDistance(route, distanceMatrix);
        }

        return totalDistance;
    }

    private static double calculateRouteDistance(Route route, double[][] distanceMatrix) {
        List<Integer> cities = route.getCities();
        int depot = route.getDepot();
        double distance = 0.0;

        if (!cities.isEmpty()) {
            distance += distanceMatrix[depot][cities.getFirst()];
        }

        for (int i = 0; i < cities.size() - 1; i++) {
            distance += distanceMatrix[cities.get(i)][cities.get(i + 1)];
        }

        if (!cities.isEmpty()) {
            distance += distanceMatrix[cities.getLast()][depot];
        }

        return distance;
    }
}
