package com.om.solvers.GeneticAlgorithm;

import java.util.*;

public class Selection {
    public final int tournamentSize;
    private final Random rand;

    public Selection(int tournamentSize, Random rand) {
        this.tournamentSize = tournamentSize;
        this.rand = rand;
    }

    public Individual tournamentSelection(List<Individual> population) {
        List<Individual> competitors = new ArrayList<>();

        while (competitors.size() < tournamentSize) {
            Individual candidate = population.get(rand.nextInt(population.size()));
            competitors.add(candidate);
        }
        return Collections.min(competitors, Comparator.comparing(Individual::getFitness));
    }
}
