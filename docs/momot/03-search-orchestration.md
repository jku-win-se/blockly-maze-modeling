# Search orchestration

The **`search`** block is required and assigns a **SearchOrchestration**. It defines the input model, solution length, transformation modules, fitness (objectives and optional constraints), and algorithms.

**Grammar:** `"search" (name=ValidID)? OpSingleAssign searchOrchestration = SearchOrchestration`  
**Structure:** A block `{ ... }` containing the following keys (all except `equalityHelper` are required inside the block).

## model

**Type:** InputModel — a block with `file` and optional `adapt`.

- **`file`** (required): An expression that evaluates to a **string**: the **project-relative path** to the input model (XMI). Example: `file = "model/input.xmi"` or `file = "problem/Cart_Item.xmi"`.
- **`adapt`** (optional): An XBlockExpression that **adapts the loaded model** before search. It receives the **root** EObject of the graph. The block must return the (possibly modified) root. Parameters available in scope: `root` (EObject). Use when the initial model must be prepared (e.g. add elements, distribute features). Example:

  ```momot
  adapt = {
     var cm = root as ClassModel
     // ... modify cm ...
     return cm
  }
  ```

Paths are resolved relative to the project (see [05-ecore-henshin-integration](05-ecore-henshin-integration.md)).

## solutionLength

**Type:** XExpression (evaluates to **int**).

- Maximum length of a **transformation sequence** (number of rule/unit applications). Example: `solutionLength = 30` or `solutionLength = 10`.

## transformations

**Type:** ModuleOrchestration — a block with the following keys.

### modules (required)

- **Array of strings**: project-relative paths to **.henshin** module files. Example: `modules = [ "transformations/architecture.henshin" ]` or `modules = [ "model/Refactoring.henshin", "model/Extra.henshin" ]`.

### ignoreUnits (optional)

- **Array of expressions** evaluating to **strings**: **unit names** to **exclude from the search space**. Each name must be a unit (rule or composite unit) that **exists** in the referenced modules (validator checks this). Example: `ignoreUnits = [ StackModule.CreateStack.NAME, StackModule.ConnectStacks.NAME ]` or string literals if the names are known.

### ignoreParameters (optional)

- **Array of expressions** evaluating to **strings**: **qualified parameter names** to treat as **non-solution parameters** (fixed by parameterValues or not part of the solution variable). Format: `ModuleName.UnitName.ParameterName` (or as returned by the Henshin API). Each must exist in the modules (validator checks). Example: `ignoreParameters = [ StackModule.ShiftLeft.Parameter.FROM_LOAD, ... ]`.

### parameterValues (optional)

- A block of **name : value** pairs. **Name** is an expression (often a constant like `ModuleName.UnitName.ParameterName`); **value** is a **constructor call** implementing **IParameterValue<?>** (e.g. `new RandomIntegerValue(1, 5)`, `new RandomDoubleValue()`). Used to supply or randomize parameter values during search. Parameter names must exist in the modules; duplicate keys are an error (see [06-validation-and-errors](06-validation-and-errors.md)).

  Example:

  ```momot
  parameterValues = {
     StackModule.ShiftLeft.Parameter.AMOUNT  : new RandomIntegerValue(1, 5)
     StackModule.ShiftRight.Parameter.AMOUNT : new RandomIntegerValue(1, 5)
  }
  ```

**Important:** All unit names in `ignoreUnits` and all parameter names in `ignoreParameters` and `parameterValues` must match units/parameters in the referenced .henshin modules.

## fitness

**Type:** FitnessFunctionSpecification. Structure:

- **constructor** (optional): XConstructorCall — custom fitness function class (advanced).
- **preprocess** (optional): XBlockExpression — run before evaluating objectives/constraints.
- **objectives** (required): Block with one or more **FitnessDimensionSpecification** entries. Each entry: `Name : minimize | maximize <value>`.
- **constraints** (optional): Same structure as objectives; constraints are typically evaluated as penalties or hard constraints.
- **postprocess** (optional): XBlockExpression — run after evaluating objectives/constraints.
- **solutionRepairer** (optional): XConstructorCall — e.g. `new TransformationPlaceholderRepairer` to repair invalid solutions.

