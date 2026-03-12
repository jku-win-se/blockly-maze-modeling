# Blocky MOMoT

MOMoT (Marrying Search-based Optimization and Model Transformation Technology) project for the Blocky maze game. It accepts a Blocky XMI model and searches for a **solution program** that reaches the goal with the **least number of changes** (transformation steps) to the model.

## Goal

- **Input**: An XMI file containing a Blocky **Game** (at least one **Level** with a GridMap, START and GOAL cells; solution may be empty or existing).
- **Output**: A transformation sequence that produces a program (sequence of MoveForward/Turn blocks) that reaches the GOAL when simulated.
- **Objective**: Minimize the number of Henshin rule applications (“least number of changes”).

## Docs for agents

When generating or modifying Henshin rules, MOMoT config, or WebView sync (e.g. map editing via UI), use the agent-oriented docs in this repo:

- **Henshin** (`.henshin_text` rules): [docs/henshin/README.md](../docs/henshin/README.md)
- **MOMoT** (`.momot` config): [docs/momot/README.md](../docs/momot/README.md)
- **WebView↔Java sync** (JSBridge, extending the game UI): [docs/webview-sync/README.md](../docs/webview-sync/README.md)

Transformation rules live in **blocky_model** (not in blocky_momot): see `blocky_model/transformations/` and the Project layout below.

## Prerequisites

- **Eclipse IDE** with:
  - EMF (Ecore)
  - **Henshin** (model transformation)
  - **MOMoT** (search-based optimization)
- **blocky_model** project built (EMF code generation) in the same workspace.
- Java 17.

Install MOMoT and Henshin from their Eclipse update sites (see [docs/momot/references.md](../docs/momot/references.md)).

## Project layout

| Path | Description |
|------|-------------|
| `blocky.momot` | MOMoT configuration (model path, transformations, fitness, algorithms, experiment). |
| `model/input/` | Directory for the input XMI; default file name used in config: **game.xmi**. |
| `src/blocky_momot/BlockySimulator.java` | Headless simulator used by fitness to check if the program reaches the goal. |
| `output/` | Results (objectives, solutions, models) after a run. |

The Henshin rules are in the **blocky_model** project:

| Path (in blocky_model) | Description |
|------------------------|-------------|
| `transformations/add_block_to_empty_slot.henshin_text` | Henshin rules in textual form (add/delete blocks to solution). |
| `transformations/add_block_to_empty_slot_henshin_text.henshin` | Compiled Henshin module (XMI); **must be generated** from the `.henshin_text` file (see below). **blocky.momot** points to this file. |

## Setup

1. **Import the project**  
   In Eclipse: File → Import → Existing Projects → select the `blocky_momot` folder. Ensure **blocky_model** is in the same workspace and built.

2. **Set the input XMI**  
   - Copy a Blocky Game XMI into `model/input/` (e.g. from **blocky_game/save.xmi** or **load.xmi**) and name it **game.xmi**, or  
   - Use your own XMI and set the path in `blocky.momot` under `search.model.file` (e.g. `file = "model/input/yourfile.xmi"`).

3. **Generate the .henshin file (XMI/XML)**  
   The rules are written in **add_block_to_empty_slot.henshin_text** in the **blocky_model** project. The **add_block_to_empty_slot_henshin_text.henshin** file is the same transformation in **XMI (XML)** format; MOMoT uses the `.henshin` file. To generate it in Eclipse:  
   - Open **blocky_model/transformations/add_block_to_empty_slot.henshin_text** in the **Henshin Text** editor.  
   - Right-click → **Transform to Henshin**.  
   This creates/updates **blocky_model/transformations/add_block_to_empty_slot_henshin_text.henshin**. You can commit that file so the repo contains the XMI. See blocky_model **transformations/README.md** for details.

   **blocky.momot** references this file via `../blocky_model/transformations/add_block_to_empty_slot_henshin_text.henshin` (relative to the blocky_momot project).

## How to run the search

1. Open **blocky.momot** in the MOMoT editor.
2. Run the MOMoT configuration (e.g. Run button or context menu “Run as MOMoT” / equivalent from your MOMoT installation).
3. The search uses the configured algorithms (e.g. Random, NSGA_II) to find transformation sequences that reach the goal and minimize solution length.
4. Results are written under **output/** (objectives, solutions, exported models). You can load the exported XMI models back into **blocky_game** to inspect the solution.

## Configuration summary

- **Fitness**:  
  - **GoalReached** (maximize): 1 if the simulated program reaches the goal, 0 otherwise.  
  - **SolutionLength** (minimize): number of rule applications (“minimal number of changes”).  
  - **MustReachGoal** (constraint): high penalty if the goal is not reached.
- **Algorithms**: Random search and NSGA-II (configurable in `blocky.momot`).
- **Paths** in the config are relative to the project root when run from Eclipse.

## Generating better Henshin and MoMoT files

To avoid common runtime errors and get correct behavior (e.g. empty solution):

1. **Henshin rules (generator / .henshin_text)**  
   - See [docs/henshin/09-generating-henshin-text.md](../docs/henshin/09-generating-henshin-text.md): use **package nsURI** in the compiled .henshin (not Ecore file path), **no IN parameters** if MoMoT won’t set them, and for slots on objects under the root (e.g. `Level.solution`) include **root + containment edge** in the LHS (e.g. `node game`, `node level`, `edges [(game->level:levels)]`).
2. **Compiled .henshin**  
   After each “Transform to Henshin”, replace `../model/blocky.ecore#` with `http://www.example.org/blocky#` in the .henshin file, or run the generator with two args so it rewrites the .henshin (see blocky_model **transformations/README.md**).
3. **MoMoT config**  
   See [docs/momot/05-ecore-henshin-integration.md](../docs/momot/05-ecore-henshin-integration.md): register the Ecore package in **initialization** and in **model.adapt** on the resource set; use **ignoreUnits** for rules that have IN parameters you don’t set.

## References

- Blocky project summary: [PROJECT_SUMMARY.md](../PROJECT_SUMMARY.md)  
- MOMoT docs: [docs/momot/README.md](../docs/momot/README.md), [02-syntax-and-structure.md](../docs/momot/02-syntax-and-structure.md), [03-search-orchestration.md](../docs/momot/03-search-orchestration.md), [04-experiment-analysis-results.md](../docs/momot/04-experiment-analysis-results.md), [08-reference.md](../docs/momot/08-reference.md)
