#!/usr/bin/env bash
# Reproduces every number in results/summary.csv. Run from the repo root after `mvn package`.
set -euo pipefail

JAR=target/cvrp-optimization.jar
INSTANCE=A-n37-k6   # instance used for GA parameter studies

rm -rf results
run() { java -jar "$JAR" "$@"; }

echo "== Main comparison: all algorithms on all instances"
run --tag main

echo "== GA budget split: pop x gen = 500k evaluations"
for split in 10:50000 50:10000 100:5000 500:1000 1000:500 5000:100 10000:50 50000:10; do
    run --tag budget --instance "$INSTANCE" --algo ga --pop "${split%%:*}" --gen "${split##*:}"
done

echo "== GA crossover probability"
for px in 0.50 0.60 0.70 0.80 0.90 1.00; do
    run --tag crossover --instance "$INSTANCE" --algo ga --px "$px"
done

echo "== GA mutation probability"
for pm in 0.01 0.05 0.10 0.20 0.30 0.40; do
    run --tag mutation --instance "$INSTANCE" --algo ga --pm "$pm"
done
