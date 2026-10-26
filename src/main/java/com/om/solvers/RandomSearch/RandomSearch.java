package com.om.solvers.RandomSearch;

import com.om.utils.Instance;
import com.om.utils.Route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomSearch {
    private final Instance instance;
    private final Random random;

    public RandomSearch(Instance instance, Random random) {
        this.instance = instance;
        this.random = random;
    }

    public List<Route> solve() {
        List<Integer> allCities = new ArrayList<>(instance.getDemandMap().keySet());
        int depot = instance.getDepotId();
        allCities.remove(Integer.valueOf(depot));

        Collections.shuffle(allCities, random);

        List<Route> routes = new ArrayList<>();
        int vehicleCapacity = instance.getCapacity();

        while (!allCities.isEmpty()) {
            Route route = new Route(depot);
            int remainingCapacity = vehicleCapacity;

            List<Integer> toRemove = new ArrayList<>();
            for (int city : allCities) {
                int demand = instance.getDemandMap().get(city);
                if (remainingCapacity >= demand) {
                    route.addCity(city);
                    remainingCapacity -= demand;
                    toRemove.add(city);
                }
            }
            allCities.removeAll(toRemove);
            routes.add(route);
        }

        return routes;
    }

}
