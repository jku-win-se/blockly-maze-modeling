# MOMoT documentation for AI agents

This folder contains structured markdown documentation so **AI agents** can generate **valid `.momot` files** from:

- **`.ecore`** metamodel(s) (domain of the models)
- **`.henshin`** transformation module(s) (rules/units)
- A **natural-language prompt** (e.g. "minimize coupling and solution length")

## Target audience

Agents that take .ecore and .henshin files plus a user prompt and output a complete, parseable, and semantically valid `.momot` orchestration file.

## How to use this documentation

| Goal | Read |
|------|------|
| Generate a .momot from scratch | [01-overview](01-overview.md) → [05-ecore-henshin-integration](05-ecore-henshin-integration.md) → [07-generation-guide](07-generation-guide.md) |
| Understand syntax and structure | [02-syntax-and-structure](02-syntax-and-structure.md), [03-search-orchestration](03-search-orchestration.md), [04-experiment-analysis-results](04-experiment-analysis-results.md) |
| Avoid validation errors | [06-validation-and-errors](06-validation-and-errors.md) |
| Look up keywords and rules | [08-reference](08-reference.md) |

**Full generation path:** 01 → 02 → 03 → 04 → 05 → 06 → 07; use 08 for quick reference while generating.

## Document index

- **[01-overview](01-overview.md)** — What MOMoT is; roles of .ecore, .henshin, and .momot
- **[02-syntax-and-structure](02-syntax-and-structure.md)** — Grammar, file extension, section order, Xbase
- **[03-search-orchestration](03-search-orchestration.md)** — `search`: model, solutionLength, transformations, fitness, algorithms
- **[04-experiment-analysis-results](04-experiment-analysis-results.md)** — experiment, analysis, results
- **[05-ecore-henshin-integration](05-ecore-henshin-integration.md)** — Paths, Ecore registration, .henshin type resolution (nsURI), ignoreUnits vs parameterValues
- **[06-validation-and-errors](06-validation-and-errors.md)** — Validator rules, errors, warnings, how to fix
- **[07-generation-guide](07-generation-guide.md)** — Step-by-step: (.ecore + .henshin + prompt) → .momot
- **[08-reference](08-reference.md)** — Compact grammar and reference tables
- **[examples/](examples/)** — Minimal .momot snippets and examples

## Minimum valid .momot

A minimal valid `.momot` file must define **search** and **experiment**. The following is a minimal example (paths must exist in the project).

```momot
package my.project.momot

import at.ac.tuwien.big.momot.^search.^fitness.dimension.TransformationLengthDimension
import at.ac.tuwien.big.momot.^search.solution.repair.TransformationPlaceholderRepairer
import at.ac.tuwien.big.moea.^experiment.executor.listener.SeedRuntimePrintListener
import at.ac.tuwien.big.momot.^search.algorithm.operator.mutation.TransformationPlaceholderMutation
import org.moeaframework.core.operator.OnePointCrossover
import org.moeaframework.core.operator.TournamentSelection
import my.project.metamodel.MyPackage

initialization = {
   MyPackage::eINSTANCE.eClass
}

search = {
   model = {
      file = "model/input.xmi"
   }
   solutionLength = 10
   transformations = {
      modules = [ "model/rules.henshin" ]
   }
   fitness = {
      objectives = {
         SolutionLength : minimize new TransformationLengthDimension
      }
      solutionRepairer = new TransformationPlaceholderRepairer
   }
   algorithms = {
      NSGAII : moea.createNSGAII(
         new TournamentSelection(2),
         new OnePointCrossover(1.0),
         new TransformationPlaceholderMutation(0.15))
   }
}

experiment = {
   populationSize = 50
   maxEvaluations = 1500
   nrRuns = 30
   progressListeners = [ new SeedRuntimePrintListener ]
}
```

Replace `my.project.momot`, `my.project.metamodel.MyPackage`, `model/input.xmi`, and `model/rules.henshin` with values derived from the given .ecore and .henshin files and project layout. See [07-generation-guide](07-generation-guide.md) for the full procedure.
