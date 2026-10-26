package com.om;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Command-line options. Every option has a default, so running without arguments
 * solves all instances in instances/ with every algorithm.
 */
public class Options {
    private static final Path INSTANCES_DIR = Path.of("instances");
    private static final List<String> ALGORITHMS = List.of("rs", "gs", "sa", "ga");

    List<Path> instances;
    List<String> algorithms = ALGORITHMS;
    int runs = 10;
    int rsRuns = 1000;
    int popSize = 1000;
    int generations = 500;
    double crossoverProb = 0.7;
    double mutationProb = 0.1;
    int tournamentSize = 5;
    long seed = 42;
    Path outDir = Path.of("results");
    String tag = "main";

    public static Options parse(String[] args) throws IOException {
        Options opts = new Options();
        String instanceArg = null;

        for (int i = 0; i < args.length; i++) {
            String flag = args[i];
            if (flag.equals("-h") || flag.equals("--help")) {
                printUsage();
                System.exit(0);
            }
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Missing value for " + flag);
            }
            String value = args[++i];
            switch (flag) {
                case "--instance" -> instanceArg = value;
                case "--algo" -> opts.algorithms = value.equals("all") ? ALGORITHMS : Arrays.asList(value.split(","));
                case "--runs" -> opts.runs = Integer.parseInt(value);
                case "--rs-runs" -> opts.rsRuns = Integer.parseInt(value);
                case "--pop" -> opts.popSize = Integer.parseInt(value);
                case "--gen" -> opts.generations = Integer.parseInt(value);
                case "--px" -> opts.crossoverProb = Double.parseDouble(value);
                case "--pm" -> opts.mutationProb = Double.parseDouble(value);
                case "--tour" -> opts.tournamentSize = Integer.parseInt(value);
                case "--seed" -> opts.seed = Long.parseLong(value);
                case "--out" -> opts.outDir = Path.of(value);
                case "--tag" -> opts.tag = value;
                default -> throw new IllegalArgumentException("Unknown option: " + flag);
            }
        }

        opts.instances = instanceArg == null ? allInstances() : resolveInstances(instanceArg);
        return opts;
    }

    private static List<Path> allInstances() throws IOException {
        try (Stream<Path> files = Files.list(INSTANCES_DIR)) {
            return files.filter(p -> p.toString().endsWith(".vrp.txt")).sorted().toList();
        }
    }

    /**
     * Accepts comma-separated paths or bare instance names such as A-n32-k5
     */
    private static List<Path> resolveInstances(String arg) {
        List<Path> paths = new ArrayList<>();
        for (String item : arg.split(",")) {
            Path path = Path.of(item);
            if (!Files.exists(path)) {
                path = INSTANCES_DIR.resolve(item + ".vrp.txt");
            }
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Instance not found: " + item);
            }
            paths.add(path);
        }
        return paths;
    }

    static void printUsage() {
        System.out.println("""
                Usage: java -jar target/cvrp-optimization.jar [options]

                  --instance NAMES   comma-separated names (A-n32-k5) or paths   [all in instances/]
                  --algo LIST        comma-separated: rs, gs, sa, ga or all      [all]
                  --runs N           independent runs for SA and GA              [10]
                  --rs-runs N        samples for Random Search                   [1000]
                  --pop N            GA population size                          [1000]
                  --gen N            GA generations                              [500]
                  --px P             GA crossover probability                    [0.7]
                  --pm P             GA mutation probability                     [0.1]
                  --tour N           GA tournament size                          [5]
                  --seed N           base seed, run i uses seed + i              [42]
                  --out DIR          output directory                            [results]
                  --tag NAME         experiment label written to summary.csv     [main]
                """);
    }
}
