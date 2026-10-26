package com.om;

import com.om.solvers.GeneticAlgorithm.GeneticAlgorithm;
import com.om.solvers.GreedySearch.GreedySearch;
import com.om.solvers.RandomSearch.RandomSearch;
import com.om.solvers.SimulatedAnnealing.SimulatedAnnealing;
import com.om.utils.Evaluator;
import com.om.utils.Instance;
import com.om.utils.Parser;
import com.om.utils.ResultWriter;
import com.om.utils.Route;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) throws IOException {
        Options opts;
        try {
            opts = Options.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            Options.printUsage();
            System.exit(2);
            return;
        }
        ResultWriter writer = new ResultWriter(opts.outDir, opts.tag);
        Evaluator evaluator = new Evaluator();

        for (Path path : opts.instances) {
            Instance instance = Parser.parse(path.toString());

            for (String algorithm : opts.algorithms) {
                switch (algorithm) {
                    case "rs" -> run("rs", "Random Search", "", opts.rsRuns, opts.seed,
                            random -> new RandomSearch(instance, random).solve(), instance, writer);
                    case "gs" -> run("gs", "Greedy Search", "", 1, opts.seed,
                            random -> GreedySearch.solve(instance), instance, writer);
                    case "sa" -> run("sa", "Simulated Annealing", "", opts.runs, opts.seed,
                            random -> new SimulatedAnnealing(instance, random).solve(), instance, writer);
                    case "ga" -> run("ga", "Genetic Algorithm",
                            String.format(Locale.ROOT, "pop=%d gen=%d px=%.2f pm=%.2f tour=%d",
                                    opts.popSize, opts.generations, opts.crossoverProb, opts.mutationProb, opts.tournamentSize),
                            opts.runs, opts.seed,
                            random -> new GeneticAlgorithm(instance, evaluator, opts.popSize, opts.crossoverProb,
                                    opts.mutationProb, opts.tournamentSize, opts.generations, random).solve(),
                            instance, writer);
                    default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
                }
            }
        }
    }

    /**
     * Method for running a solver several times with consecutive seeds and saving the summary
     */
    private static void run(String key, String algorithm, String params, int runs, long seed,
                            Function<Random, List<Route>> solver, Instance instance, ResultWriter writer) throws IOException {
        List<Double> scores = new ArrayList<>();
        List<Route> bestSolution = null;
        double bestScore = Double.MAX_VALUE;
        long start = System.nanoTime();

        for (int i = 0; i < runs; i++) {
            List<Route> solution = solver.apply(new Random(seed + i));
            if (!Evaluator.isFeasible(solution, instance)) {
                throw new IllegalStateException(algorithm + " returned an infeasible solution for " + instance.getName());
            }
            double score = Evaluator.evaluate(solution, instance.getDistanceMatrix());
            scores.add(score);
            if (score < bestScore) {
                bestScore = score;
                bestSolution = solution;
            }
        }

        double avgMillis = (System.nanoTime() - start) / 1e6 / runs;
        writer.write(key, algorithm, params, instance, scores, bestSolution, avgMillis);
    }
}
