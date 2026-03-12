# Examples

This folder contains minimal .momot snippets for AI agents.

| File | Description |
|------|-------------|
| [minimal.momot.example](minimal.momot.example) | Smallest valid structure: package, imports, initialization, search (model, solutionLength, transformations, fitness with one objective, one algorithm), experiment. Replace placeholders with your .ecore/.henshin and paths. |
| [fitness-ocl.momot.example](fitness-ocl.momot.example) | Same as minimal plus an **OCL objective** (`ContentSize : minimize "properties->size() * 1.1 + entities->size()"`) and a **results** block. Use when the prompt asks for OCL-based fitness or saving objectives/models. |

For full runnable examples, see the repository:

- [ArchitectureSearch.momot](../../examples/at.ac.tuwien.big.momot.examples.cra/src/icmt/tool/momot/demo/ArchitectureSearch.momot) — model adapt, multiple objectives, analysis, results.
- [Refactoring.momot](../../examples/at.ac.tuwien.big.momot.examples.refactoring/src/at/ac/tuwien/big/momot/examples/refactoring/Refactoring.momot) — OCL objective, two algorithms, results.
- [StackSearchExample.momot](../../examples/at.ac.tuwien.big.momot.examples.stack/src/at/ac/tuwien/big/momot/examples/stack/StackSearchExample.momot) — ignoreUnits, ignoreParameters, parameterValues, analysis, multiple result commands.
