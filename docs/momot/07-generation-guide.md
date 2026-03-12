# Generation guide: (.ecore + .henshin + prompt) → .momot

This document is a **deterministic step-by-step procedure** for AI agents to generate a valid .momot file from:

1. **.ecore** metamodel file(s)
2. **.henshin** transformation module file(s)
3. A **natural-language prompt** (e.g. “minimize coupling and solution length”, “use NSGA-III and save models”)

---

## Step 1: Gather inputs

- Obtain the **.ecore** file(s) that define the metamodel of the input model.
- Obtain the **.henshin** file(s) that define the transformation rules/units.
- Obtain the **prompt** (user goal): objectives (minimize/maximize what), constraints, algorithms, experiment size, and what to save (objectives, solutions, models).

---

## Step 2: Parse .ecore

- Identify the **EPackage** (name or nsURI) and the **root EClass** of the input model (the top-level container type).
- Determine the **fully qualified Java name** of the generated package singleton (e.g. `my.project.metamodel.MyPackage`). This is used for:
  - **Import** in the .momot file.
  - **initialization** block: `MyPackage::eINSTANCE.eClass` (or `MyPackage.eINSTANCE.eClass`) so that domain types and OCL can use the metamodel.
- If the prompt or fitness uses **domain types** (e.g. “minimize coupling on ClassModel”), ensure this root/package is the one used for casts and OCL context.

---

## Step 3: Parse .henshin

- For each .henshin file:
  - **Module name:** Use the **file name without path and without extension** (e.g. `architecture` for `architecture.henshin`).
  - **Units:** Collect every `<units ... name="...">` (rules and composite units). Record each **name**.
  - **Parameters:** For each unit, collect every `<parameters name="...">`. Build **qualified names**: `ModuleName.UnitName.ParameterName`.
- Use this to:
  - Populate **ignoreUnits** only if the prompt says to exclude certain rules/units.
  - Populate **ignoreParameters** if some parameters must be fixed (non-solution).
  - Populate **parameterValues** if the prompt or common practice requires value generators for parameters (e.g. random integers).

---

## Step 4: Decide paths

- Choose **project-relative paths** that will hold the input model and the modules. Assume the .momot file lives in a known project (e.g. same folder as the .henshin or a parent).
  - **Model:** e.g. `"model/input.xmi"`, `"problem/Cart_Item.xmi"`. Use a path the user can place their XMI at (or document the expected path).
  - **Modules:** e.g. `"model/Refactoring.henshin"`, `"transformations/architecture.henshin"`. List one path per .henshin file.
- Ensure paths are **consistent** with the intended project layout. If the layout is unknown, use generic paths (e.g. `model/input.xmi`, `model/rules.henshin`) and state in comments or docs that paths must be adjusted to the project.

---

## Step 5: Build the skeleton

- **package:** Set to a valid Java package (e.g. derived from the project or prompt). Optional but recommended.
- **importSection:** Add imports for:
  - Fitness dimension types (e.g. `TransformationLengthDimension` from `at.ac.tuwien.big.momot.^search.^fitness.dimension`).
  - Solution repairer (e.g. `TransformationPlaceholderRepairer` from `at.ac.tuwien.big.momot.^search.solution.repair`).
  - Algorithm factories and operators: `moea` (from orchestration), `TournamentSelection`, `OnePointCrossover`, `TransformationPlaceholderMutation` (and `TransformationParameterMutation` if parameterValues are used), from `org.moeaframework` and `at.ac.tuwien.big.momot.^search.algorithm.operator.mutation`.
  - Experiment listener (e.g. `SeedRuntimePrintListener` from `at.ac.tuwien.big.moea.^experiment.executor.listener`).
  - The Ecore package class (e.g. `my.project.metamodel.MyPackage`). Use `^` for keyword segments: `^search`, `^fitness`, `^experiment`.
- **initialization:** Block that registers the Ecore package: `{ MyPackage::eINSTANCE.eClass }` (or equivalent).
- **search:** Empty block to fill in Step 6.
- **experiment:** Block to fill in Step 7.
- **analysis** (optional): Omit unless the prompt asks for analysis output.
- **results** (optional): Omit or minimal (e.g. one objectives/solutions/models command) unless the prompt specifies outputs.
- **finalization** (optional): Omit unless the prompt asks for cleanup or logging.

---

## Step 6: Fill the search block

- **model:** `{ file = "<path>" }` with the path from Step 4. Add `adapt = { ... }` only if the prompt explicitly requires pre-processing the initial model (e.g. “add one class per feature”, “distribute features”). In `adapt`, use `root` (EObject) and return the (possibly modified) root; register the Ecore package in initialization so casts (e.g. `root as ClassModel`) work.
- **solutionLength:** Integer expression (e.g. 10, 30). Choose a reasonable max sequence length from the prompt or default (e.g. 10).
- **transformations:**
  - **modules:** `[ "<path1>", "<path2>", ... ]` from Step 4.
  - **ignoreUnits:** Only if the prompt or .henshin analysis says to exclude units; use names from Step 3. Otherwise omit.
  - **ignoreParameters:** Only if some parameters must be non-solution; use qualified names from Step 3. Otherwise omit.
  - **parameterValues:** Only if the prompt or common practice requires it (e.g. random parameter values); use qualified parameter names and suitable constructors (e.g. `new RandomIntegerValue(1, 5)`). Otherwise omit.
