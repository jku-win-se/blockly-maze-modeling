#!/usr/bin/env python3
"""
Plots visualization charts for the MOMoT First-Goal Benchmark across Blockly maze levels 1..10.
Generates Box plots, Mean +/- StdDev error bar plots, and combined overview charts
for both Time to First Goal (seconds) and Generation Number to First Goal.

Exports figures to PNG, PDF, and SVG formats.
"""

import sys
import os
import glob
import csv
import matplotlib.pyplot as plt
import numpy as np


def find_latest_csvs(analysis_dir, session=None):
    if session:
        raw_path = os.path.join(analysis_dir, f"first_goal_benchmark_raw_{session}.csv")
        summary_path = os.path.join(analysis_dir, f"first_goal_benchmark_summary_{session}.csv")
        if os.path.isfile(raw_path) and os.path.isfile(summary_path):
            return raw_path, summary_path

    raw_files = sorted(glob.glob(os.path.join(analysis_dir, "first_goal_benchmark_raw_*.csv")), key=os.path.getmtime, reverse=True)
    summary_files = sorted(glob.glob(os.path.join(analysis_dir, "first_goal_benchmark_summary_*.csv")), key=os.path.getmtime, reverse=True)

    if not raw_files or not summary_files:
        raise FileNotFoundError(f"No first_goal_benchmark_raw_*.csv or summary CSV found in {analysis_dir}")

    return raw_files[0], summary_files[0]


