package com.om.solvers.GeneticAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Crossover {
    private final double crossoverProb;
    private final Random rand;

    public Crossover(double crossoverProb, Random rand) {
        this.crossoverProb = crossoverProb;
        this.rand = rand;
    }

    public Individual orderedCrossover(Individual parent1, Individual parent2) {
        if (rand.nextDouble() >= crossoverProb) {
            return new Individual(parent1.getInstance(), parent1.getSequence());
        }

       List<Integer> parentSeq1 = parent1.getSequence();
       List<Integer> parentSeq2 = parent2.getSequence();
       int parentSeqSize1 = parentSeq1.size();
       int start = rand.nextInt(parentSeqSize1);
       int end = rand.nextInt(parentSeqSize1 - start) + start;

       if (start == end) {
           end = (start + 1) % parentSeqSize1;
       }

       List<Integer> childSeq = new ArrayList<>(Collections.nCopies(parentSeqSize1, -1));
       for (int i = start; i <= end; i++) {
           childSeq.set(i, parentSeq1.get(i));
       }

       int index = (end + 1) % parentSeqSize1;
       for (int gene : parentSeq2) {
           if (!childSeq.contains(gene)) {
               childSeq.set(index, gene);
               index = (index + 1) % parentSeqSize1;
           }
       }

       return new Individual(parent1.getInstance(), childSeq);
    }

}