- **fitness:**
  - **objectives:** Map each objective from the prompt to a dimension:
    - “minimize solution length” → `SolutionLength : minimize new TransformationLengthDimension`.
    - “minimize coupling” / “maximize cohesion” → either XBase block (e.g. `{ FitnessCalculator.calculateCoupling(root as ClassModel) }`) or OCL string if a simple expression fits (e.g. `"properties->size()"`). Use **minimize** / **maximize** as appropriate.
  - **constraints:** Add only if the prompt mentions constraints (e.g. hard constraints or penalties).
  - **solutionRepairer:** Add `new TransformationPlaceholderRepairer` unless the prompt says otherwise (recommended for transformation search).
- **algorithms:** At least one algorithm. Use **unique names** (e.g. NSGAII, NSGAIII, Random). Typical:
  - Random: `moea.createRandomSearch()`.
  - NSGA-II: `moea.createNSGAII(new TournamentSelection(2), new OnePointCrossover(1.0), new TransformationPlaceholderMutation(0.15))`.
  - NSGA-III: `moea.createNSGAIII(...)` with same operators. If there are parameters in parameterValues, add `TransformationParameterMutation(0.1, orchestration.moduleManager)` where appropriate.
- **equalityHelper:** Omit unless the prompt or validator warns about parameter type equality.

---

## Step 7: Fill the experiment block

- **populationSize:** Integer (e.g. 50–100). Avoid ≤ 10 (validator warning).
- **maxEvaluations:** Integer (e.g. 1500–10000). Ensure **maxEvaluations / populationSize ≥ 10** (validator warning).
- **nrRuns:** Integer. Prefer ≥ 30 for statistical validity (validator warning).
- **progressListeners:** e.g. `[ new SeedRuntimePrintListener ]` if progress output is desired.
- **collectors:** Omit or add (e.g. `[ hypervolume, invertedGenerationalDistance ]`) if the prompt asks for specific metrics.
- **referenceSet:** Only if the prompt provides or requires a reference set file path.

---

## Step 8: Optional analysis and results

- **analysis:** Add only if the prompt asks for analysis. Include:
  - **indicators** (e.g. `[ hypervolume, invertedGenerationalDistance ]`),
  - **significance** (e.g. 0.01),
  - **show** (e.g. `[ aggregateValues, statisticalSignificance, individualValues ]`),
  - and optionally **outputFile**, **boxplotDirectory**, **printOutput**.
- **results:** Add if the prompt asks to save outputs:
  - **objectives:** `outputFile` and/or `printOutput`; optionally **algorithms**, **neighborhoodSize** or **maxNeighborhoodSize**.
  - **solutions:** **outputFile** and/or **outputDirectory**, and optionally **algorithms**, **neighborhoodSize**.
  - **models:** **outputDirectory**, and optionally **algorithms**, **neighborhoodSize** (e.g. kneepoints with **maxNeighborhoodSize**).
  - **adaptModels:** Only if the prompt says to post-process models before saving (e.g. remove empty elements).

---

## Step 9: Henshin and Ecore runtime (optional but recommended)

- **Compiled .henshin:** If the transformation module is produced by compiling `.henshin_text`, the resulting `.henshin` often contains type hrefs to the Ecore file path. At runtime this can cause **"Missing factory for 'Node newBlock:null'"**. Ensure the .henshin uses the **package nsURI** for type references (e.g. `http://www.example.org/blocky#//MoveForward`), or document that the user must replace path-based hrefs after each compile. See [05-ecore-henshin-integration](05-ecore-henshin-integration.md#compiled-henshin-and-runtime-type-resolution).
- **Package registration:** Register the Ecore package in `initialization` and, when loading the model, on the resource set (e.g. in `adapt`) so that nsURI-based type refs in the .henshin resolve. See [05-ecore-henshin-integration](05-ecore-henshin-integration.md).

---

## Step 10: Validation checklist

Before returning the .momot file, verify:

1. **Paths:** `search.model.file` and every `search.transformations.modules` entry are project-relative; document or assume they exist in the project.
2. **Algorithm names:** All names in `search.algorithms` are unique.
3. **Units:** Every name in `ignoreUnits` (if any) exists in the .henshin modules parsed in Step 3.
4. **Parameters:** Every name in `ignoreParameters` and every key in `parameterValues` (if any) exists in the modules; no duplicate keys in `parameterValues`.
5. **OCL:** If any objective or constraint uses OCL, the expression is valid for the root EClass identified in Step 2.
6. **Ecore:** If domain types or OCL are used, `initialization` registers the Ecore package from Step 2.
7. **Grammar:** Section order is: package → imports → variables → initialization → search → experiment → analysis → results → finalization. Use `=` for section/key assignment and `:` for fitness dimensions and algorithm/parameter entries.

---

## Summary

| Step | Action |
|------|--------|
| 1 | Gather .ecore, .henshin, and prompt. |
| 2 | Parse .ecore: EPackage, root EClass, package class name for registration. |
| 3 | Parse .henshin: module names, unit names, qualified parameter names. |
| 4 | Choose project-relative paths for model and modules. |
| 5 | Build skeleton: package, imports, initialization, search, experiment, optional analysis/results. |
| 6 | Fill search: model, solutionLength, transformations, fitness, algorithms. |
| 7 | Fill experiment: populationSize, maxEvaluations, nrRuns, optional listeners/collectors. |
| 8 | Add analysis and results only if the prompt requires them. |
| 9 | (Optional) Ensure .henshin uses nsURI for types and package is registered on the resource set. |
| 10 | Run validation checklist and fix any violations. |

Following these steps produces a .momot file that is parseable and semantically valid provided the referenced files exist and names match the .henshin and .ecore content. See [05-ecore-henshin-integration](05-ecore-henshin-integration.md) for .henshin type resolution and [08-reference](08-reference.md) for quick lookup of keywords and rule names.
