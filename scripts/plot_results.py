# /// script
# requires-python = ">=3.11"
# dependencies = ["matplotlib>=3.9"]
# ///
"""Builds the README figures from results/summary.csv and results/routes/.

    uv run scripts/plot_results.py
"""

import csv
from collections import defaultdict
from pathlib import Path

import matplotlib.pyplot as plt

RESULTS = Path("results")
FIGURES = Path("docs")
ROUTE_INSTANCE = "A-n37-k6"

SURFACE = "#fcfcfb"
TEXT = "#0b0b0b"
TEXT_MUTED = "#52514e"
GRID = "#e4e3df"
SERIES = ["#2a78d6", "#eb6834", "#1baf7a", "#eda100", "#e87ba4", "#008300", "#6250d6", "#e34948"]
ALGORITHMS = {"Genetic Algorithm": SERIES[0], "Simulated Annealing": SERIES[1], "Greedy Search": SERIES[2]}

plt.rcParams.update({
    "figure.facecolor": SURFACE,
    "axes.facecolor": SURFACE,
    "axes.edgecolor": GRID,
    "axes.labelcolor": TEXT_MUTED,
    "axes.titlecolor": TEXT,
    "axes.titlesize": 11,
    "axes.titleweight": "bold",
    "axes.spines.top": False,
    "axes.spines.right": False,
    "axes.grid": True,
    "axes.axisbelow": True,
    "grid.color": GRID,
    "grid.linewidth": 0.8,
    "xtick.color": TEXT_MUTED,
    "ytick.color": TEXT_MUTED,
    "font.size": 9.5,
    "legend.frameon": False,
    "savefig.dpi": 200,
    "savefig.bbox": "tight",
})


def load_summary() -> dict[str, list[dict]]:
    by_tag = defaultdict(list)
    with open(RESULTS / "summary.csv") as f:
        for row in csv.DictReader(f):
            by_tag[row["tag"]].append(row)
    return by_tag


def param(row: dict, key: str) -> float:
    return dict(p.split("=") for p in row["params"].split())[key]


def plot_gap_by_instance(rows: list[dict]):
    instances = sorted({r["instance"] for r in rows}, key=lambda n: int(n.split("-n")[1].split("-")[0]))
    width = 0.26
    fig, ax = plt.subplots(figsize=(9, 3.8))
    for k, (algo, color) in enumerate(ALGORITHMS.items()):
        gaps = {r["instance"]: float(r["gap_avg_pct"]) for r in rows if r["algorithm"] == algo}
        xs = [i + (k - 1) * width for i in range(len(instances))]
        ys = [gaps[n] for n in instances]
        bars = ax.bar(xs, ys, width - 0.03, color=color, label=algo, edgecolor=SURFACE, linewidth=1)
        if algo == "Genetic Algorithm":
            ax.bar_label(bars, fmt="%.1f", padding=2, fontsize=8, color=TEXT)
    ax.set_xticks(range(len(instances)), instances)
    ax.set_ylabel("Gap to optimum, mean of 10 runs (%)")
    ax.set_title("Average gap to the best known solution (lower is better)", loc="left")
    ax.grid(axis="x", visible=False)
    ax.set_ylim(0, 70)
    ax.legend(loc="upper left", ncols=3)
    fig.savefig(FIGURES / "gap_by_instance.png")


def plot_ga_parameters(by_tag: dict[str, list[dict]]):
    studies = [
        ("budget", "pop", "Population size (pop × gen = 500k)", True),
        ("crossover", "px", "Crossover probability", False),
        ("mutation", "pm", "Mutation probability", False),
    ]
    fig, axes = plt.subplots(1, 3, figsize=(10, 3.2), sharey=True)
    for ax, (tag, key, xlabel, log) in zip(axes, studies):
        rows = sorted(by_tag[tag], key=lambda r: float(param(r, key)))
        xs = [float(param(r, key)) for r in rows]
        opt = float(rows[0]["optimum"])
        avg = [float(r["gap_avg_pct"]) for r in rows]
        best = [float(r["gap_best_pct"]) for r in rows]
        worst = [100 * (float(r["worst"]) - opt) / opt for r in rows]
        ax.fill_between(xs, best, worst, color=SERIES[0], alpha=0.15, linewidth=0)
        ax.plot(xs, avg, color=SERIES[0], linewidth=2, marker="o", markersize=5,
                markeredgecolor=SURFACE, markeredgewidth=1.5)
        if log:
            ax.set_xscale("log")
        ax.set_xlabel(xlabel)
        ax.set_title(tag.capitalize(), loc="left")
    axes[0].set_ylabel("Gap to optimum (%)")
    fig.suptitle("GA on A-n37-k6: mean gap (line) and best–worst range over 10 runs",
                 x=0.01, ha="left", fontsize=11, fontweight="bold", color=TEXT)
    fig.tight_layout()
    fig.savefig(FIGURES / "ga_parameters.png")


def load_coords(instance: str) -> tuple[dict[int, tuple[int, int]], int]:
    coords, depot, section = {}, 1, None
    for line in (Path("instances") / f"{instance}.vrp.txt").read_text().splitlines():
        line = line.strip()
        if line.endswith("_SECTION"):
            section = line
            continue
        parts = line.split()
        if section == "NODE_COORD_SECTION" and len(parts) == 3:
            coords[int(parts[0])] = (int(parts[1]), int(parts[2]))
        elif section == "DEPOT_SECTION" and line not in ("-1", "EOF") and parts:
            depot = int(parts[0])
            section = None
    return coords, depot


def plot_routes(rows: list[dict]):
    coords, depot = load_coords(ROUTE_INSTANCE)
    panels = [("gs", "Greedy Search"), ("ga", "Genetic Algorithm")]
    fig, axes = plt.subplots(1, 2, figsize=(10, 4.8))
    for ax, (key, algo) in zip(axes, panels):
        routes = [list(map(int, line.split()))
                  for line in (RESULTS / "routes" / f"main_{key}_{ROUTE_INSTANCE}.txt").read_text().splitlines() if line]
        row = next(r for r in rows if r["algorithm"] == algo and r["instance"] == ROUTE_INSTANCE)
        for i, route in enumerate(routes):
            path = [depot, *route, depot]
            ax.plot([coords[c][0] for c in path], [coords[c][1] for c in path],
                    color=SERIES[i % len(SERIES)], linewidth=2, marker="o", markersize=5,
                    markeredgecolor=SURFACE, markeredgewidth=1.2)
        ax.plot(*coords[depot], marker="s", markersize=10, color=TEXT, markeredgecolor=SURFACE, markeredgewidth=1.5)
        ax.set_title(f"{algo}: {float(row['best']):.0f} (gap {float(row['gap_best_pct']):.1f}%)", loc="left")
        ax.set_aspect("equal")
        ax.set_xticks([])
        ax.set_yticks([])
        ax.grid(False)
        for spine in ax.spines.values():
            spine.set_visible(False)
    fig.suptitle(f"Best routes on {ROUTE_INSTANCE} (optimum {row['optimum']}); ■ depot, one colour per vehicle",
                 x=0.01, ha="left", fontsize=11, fontweight="bold", color=TEXT)
    fig.tight_layout(rect=(0, 0, 1, 0.94))
    fig.savefig(FIGURES / f"routes_{ROUTE_INSTANCE}.png")


def main():
    FIGURES.mkdir(exist_ok=True)
    by_tag = load_summary()
    plot_gap_by_instance(by_tag["main"])
    plot_ga_parameters(by_tag)
    plot_routes(by_tag["main"])
    print(f"figures written to {FIGURES}/")


if __name__ == "__main__":
    main()
