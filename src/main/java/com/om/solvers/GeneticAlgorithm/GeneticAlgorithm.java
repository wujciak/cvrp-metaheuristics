package com.om.solvers.GeneticAlgorithm;

import com.om.utils.Evaluator;
import com.om.utils.Instance;
import com.om.utils.Route;

import java.util.*;

public class GeneticAlgorithm {
    public final Instance instance;
    public final Evaluator evaluator;
    private final Selection selection;
    private final Crossover crossover;
    private final Mutation mutation;
    private final int popSize;
    private final int maxGenerations;
    private final Random random;

    public GeneticAlgorithm(Instance instance, Evaluator evaluator, int popSize, double crossoverP, double mutationP , int tournamentSize, int maxGenerations, Random random) {
        this.instance = instance;
        this.evaluator = evaluator;
        this.selection = new Selection(tournamentSize, random);
        this.crossover = new Crossover(crossoverP, random);
        this.mutation = new Mutation(mutationP, random);
        this.popSize = popSize;
        this.maxGenerations = maxGenerations;
        this.random = random;
    }

    public List<Route> solve() {
        List<Individual> population = initializePopulation();
        Individual bestIndividual = findBestIndividual(population);

        for (int gen=0; gen < maxGenerations; gen++) {
            population = generateNextGeneration(population);
            Individual currentBestIndividual = findBestIndividual(population);
            if (currentBestIndividual.getFitness() < bestIndividual.getFitness()) {
                bestIndividual = currentBestIndividual;
            }
        }

        return decodeSolution(bestIndividual, instance);
    }

    /**
     * Method for initializing population
     * @return list of individuals
     */
    private List<Individual> initializePopulation() {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < popSize; i++) {
            Individual individual = new Individual(instance, generateRandomRoute());
            individual.eval(evaluator);
            population.add(individual);
        }
        return population;
    }

    /**
     * Method for generating random route
     * @return list of cities ids in a route
     */
    private List<Integer> generateRandomRoute() {
        List<Integer> route = new ArrayList<>();
        for (int i = 2; i <= instance.getDimension(); i++) {
            route.add(i);
        }
        Collections.shuffle(route, random);
        return route;
    }

    /**
     * Method for finding individual with best fitness
     * @param population list of individuals
     * @return individual object representation
     */
    private Individual findBestIndividual(List<Individual> population) {
        return Collections.min(population, Comparator.comparingDouble(Individual::getFitness));
    }

    /**
     * Method for generating next generation
     * @param population list of individuals
     * @return List<Individual> new population
     */
    private List<Individual> generateNextGeneration(List<Individual> population) {
        List<Individual> newPopulation = new ArrayList<>();
        Individual bestIndividual = findBestIndividual(population);

        // Elitism
        Individual eliteCopy = new Individual(bestIndividual.getInstance(), bestIndividual.getSequence());
        eliteCopy.eval(evaluator);
        newPopulation.add(eliteCopy);

        while (newPopulation.size() < popSize) {
            Individual parent1 = selection.tournamentSelection(population);
            Individual parent2 = selection.tournamentSelection(population);
            Individual child = crossover.orderedCrossover(parent1, parent2);
            mutation.mutate(child.getSequence());
            child.eval(evaluator);
            newPopulation.add(child);
        }
        return newPopulation;
    }

    /**
     * Method for decoding individual to the list of routes
     * @param bestIndividual best found individual
     * @return List<Route> final solution
     */
    private List<Route> decodeSolution(Individual bestIndividual, Instance instance) {
        List<Route> routes = new ArrayList<>();
        int capacity = instance.getCapacity();
        int depot = instance.getDepotId();
        Map<Integer, Integer> demandMap = instance.getDemandMap();

        Route currentRoute = new Route(depot);
        int currentLoad = 0;

        for (int gene : bestIndividual.getSequence()) {
            int demand = demandMap.get(gene);
            if (currentLoad + demand > capacity) {
                routes.add(currentRoute);
                currentRoute = new Route(depot);
                currentLoad = 0;
            }
            currentRoute.addCity(gene);
            currentLoad += demand;
        }

        if (!currentRoute.getCities().isEmpty()) {
            routes.add(currentRoute);
        }

        return routes;
    }

}
