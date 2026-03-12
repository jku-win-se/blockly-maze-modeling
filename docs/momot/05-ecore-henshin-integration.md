# Ecore and Henshin integration

This document explains how .momot files relate to **paths**, **.ecore** metamodels, and **.henshin** modules, so an AI agent can generate valid references and names.

## Paths: model and modules

- **Model path** (`search.model.file`) and **module paths** (`search.transformations.modules`) are **project-relative**.
- The validator uses `projectMember(path)` / `projectFileExists(path)` — i.e. paths are resolved relative to the **Eclipse project** that contains the .momot file. At runtime, the base directory is typically that project (or the .momot file location when run headless).
- **Do not** use `platform:/resource/...` in the .momot DSL strings. Use **relative paths** such as:
  - `"model/input.xmi"`
  - `"problem/Cart_Item.xmi"`
  - `"transformations/architecture.henshin"`
  - `"model/Refactoring.henshin"`
- Ensure that the **same relative paths** point to existing files in the project layout where the .momot will live. If the agent does not know the exact folder structure, use placeholder paths and document that the user must place the .ecore/.henshin/.xmi in the expected locations or adjust the paths.

## Ecore: metamodel and registration

- The **input model** (XMI) must **conform** to an Ecore metamodel. The .momot file does **not** reference the .ecore file by path.
- When the .momot script uses **domain types** (e.g. `root as ClassModel`, or OCL/fitness over domain EClasses), the corresponding **Ecore package must be registered** so that the EMF resource set can load the model and resolve types.
- **Registration** is done in the **`initialization`** block by ensuring the package is loaded. Common pattern:
  - `MyPackage::eINSTANCE.eClass` or
  - `MyPackage.eINSTANCE.eClass`
  where `MyPackage` is the generated EPackage interface/class for the metamodel (e.g. `ArchitecturePackage`, `RefactoringPackage`).
- The .ecore file is used at **design time** to generate the `*Package` and model classes; the .momot only needs the **package class name** and a call that triggers registration (as above).

### What to extract from .ecore (for the agent)

- **EPackage name** (or nsURI) to identify the metamodel.
- **Root EClass** of the expected input model (often the one that is the root container), for:
  - Casts in `adapt` or fitness: `root as RootClassName`.
  - OCL context: OCL expressions in fitness are evaluated in the context of the root EClass.
- **Fully qualified package class name** (e.g. `icmt.tool.momot.demo.architecture.ArchitecturePackage`) for imports and the `initialization` block.

## Henshin: modules, units, parameters

- .momot only **references** .henshin files by path in `search.transformations.modules`. It does not define rules.
- **Unit names** and **parameter names** used in `ignoreUnits`, `ignoreParameters`, and `parameterValues` must **exist** in the referenced modules; the MOMoT validator checks this after loading the modules.

### Compiled .henshin and runtime type resolution

The Henshin **compiler** (Transform to Henshin) writes type references in the `.henshin` XML using the Ecore **file path** (e.g. `../model/blocky.ecore#//MoveForward`). At **runtime**, the interpreter may not resolve that path, so types become `null` and you get **"Missing factory for 'Node newBlock:null'"**.

**Fix:** In the compiled `.henshin` file, replace Ecore path hrefs with the **package nsURI** (e.g. `http://www.example.org/blocky#//MoveForward`). Then the interpreter resolves types from `EPackage.Registry` / the resource set’s package registry. Do this after every recompile from `.henshin_text`; the blocky generator script supports a second argument to rewrite the `.henshin` automatically (see blocky_model transformations README).

**Registration:** Ensure the Ecore package is registered both in **initialization** (e.g. `EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg)`) and, when loading the input model, on the **resource set** used by the adapter (e.g. in `search.model.adapt` register the package on `game.eResource().getResourceSet().getPackageRegistry()`). That way the .henshin’s nsURI-based type refs resolve when the transformation runs.

### Henshin XML structure (for parsing by an agent)

- **Module:** Root element `<henshin:Module>`. The **module name** used by MOMoT is typically the **file name without extension** (e.g. `architecture` for `architecture.henshin`). Some code uses `module.eResource().getURI().trimFileExtension().lastSegment()`.
- **Units:** Child elements `<units xsi:type="henshin:Rule" name="...">` or other unit types (e.g. composite units). Each unit has a **`name`** attribute (e.g. `reassignFeature`). These are the names to use in **ignoreUnits** and for qualifying parameters.
- **Parameters:** Under a unit, `<parameters name="..." kind="...">`. Each parameter has a **`name`** (e.g. `featureName`, `className`).

