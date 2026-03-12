# Syntax and structure

The .momot format is defined by an **Xtext** grammar that extends **Xbase** (Java-like expressions, imports, blocks). This document summarizes the file extension, root rule, section order, operators, and Xbase usage.

**Source:** [MOMoT.xtext](../plugins/at.ac.tuwien.big.momot.lang/src/at/ac/tuwien/big/momot/lang/MOMoT.xtext)

## File extension

- Use the extension **`.momot`** (the grammar declares `fileExtensions = "momot"`).

## Root rule

- The root rule is **`MOMoTSearch`**. Every valid .momot file is one instance of `MOMoTSearch`.

## Order of top-level sections

From the grammar, the **required order** is:

1. **`package`** (optional) — `package` followed by a qualified name.
2. **`importSection`** (optional) — Xbase imports.
3. **`variables`** (optional) — zero or more `var` declarations.
4. **`initialization`** (optional) — `initialization` `=` block expression.
5. **`search`** (required) — `search` optionally a name `=` `SearchOrchestration`.
6. **`experiment`** (required) — `experiment` `=` `ExperimentOrchestration`.
7. **`analysis`** (optional) — `analysis` `=` `AnalysisOrchestration`.
8. **`results`** (optional) — `results` `=` `ResultManagement`.
9. **`finalization`** (optional) — `finalization` `=` block expression.

Example skeleton:

```momot
package my.package.name
import ...
var x = ...
initialization = { ... }
search = { ... }
experiment = { ... }
analysis = { ... }
results = { ... }
finalization = { ... }
```

## Operators

- **`=`** (OpSingleAssign): Used for assigning a value to a keyword (e.g. `search = { ... }`, `file = "model/input.xmi"`, `solutionLength = 10`).
- **`:`** (OpKeyAssign): Used for key-style entries:
  - Fitness dimensions: `Name : minimize ...` or `Name : maximize ...`.
  - Algorithm specifications: `AlgorithmName : moea.createNSGAII(...)`.
  - Parameter values: `ParameterRef : new RandomIntegerValue(1, 5)`.
  - Analysis grouping: `GroupName : [ Alg1, Alg2 ]`.

## Xbase usage

The grammar imports Xbase, so the following are available:

- **Imports:** Standard Java-style `import fully.qualified.TypeName`. For reserved words used as segment names (e.g. `search`, `fitness`), use the **caret escape**: `import at.ac.tuwien.big.momot.^search.^fitness.dimension.TransformationLengthDimension`.
- **Variables:** `var name = expression` or `var TypeName name = expression`.
- **Block expressions:** `{ statement1; statement2; expression }`. The last expression is the block’s value. Use `return expression` to return from the block when needed.
- **Literals:** String literals in **double quotes** (`"..."`). List literals: `[ expr1, expr2, expr3 ]`.
- **Types and calls:** Java-like constructor calls `new ClassName(...)`, method calls, and type references. Common in .momot: `moea.createNSGAII(...)`, `new TournamentSelection(2)`, `new TransformationPlaceholderRepairer`, etc.

## Keyword escaping in imports

Xtext/Xbase treat some identifiers as keywords. If a **package or type name** contains a keyword (e.g. `search`, `fitness`), prefix that segment with **`^`** in the import:

- `import at.ac.tuwien.big.momot.^search.^fitness.dimension.TransformationLengthDimension`
- `import at.ac.tuwien.big.moea.^experiment.executor.listener.SeedRuntimePrintListener`

## Block structure

- **SearchOrchestration**, **InputModel**, **ModuleOrchestration**, **FitnessFunctionSpecification**, **ExperimentOrchestration**, **AnalysisOrchestration**, **ResultManagement** are all written as **blocks** `{ ... }` with key-value pairs inside (key `=` value or key `:` value as per grammar).
- Lists use **brackets**: `[ "a.henshin", "b.henshin" ]`, `[ hypervolume, spacing ]`.

## Summary

| Element        | Syntax / note                                      |
|----------------|----------------------------------------------------|
| File extension | `.momot`                                           |
| Root           | `MOMoTSearch`                                      |
| Assignment     | `keyword = value` for sections and most options   |
| Key-style      | `Name : minimize/maximize value`, `AlgName : call` |
| Strings        | Double-quoted `"..."`                              |
| Lists          | `[ a, b, c ]`                                      |
| Imports        | Use `^` before keyword segments (e.g. `^search`)   |

See [03-search-orchestration](03-search-orchestration.md) and [04-experiment-analysis-results](04-experiment-analysis-results.md) for the exact keys and structure of each block.
