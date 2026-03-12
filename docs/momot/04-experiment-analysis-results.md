# Experiment, analysis, and results

This document describes the **experiment**, **analysis**, and **results** sections of a .momot file.

## experiment (required)

**Type:** ExperimentOrchestration. Block with:

| Key                | Required | Type / description |
|--------------------|----------|--------------------|
| populationSize     | yes      | XExpression → int. Population size per run. |
| maxEvaluations     | yes      | XExpression → int. Maximum fitness evaluations per run. |
| nrRuns             | yes      | XNumberLiteral. Number of independent runs. |
| referenceSet       | no       | XExpression → string. Path to reference set file. |
| progressListeners  | no       | Array of XConstructorCall. e.g. `[ new SeedRuntimePrintListener ]`. |
| collectors         | no       | Mix of built-in names and custom XConstructorCall. See below. |

### Validator hints (experiment)

- **nrRuns &lt; 30:** Warning — at least 30 runs recommended for statistically valid conclusions.
- **populationSize ≤ 10:** Warning — population size very small.
- **maxEvaluations / populationSize &lt; 10:** Warning — too few iterations (recommend at least 10, i.e. maxEvaluations ≥ 10 × populationSize).
- **referenceSet:** If set, the file must exist (error if missing).

### Collectors (experiment)

**collectors** is an array that can contain:

- **Built-in names** (from CollectorArray): `hypervolume`, `generationalDistance`, `invertedGenerationalDistance`, `spacing`, `additiveEpsilonIndicator`, `contribution`, `R1`, `R2`, `R3`, `adaptiveMultimethodVariation`, `adaptiveTimeContinuation`, `approximationSet`, `epsilonProgress`, `elapsedTime`, `populationSize`.
- **Custom collectors:** Constructor calls (e.g. custom `Collector` implementations).

Example:

```momot
experiment = {
   populationSize    = 100
   maxEvaluations    = 10000
   nrRuns            = 30
   progressListeners = [ new SeedRuntimePrintListener ]
   collectors        = [ hypervolume, invertedGenerationalDistance ]
}
```

---

## analysis (optional)

**Type:** AnalysisOrchestration. When present, the block **must** contain:

| Key           | Required | Type / description |
|---------------|----------|---------------------|
| indicators    | yes      | IndicatorArray — list of indicator names (see below). |
| significance  | yes      | XNumberLiteral (e.g. 0.01 for 1% significance). |
| show          | yes      | ShowArray — what to show (see below). |
| grouping      | no       | AnalysisGroupList — named groups of algorithms for comparison. |
| outputFile    | no       | SaveAnalysisCommand: `outputFile` `=` string (path for analysis output). |
| boxplotDirectory | no    | BoxplotCommand: `boxplotDirectory` `=` string. |
| printOutput   | no       | PrintAnalysisCommand: keyword `printOutput` (no value). |

### Indicators (indicators)

Allowed names in **IndicatorArray** (list notation `[ name1, name2 ]`):

- `hypervolume`
- `generationalDistance`
- `invertedGenerationalDistance`
- `spacing`
- `additiveEpsilonIndicator`
- `contribution`
- `R1`, `R2`, `R3`
- `maximumParetoFrontError`

### Show (show)

Allowed names in **ShowArray**:

- `individualValues`
- `aggregateValues`
- `statisticalSignificance`

### Grouping (grouping)

Optional block of **group name : algorithm list**:

- **AnalysisGroupList:** `{ GroupName : [ Alg1, Alg2 ], ... }`. Each value is an **AlgorithmReferences** list: algorithm names defined in `search.algorithms` (e.g. `[ NSGAII, NSGAIII ]`).

### Analysis output commands

- **outputFile** = `"path/to/analysis.txt"` — where to save analysis text.
- **boxplotDirectory** = `"path/to/boxplot/"` — directory for boxplot outputs.
- **printOutput** — print analysis to console (no path).

Example:

```momot
analysis = {
   indicators       = [ hypervolume, invertedGenerationalDistance ]
   significance     = 0.01
   show             = [ aggregateValues, statisticalSignificance, individualValues ]
   outputFile       = "output/analysis/analysis.txt"
   boxplotDirectory = "output/analysis/"
   printOutput
}
```

---

## results (optional)

**Type:** ResultManagement. Block with:

- **adaptModels** (optional): XBlockExpression. Applied to the **root** of each result model before saving (e.g. remove empty elements). Same style as `search.model.adapt`; has access to `root`.
- **commands** (required): One or more of: **objectives**, **solutions**, **models**. Each command can appear multiple times with different options (e.g. different algorithms or neighborhoodSize).

### objectives

Block with:

- **algorithms** (optional): `[ AlgName1, AlgName2 ]` — restrict to these algorithms (names from `search.algorithms`).
- **neighborhoodSize** (optional): int or `maxNeighborhoodSize` — for kneepoint selection.
- **outputFile** (optional): string path.
- **printOutput** (optional): keyword.

### solutions

Block with:

- **algorithms** (optional): as above.
- **neighborhoodSize** (optional): as above.
- **outputFile** (optional): string path.
- **outputDirectory** (optional): string path.
- **printOutput** (optional): keyword.

### models

Block with:

- **algorithms** (optional): as above.
- **neighborhoodSize** (optional): as above.
- **outputDirectory** (optional): string path.
- **printOutput** (optional): keyword.

Example:

```momot
results = {
   adaptModels = {
      val cm = root as ClassModel
      cm.classes.removeAll(cm.classes.filter[c | c.encapsulates.size == 0])
   }
   objectives = {
      outputFile  = "output/objectives/objective_values.txt"
      printOutput
   }
   solutions = {
      outputFile      = "output/solutions/all_solutions.txt"
      outputDirectory = "output/solutions/"
   }
   models = {
      outputDirectory = "output/models/"
   }
   models = {
      algorithms      = [ NSGAIII, NSGAII ]
      neighborhoodSize = maxNeighborhoodSize
      outputDirectory  = "output/models/kneepoints/"
   }
}
```

---

## Summary

| Section    | Required | Purpose |
|------------|----------|---------|
| experiment | yes      | populationSize, maxEvaluations, nrRuns; optional listeners and collectors. |
| analysis   | no       | indicators, significance, show; optional grouping, outputFile, boxplotDirectory, printOutput. |
| results    | no       | Optional adaptModels; one or more objectives/solutions/models commands with optional algorithms, neighborhoodSize, output paths. |

Validator warnings for experiment (nrRuns, populationSize, iterations) are described in [06-validation-and-errors](06-validation-and-errors.md).