### Qualified names

- **Unit:** `ModuleName.UnitName`. ModuleName = module file name without extension (e.g. `architecture`). UnitName = unit’s `name` attribute. Example: `architecture.reassignFeature`.
- **Parameter:** `ModuleName.UnitName.ParameterName`. Example: `architecture.reassignFeature.featureName`.
- In .momot, these can appear as:
  - **String literals:** `ignoreUnits = [ "reassignFeature" ]` (unit name only; if multiple modules, fully qualified may be required depending on runtime).
  - **Java constants:** If the project has generated constants (e.g. `StackModule.ShiftLeft.Parameter.AMOUNT`), use those for type safety and refactoring.

### ignoreUnits when rules have IN parameters

If the .henshin rules declare **IN parameters** (e.g. `allowConditionals`, `allowLoops`) and **attribute conditions** that use them, the Henshin interpreter requires those parameters to be set before applying the rule. MoMoT does **not** set rule parameters unless you configure **parameterValues** in the .momot file. If you do not configure them, you get **"IN Parameter ... not set"**. Two options:

- **ignoreUnits:** List the unit names of those rules in `search.transformations.ignoreUnits` so they are never applied (smaller search space, no parameter setup).
- **parameterValues:** Provide a value generator for each parameter (e.g. from the model). Then the rules can be applied with the given values.

For minimal setup, generating **parameter-free rules** (no IN parameters / no attribute conditions) or using **ignoreUnits** for parameterized rules is recommended.

### What to extract from .henshin (for the agent)

1. **Module file name** (without path, with or without extension) for listing modules and building qualified names.
2. **All unit names** — from every `<units ... name="...">`. Use for:
   - Knowing the search space.
   - Populating **ignoreUnits** if the prompt says to exclude certain rules/units or if those units have IN parameters that won’t be set.
3. **All parameters** with qualified name `ModuleName.UnitName.ParameterName` — for:
   - **ignoreParameters**: parameters that should not be part of the solution (e.g. fixed or derived).
   - **parameterValues**: parameters that need a value generator (e.g. `new RandomIntegerValue(1, 5)`).

Example extraction (pseudo):

- From `architecture.henshin`: module name `architecture`; units: `reassignFeature`; parameters: `architecture.reassignFeature.featureName`, `architecture.reassignFeature.className`.

### Validator checks

- **Module file existence:** Each path in `modules` must exist in the project (error if missing).
- **Unit existence:** Each name in `ignoreUnits` must resolve to a unit in the loaded modules (error if not found).
- **Parameter existence:** Each name in `ignoreParameters` and each key in `parameterValues` must resolve to a parameter in the loaded modules (error if not found).
- **Duplicate parameter keys:** No duplicate keys in `parameterValues` (error).

See [06-validation-and-errors](06-validation-and-errors.md) for the full list.

## Summary

| Topic | Action for .momot generation |
|-------|-----------------------------|
| Model path | Use project-relative path to input XMI (e.g. `"model/input.xmi"`). |
| Module paths | Use project-relative paths to .henshin files (e.g. `[ "model/rules.henshin" ]`). |
| Ecore | Do not reference .ecore by path. Register package in `initialization` (e.g. `MyPackage::eINSTANCE.eClass`) if using domain types or OCL. |
| Unit names | Parse from .henshin `<units name="...">`; use in `ignoreUnits` only if needed. |
| Parameter names | Parse from .henshin `<parameters name="...">` under each unit; qualify as ModuleName.UnitName.ParameterName for `ignoreParameters` and `parameterValues`. |
| Compiled .henshin | Replace Ecore path hrefs with package nsURI in the .henshin so the interpreter resolves types; register the package in initialization and on the resource set when loading the model. |
| IN parameters | Use ignoreUnits for rules with IN parameters if you do not set parameterValues; or generate parameter-free rules. |

Ensuring paths exist, names match the .henshin modules, and the .henshin uses nsURI for types avoids validator errors and runtime failures (e.g. "Missing factory for Node newBlock:null", "IN Parameter not set").