def load_raw_data(raw_csv_path):
    # returns dict mapping level -> list of dicts of solved runs
    level_runs = {lvl: [] for lvl in range(1, 11)}
    with open(raw_csv_path, newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            if row.get('solved', '').lower() in ('true', '1'):
                lvl = int(row['level'])
                time_sec = float(row['timeToFirstGoalSec']) if row.get('timeToFirstGoalSec') else float(row['timeToFirstGoalMs']) / 1000.0
                gen = float(row['generationOfFirstGoal'])
                level_runs[lvl].append({
                    'runIndex': int(row['runIndex']),
                    'timeSec': time_sec,
                    'gen': gen
                })
    return level_runs


def load_summary_data(summary_csv_path):
    by_level = {}
    with open(summary_csv_path, newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            lvl = int(row['level'])
            by_level[lvl] = {
                'successRate': float(row['successRate']),
                'meanTimeSec': float(row['meanTimeSec']),
                'stdDevTimeSec': float(row['stdDevTimeSec']),
                'medianTimeSec': float(row['medianTimeSec']),
                'meanGen': float(row['meanGen']),
                'stdDevGen': float(row['stdDevGen']),
                'medianGen': float(row['medianGen']),
            }
    return by_level


def configure_style():
    style_name = 'seaborn-v0_8-whitegrid' if 'seaborn-v0_8-whitegrid' in plt.style.available else 'default'
    plt.style.use(style_name)


def save_figure(fig, base_name, output_dir):
    for ext in ('png', 'pdf', 'svg'):
        out_path = os.path.join(output_dir, f"{base_name}.{ext}")
        fig.savefig(out_path, dpi=300 if ext == 'png' else None, bbox_inches='tight')
        print(f"Saved: {out_path}")


def plot_time_boxplot(level_runs, output_dir):
    fig, ax = plt.subplots(figsize=(10, 5), dpi=300)
    data = [ [r['timeSec'] for r in level_runs[lvl]] if level_runs[lvl] else [np.nan] for lvl in range(1, 11) ]

    bp = ax.boxplot(data, tick_labels=[str(lvl) for lvl in range(1, 11)], patch_artist=True,
                    boxprops=dict(facecolor='#4c72b0', color='#1d3557', alpha=0.8),
                    medianprops=dict(color='#e63946', linewidth=2),
                    whiskerprops=dict(color='#1d3557', linewidth=1.5),
                    capprops=dict(color='#1d3557', linewidth=1.5),
                    flierprops=dict(marker='o', color='#e63946', alpha=0.6))

    ax.set_title("Time to First Goal Solution Across Blockly Maze Levels", fontsize=12, fontweight='bold', pad=15)
    ax.set_xlabel("Maze Level", fontsize=11, labelpad=8)
    ax.set_ylabel("Time to First Goal (seconds)", fontsize=11, labelpad=8)
    ax.grid(True, axis='y', linestyle=':', alpha=0.6)

    save_figure(fig, "first_goal_time_sec_boxplot", output_dir)
    plt.close(fig)


def plot_time_errorbars(summary_data, output_dir):
    fig, ax = plt.subplots(figsize=(10, 5), dpi=300)
    levels = list(range(1, 11))
    means = [summary_data.get(lvl, {}).get('meanTimeSec', 0.0) for lvl in levels]
    stds = [summary_data.get(lvl, {}).get('stdDevTimeSec', 0.0) for lvl in levels]

    ax.errorbar(levels, means, yerr=stds, fmt='-o', color='#1f77b4', ecolor='#d62728',
                elinewidth=2, capsize=5, capthick=1.5, label='Mean ± Std Dev')

    ax.set_title("Time to First Goal Solution (Mean ± Std Dev)", fontsize=12, fontweight='bold', pad=15)
    ax.set_xlabel("Maze Level", fontsize=11, labelpad=8)
    ax.set_ylabel("Time to First Goal (seconds)", fontsize=11, labelpad=8)
    ax.set_xticks(levels)
    ax.grid(True, linestyle=':', alpha=0.6)
    ax.legend(frameon=True, facecolor='white', framealpha=0.95, loc='upper left')

    save_figure(fig, "first_goal_time_sec_errorbars", output_dir)
    plt.close(fig)


def plot_gen_boxplot(level_runs, output_dir):
    fig, ax = plt.subplots(figsize=(10, 5), dpi=300)
    data = [ [r['gen'] for r in level_runs[lvl]] if level_runs[lvl] else [np.nan] for lvl in range(1, 11) ]

    ax.boxplot(data, tick_labels=[str(lvl) for lvl in range(1, 11)], patch_artist=True,
               boxprops=dict(facecolor='#55a868', color='#2a9d8f', alpha=0.8),
               medianprops=dict(color='#e76f51', linewidth=2),
               whiskerprops=dict(color='#2a9d8f', linewidth=1.5),
               capprops=dict(color='#2a9d8f', linewidth=1.5),
               flierprops=dict(marker='o', color='#e76f51', alpha=0.6))

    ax.set_title("Generation of First Goal Solution Across Blockly Maze Levels", fontsize=12, fontweight='bold', pad=15)
    ax.set_xlabel("Maze Level", fontsize=11, labelpad=8)
    ax.set_ylabel("Generation Number", fontsize=11, labelpad=8)
    ax.grid(True, axis='y', linestyle=':', alpha=0.6)

    save_figure(fig, "first_goal_generation_boxplot", output_dir)
    plt.close(fig)


def plot_gen_errorbars(summary_data, output_dir):
    fig, ax = plt.subplots(figsize=(10, 5), dpi=300)
    levels = list(range(1, 11))
    means = [summary_data.get(lvl, {}).get('meanGen', 0.0) for lvl in levels]
    stds = [summary_data.get(lvl, {}).get('stdDevGen', 0.0) for lvl in levels]

    ax.errorbar(levels, means, yerr=stds, fmt='-s', color='#2ca02c', ecolor='#ff7f0e',
                elinewidth=2, capsize=5, capthick=1.5, label='Mean ± Std Dev')

    ax.set_title("Generation of First Goal Solution (Mean ± Std Dev)", fontsize=12, fontweight='bold', pad=15)
    ax.set_xlabel("Maze Level", fontsize=11, labelpad=8)
    ax.set_ylabel("Generation Number", fontsize=11, labelpad=8)
    ax.set_xticks(levels)
    ax.grid(True, linestyle=':', alpha=0.6)
    ax.legend(frameon=True, facecolor='white', framealpha=0.95, loc='upper left')

    save_figure(fig, "first_goal_generation_errorbars", output_dir)
    plt.close(fig)


def plot_combined_overview(summary_data, output_dir):
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5), dpi=300)
    levels = list(range(1, 11))

    time_means = [summary_data.get(lvl, {}).get('meanTimeSec', 0.0) for lvl in levels]
    time_stds = [summary_data.get(lvl, {}).get('stdDevTimeSec', 0.0) for lvl in levels]

    ax1.errorbar(levels, time_means, yerr=time_stds, fmt='-o', color='#1f77b4', ecolor='#d62728',
                 elinewidth=1.8, capsize=4, capthick=1.2)
    ax1.set_title("(A) Time to First Goal (sec)", fontsize=11, fontweight='bold')
    ax1.set_xlabel("Maze Level", fontsize=10)
    ax1.set_ylabel("Seconds", fontsize=10)
    ax1.set_xticks(levels)
    ax1.grid(True, linestyle=':', alpha=0.6)

    gen_means = [summary_data.get(lvl, {}).get('meanGen', 0.0) for lvl in levels]
    gen_stds = [summary_data.get(lvl, {}).get('stdDevGen', 0.0) for lvl in levels]

    ax2.errorbar(levels, gen_means, yerr=gen_stds, fmt='-s', color='#2ca02c', ecolor='#ff7f0e',
                 elinewidth=1.8, capsize=4, capthick=1.2)
    ax2.set_title("(B) Generation of First Goal", fontsize=11, fontweight='bold')
    ax2.set_xlabel("Maze Level", fontsize=10)
    ax2.set_ylabel("Generation Number", fontsize=10)
    ax2.set_xticks(levels)
    ax2.grid(True, linestyle=':', alpha=0.6)

    fig.suptitle("MOMoT First-Goal Benchmark Search Effort Overview Across Levels 1–10", fontsize=13, fontweight='bold', y=0.98)
    plt.tight_layout(rect=[0, 0, 1, 0.95])

    save_figure(fig, "first_goal_combined_overview", output_dir)
    plt.close(fig)


def main():
    session = sys.argv[1] if len(sys.argv) > 1 else None
    analysis_dir = os.path.dirname(os.path.abspath(__file__))

    raw_csv, summary_csv = find_latest_csvs(analysis_dir, session)
    print(f"Using Raw CSV:     {raw_csv}")
    print(f"Using Summary CSV: {summary_csv}")

    level_runs = load_raw_data(raw_csv)
    summary_data = load_summary_data(summary_csv)

    configure_style()

    plot_time_boxplot(level_runs, analysis_dir)
    plot_time_errorbars(summary_data, analysis_dir)
    plot_gen_boxplot(level_runs, analysis_dir)
    plot_gen_errorbars(summary_data, analysis_dir)
    plot_combined_overview(summary_data, analysis_dir)


if __name__ == "__main__":
    main()
