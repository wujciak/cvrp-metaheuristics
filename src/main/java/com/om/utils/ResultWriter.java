package com.om.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Appends one summary row per (algorithm, instance) to summary.csv and stores the best routes.
 */
public class ResultWriter {
    private static final String HEADER =
            "tag,algorithm,params,instance,optimum,runs,best,avg,worst,std,gap_best_pct,gap_avg_pct,avg_ms\n";

    private final Path outDir;
    private final String tag;

    public ResultWriter(Path outDir, String tag) throws IOException {
        this.outDir = outDir;
        this.tag = tag;
        Files.createDirectories(outDir.resolve("routes"));
    }

    public void write(String key, String algorithm, String params, Instance instance, List<Double> scores,
                      List<Route> bestSolution, double avgMillis) throws IOException {
        double best = scores.stream().min(Double::compareTo).orElse(Double.NaN);
        double worst = scores.stream().max(Double::compareTo).orElse(Double.NaN);
        double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
        double std = calculateStandardDeviation(scores, avg);
        int optimum = instance.getOptimum();

        Path summary = outDir.resolve("summary.csv");
        if (!Files.exists(summary)) {
            Files.writeString(summary, HEADER);
        }
        String row = String.format(Locale.ROOT, "%s,%s,%s,%s,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.1f%n",
                tag, algorithm, params, instance.getName(), optimum, scores.size(), best, avg, worst, std,
                gap(best, optimum), gap(avg, optimum), avgMillis);
        Files.writeString(summary, row, StandardOpenOption.APPEND);

        String routes = bestSolution.stream()
                .map(r -> r.getCities().stream().map(String::valueOf).collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n", "", "\n"));
        Files.writeString(outDir.resolve("routes").resolve(tag + "_" + key + "_" + instance.getName() + ".txt"), routes);

        System.out.printf(Locale.ROOT, "%-20s %-10s best %8.1f  avg %8.1f  gap %5.1f%%  %s%n",
                algorithm, instance.getName(), best, avg, gap(best, optimum), params);
    }

    private static double gap(double value, int optimum) {
        return optimum > 0 ? 100.0 * (value - optimum) / optimum : Double.NaN;
    }

    /**
     * Method for calculating standard deviation
     */
    private static double calculateStandardDeviation(List<Double> scores, double mean) {
        double sumSquaredDiffs = scores.stream().mapToDouble(score -> Math.pow(score - mean, 2)).sum();
        return Math.sqrt(sumSquaredDiffs / scores.size());
    }
}
