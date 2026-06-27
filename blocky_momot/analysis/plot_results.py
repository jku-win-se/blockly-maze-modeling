import csv
import glob
import os

import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

CANONICAL_SESSION = "paper_simple_boosted_20260627"
CANONICAL_CSV = f"min_solution_length_simple_{CANONICAL_SESSION}.csv"


def resolve_csv(analysis_dir):
    canonical = os.path.join(analysis_dir, CANONICAL_CSV)
    if os.path.isfile(canonical):
        return canonical
    pattern = os.path.join(analysis_dir, "min_solution_length_simple_*.csv")
    files = sorted(glob.glob(pattern), key=os.path.getmtime, reverse=True)
    if not files:
        raise FileNotFoundError(f"No min_solution_length_simple_*.csv found in {analysis_dir}")
    return files[0]


def load_results(csv_path):
    optimal = []
    min_length = []
    momot_blocks = []
    with open(csv_path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        by_level = {}
        for row in reader:
            if row.get("status") != "SOLVED":
                continue
            level = int(row["level"])
            by_level[level] = row
        for level in range(1, 11):
            row = by_level.get(level)
            if row is None:
                optimal.append(None)
                min_length.append(None)
                momot_blocks.append(None)
                continue
            optimal.append(int(row["optimalBlockCount"]))
            min_length.append(int(row["minSolutionLength"]))
            raw_blocks = (row.get("momotBlockCount") or "").strip()
            momot_blocks.append(int(raw_blocks) if raw_blocks else None)
    return optimal, min_length, momot_blocks


def main():
    analysis_dir = os.path.dirname(os.path.abspath(__file__))
    csv_path = resolve_csv(analysis_dir)
    optimal, min_length, momot_blocks = load_results(csv_path)

    levels = list(range(1, 11))
    ymax = max(
        v for v in optimal + min_length + [b for b in momot_blocks if b is not None]
    )
    ymax = int((ymax + 4) // 5 * 5 + 5)

    plt.style.use(
        "seaborn-v0_8-whitegrid"
        if "seaborn-v0_8-whitegrid" in plt.style.available
        else "default"
    )
    fig, ax = plt.subplots(figsize=(10, 5), dpi=300)

    x = range(len(levels))
    width = 0.25
    colors = {
        "optimal": "#1f77b4",
        "transformations": "#ff7f0e",
        "blocks": "#2ca02c",
    }

    ax.bar([i - width for i in x], optimal, width, color=colors["optimal"], label="Optimal block count")
    ax.bar(x, min_length, width, color=colors["transformations"], label="Min. MOMoT solutionLength")
    ax.bar(
        [i + width for i in x],
        momot_blocks,
        width,
        color=colors["blocks"],
        label="MOMoT program block count",
    )

    ax.set_title(
        "Optimal vs. MOMoT Search Length vs. MOMoT Program Size per Level",
        fontsize=12,
        fontweight="bold",
        pad=15,
    )
    ax.set_xlabel("Maze Level", fontsize=11, labelpad=8)
    ax.set_ylabel("Count (blocks or transformations)", fontsize=11, labelpad=8)
    ax.set_xticks(list(x))
    ax.set_xticklabels(levels)
    ax.set_ylim(0, ymax)
    ax.legend(frameon=True, facecolor="white", framealpha=0.95, fontsize=9, loc="upper left")
    ax.grid(True, axis="y", linestyle=":", alpha=0.6)

    plt.tight_layout()
    for ext in ("png", "pdf", "svg"):
        out = os.path.join(analysis_dir, f"min_solution_length_chart.{ext}")
        plt.savefig(out, dpi=300 if ext == "png" else None)
        print(f"Saved: {out}")
    print(f"CSV source: {csv_path}")


if __name__ == "__main__":
    main()
