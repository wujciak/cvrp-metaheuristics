package com.om.solvers.SimulatedAnnealing;

import com.om.solvers.RandomSearch.RandomSearch;
import com.om.utils.*;

import java.util.*;

public class SimulatedAnnealing {
    private final Instance instance;
    private final Random random;

    private static final double START_TEMPERATURE = 1000;
    private static final double END_TEMPERATURE = 0.01;
    private static final double COOLING_RATE = 0.995;
    private static final int ITERATIONS_PER_TEMP = 100;

    public SimulatedAnnealing(Instance instance, Random random) {
        this.instance = instance;
        this.random = random;
    }

    public List<Route> solve() {
        RandomSearch RS = new RandomSearch(instance, random);
        List<Route> routesSA = RS.solve();

        List<Route> currentSolution = deepCopyRoutes(routesSA);
        List<Route> bestSolution = deepCopyRoutes(currentSolution);
        double currentDistance = Evaluator.evaluate(currentSolution, instance.getDistanceMatrix());
        double bestDistance = currentDistance;

        double temperature = START_TEMPERATURE;

        while (temperature > END_TEMPERATURE) {
            for (int i = 0; i < ITERATIONS_PER_TEMP; i++) {
                List<Route> neighbor = generateNeighbor(deepCopyRoutes(currentSolution), instance, random);
                double neighborDistance = Evaluator.evaluate(neighbor, instance.getDistanceMatrix());

                double delta = neighborDistance - currentDistance;

                if (delta < 0 || Math.exp(-delta / temperature) > random.nextDouble()) {
                    currentSolution = neighbor;
                    currentDistance = neighborDistance;

                    if (currentDistance < bestDistance) {
                        bestSolution = deepCopyRoutes(currentSolution);
                        bestDistance = currentDistance;
                    }
                }
            }

            temperature *= COOLING_RATE;
        }

        return bestSolution;
    }

    private static List<Route> generateNeighbor(List<Route> routes, Instance instance, Random random) {
        if (routes.size() < 2) return routes;
        int index1 = random.nextInt(routes.size());
        int index2;
        do { index2 = random.nextInt(routes.size()); } while (index1 == index2);

        Route route1 = routes.get(index1);
        Route route2 = routes.get(index2);

        if (route1.getCities().isEmpty() || route2.getCities().isEmpty()) return routes;

        int i = random.nextInt(route1.getCities().size());
        int j = random.nextInt(route2.getCities().size());
        int city1 = route1.getCities().get(i);
        int city2 = route2.getCities().get(j);
        route1.getCities().set(i, city2);
        route2.getCities().set(j, city1);

        int demandRoute1 = calculateTotalDemand(route1, instance);
        int demandRoute2 = calculateTotalDemand(route2, instance);

        if (demandRoute1 > instance.getCapacity() || demandRoute2 > instance.getCapacity()) {
            route1.getCities().set(i, city1);
            route2.getCities().set(j, city2);
        }

        return routes;
    }

    private static int calculateTotalDemand(Route route, Instance instance) {
        int totalDemand = 0;
        for (int city : route.getCities()) {
            totalDemand += instance.getDemandMap().get(city);
        }
        return totalDemand;
    }

    private static List<Route> deepCopyRoutes(List<Route> original) {
        List<Route> copy = new ArrayList<>();
        for (Route route : original) {
            Route newRoute = new Route(route.getDepot());
            newRoute.addCities(new ArrayList<>(route.getCities()));
            copy.add(newRoute);
        }
        return copy;
    }
}
