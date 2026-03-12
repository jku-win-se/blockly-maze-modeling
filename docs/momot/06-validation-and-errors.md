# Validation and errors

The MOMoT language uses a custom validator that runs in the IDE and can report **errors**, **warnings**, and **info**. This document lists the checks so AI-generated .momot files can avoid or fix them.

**Source:** [MOMoTValidator.xtend](../plugins/at.ac.tuwien.big.momot.lang/src/at/ac/tuwien/big/momot/lang/validation/MOMoTValidator.xtend)

**Note:** Some checks are gated by **MOMoTPreferences** (e.g. `evaluationModuleFileExistence`). The documentation still lists them so agents produce robust .momot files that pass validation when those preferences are enabled.

---

## Errors (must fix)

These prevent the file from being considered valid when the corresponding preference is enabled.

| Check | Condition | Message / fix |
|-------|-----------|----------------|
| **Model file does not exist** | `search.model.file` evaluates to a path that is not an existing file in the project. | Error on `SearchOrchestration.model`. Fix: use a project-relative path to an existing XMI file, or create the file. |
| **Module file does not exist** | A path in `search.transformations.modules` does not point to an existing file in the project. | Error on the corresponding element in `modules`. Fix: correct the path or add the .henshin file to the project. |
| **Unit does not exist** | A name in `ignoreUnits` does not match any unit in the loaded Henshin modules. | Error on the element in `unitsToRemove`. Fix: use a unit name that exists in the referenced .henshin (or remove the entry). |
| **Parameter does not exist** | A name in `ignoreParameters` or a key in `parameterValues` does not match any parameter in the loaded modules. | Error on the corresponding element. Fix: use qualified parameter names that exist in the .henshin modules. |
| **Duplicate algorithm name** | Two entries in `search.algorithms` have the same name. | Error on the duplicate `AlgorithmSpecification`. Fix: give each algorithm a unique name. |
| **Duplicate algorithm reference** | In a result command or analysis grouping, the same algorithm name appears more than once in one `AlgorithmReferences` list. | Error on the `AlgorithmReferences` element. Fix: list each algorithm at most once per reference list. |
| **Duplicate parameter key in parameterValues** | The same parameter (evaluated name) appears twice in `parameterValues`. | Error on the duplicate `ParmeterValueSpecification`. Fix: one entry per parameter. |
| **Reference set file does not exist** | `experiment.referenceSet` is set and evaluates to a path that is not an existing file. | Error on `ExperimentOrchestration.referenceSet`. Fix: remove referenceSet or use an existing file path. |
| **None of the transformations can be applied** | With unit applicability checking enabled: after loading the model and modules, no unit can be applied to the initial graph (and no `adapt` is used). | Error on `SearchOrchestration.moduleOrchestration`. Fix: ensure the input model matches the rules’ LHS or provide an `adapt` block. |
| **OCL parse / context error** | An OCL objective or constraint (FitnessDimensionOCL) has a parse error or invalid context. | Error on the OCL dimension (e.g. `query` or `defExpressions`). Fix: correct the OCL expression and ensure the context (root EClass) matches. |

---

## Warnings (should fix for robustness)

| Check | Condition | Message / fix |
|-------|-----------|----------------|
| **nrRuns &lt; 30** | `experiment.nrRuns` evaluates to an integer &lt; 30. | Warning on `ExperimentOrchestration.nrRuns`. Recommendation: use at least 30 runs for statistically valid conclusions. |
| **Population size very small** | `experiment.populationSize` evaluates to ≤ 10. | Warning on `ExperimentOrchestration.populationSize`. Recommendation: use a higher population size. |
| **Too few iterations** | `maxEvaluations / populationSize` &lt; 10. | Warning on `ExperimentOrchestration.maxEvaluations`. Recommendation: use at least 10 iterations (e.g. maxEvaluations ≥ 10 × populationSize). |
| **Many objectives with non-NSGA-II algorithm** | More than 3 objectives and an algorithm that is not NSGA-II. | Warning on the algorithm’s `call`. Recommendation: for &gt; 3 objectives, consider NSGA-III or algorithms suited to many objectives. |
| **Single objective** | Exactly one objective. | Info/suggestion: consider local search algorithms (e.g. HillClimbing, RandomDescent) for single-objective search. |
| **No equals for parameter type** | Solution parameters include a type (e.g. EObject) that does not override `equals`, and no `equalityHelper` is set. | Warning on `ModuleOrchestration.parameterValues`. Recommendation: provide an `equalityHelper` or ensure parameter types implement proper equality. |

---

## Info (informational)

| Check | Condition | Message |
|-------|-----------|---------|
| **Analysis file will be overridden** | `analysis.outputFile` points to an existing file. | Info: the file will be overridden. |
| **Objectives file will be overridden** | An `objectives` result command’s `outputFile` points to an existing file. | Info: the file will be overridden. |
| **Solutions file will be overridden** | A `solutions` result command’s `outputFile` points to an existing file. | Info: the file will be overridden. |

---

## Preferences that gate checks

The validator uses **MOMoTPreferences** to enable/disable certain checks. Even when a check is disabled, generating .momot that satisfy these constraints is recommended:

- **evaluationModelFileExistence** — model file exists
- **evaluationModuleFileExistence** — module files exist
- **evaluationUnitApplicability** — at least one unit applicable
- **evaluationUnitExistence** — units in ignoreUnits exist
- **evaluationParameterExistence** — parameters in ignoreParameters/parameterValues exist
- **evaluationDuplicateAlgorithmName** — no duplicate algorithm names
- **evaluationDuplicateAlgorithmReference** — no duplicate refs in algorithm lists
- **evaluationDuplicateParameterKeys** — no duplicate keys in parameterValues
- **evaluationReferenceSetExistence** — reference set file exists if specified
- **evaluationOCL** — OCL objectives/constraints parse and are valid
- **evaluationNrRuns**, **evaluationPopulationSize**, **evaluationNrIterations** — experiment warnings
- **evaluationManyObjectives**, **evaluationSingleObjective** — algorithm/objective hints
- **evaluationObjectIdentity** — equalityHelper warning
- **evaluationAnalysisFileOverriden**, **evaluationObjectivesFileOverriden**, **evaluationSolutionsFileOverriden** — override info

---

## Checklist before outputting .momot

1. **Paths:** Model `file` and every `modules` entry are project-relative and point to existing files (or document that the user must add them).
2. **Algorithm names:** All names in `search.algorithms` are unique.
3. **Units:** Every name in `ignoreUnits` exists in the referenced .henshin modules (use parsed unit names).
4. **Parameters:** Every name in `ignoreParameters` and every key in `parameterValues` exists in the modules; no duplicate keys in `parameterValues`.
5. **OCL:** If fitness uses OCL, the expression is valid for the root EClass of the loaded model.
6. **Ecore:** If the script uses domain types (e.g. `root as MyRoot`), `initialization` registers the corresponding Ecore package.

See [07-generation-guide](07-generation-guide.md) for the full generation procedure.