### Objective/constraint value forms

Each fitness dimension has a **name** (ValidID), a **direction** (`minimize` or `maximize`), and a **value**, which can be:

1. **Constructor call** (FitnessDimensionConstructor): e.g. `SolutionLength : minimize new TransformationLengthDimension`. The type must implement a fitness dimension (e.g. `IFitnessDimension<TransformationSolution>` / `AbstractEGraphFitnessDimension`).
2. **XBase block** (FitnessDimensionXBase): e.g. `CouplingRatio : minimize { FitnessCalculator.calculateCoupling(root as ClassModel) }`. The block has access to **`root`** (EObject), **`solution`** (TransformationSolution), **`graph`** (EGraph). The block’s result must be a number (double).
3. **OCL string** (FitnessDimensionOCL): e.g. `ContentSize : minimize "properties->size() * 1.1 + entities->size()"`. Optional **def** block for OCL helpers: `"query" { def helper = "..." }`. The OCL context is the **root** EClass. The expression must evaluate to a number.

Examples:

```momot
objectives = {
   SolutionLength : minimize new TransformationLengthDimension
   CouplingRatio   : minimize { FitnessCalculator.calculateCoupling(root as ClassModel) }
   CohesionRatio   : maximize { FitnessCalculator.calculateCohesion(root as ClassModel) }
   ContentSize     : minimize "properties->size() * 1.1 + entities->size()"
}
```

## algorithms

**Type:** AlgorithmList — a block of **name : expression** pairs.

- **Names** must be **unique** (duplicate algorithm name is an error).
- **Value** is an expression that returns an algorithm or a factory registration (e.g. `moea.createNSGAII(...)`, `moea.createNSGAIII(...)`, `moea.createEpsilonMOEA()`, `moea.createRandomSearch()`). Commonly used arguments: selection (e.g. `new TournamentSelection(2)`), crossover (e.g. `new OnePointCrossover(1.0)`), mutation (e.g. `new TransformationPlaceholderMutation(0.15)`), and for parameter mutation `TransformationParameterMutation(0.1, orchestration.moduleManager)`.

Example:

```momot
algorithms = {
   Random   : moea.createRandomSearch()
   NSGAII   : moea.createNSGAII(
      new TournamentSelection(2),
      new OnePointCrossover(1.0),
      new TransformationPlaceholderMutation(0.15))
   NSGAIII  : moea.createNSGAIII(
      new TournamentSelection(2),
      new OnePointCrossover(1.0),
      new TransformationPlaceholderMutation(0.15))
}
```

## equalityHelper (optional)

**Type:** EqualityHelper — either a **constructor call** or a **block expression** implementing **IEObjectEqualityHelper**. Use when solution parameters are EObjects that do not implement `equals` properly, so the search can compare solutions correctly.

---

## Summary

| Key              | Required | Description |
|------------------|----------|-------------|
| model            | yes      | `{ file = "<path>" }` or with `adapt = { ... }` |
| solutionLength   | yes      | int expression (max transformation steps) |
| transformations  | yes      | `{ modules = [ ... ], ignoreUnits?, ignoreParameters?, parameterValues? }` |
| fitness          | yes      | `{ objectives = { ... }, constraints?, solutionRepairer?, ... }` |
| algorithms       | yes      | `{ AlgName : factoryCall, ... }` (unique names) |
| equalityHelper   | no       | Constructor or block for IEObjectEqualityHelper |

Unit and parameter names used in `ignoreUnits`, `ignoreParameters`, and `parameterValues` must exist in the .henshin modules listed in `modules` (see [05-ecore-henshin-integration](05-ecore-henshin-integration.md) and [06-validation-and-errors](06-validation-and-errors.md)).
