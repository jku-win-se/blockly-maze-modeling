# Generating henshin_text (agent and codegen guide)

This document tells agents and code generators how to produce **valid** `.henshin_text` files that pass the parser and validator. Use it when writing scripts or tools that emit henshin_text.

## 1. ePackageImport: qualified name only

**Syntax:** `ePackageImport` followed by a single **qualified name** (EString): one or more identifiers separated by dots.

**Valid:**
```
ePackageImport blocky
ePackageImport bank
ePackageImport my.package.name
```

**Invalid:** Do **not** use a quoted URI or string literal. The grammar does not accept it and will report a parse error (e.g. "missing EOF at '...'").

```
ePackageImport blocky "platform:/resource/blocky_model/model/blocky.ecore"   // INVALID
```

The package is resolved by the tooling from the project (e.g. Ecore in the same workspace). No platform URI in the file.

**If you get "Couldn't resolve reference to EPackage 'blocky'":** The editor cannot find the package. Keep the `.henshin_text` file in the **blocky_model** project, build that project, and ensure the blocky package is registered in the running Eclipse (e.g. use the blocky_henshin_fragment in your run configuration). See [README.md](README.md#troubleshooting-couldnt-resolve-reference-to-epackage-blocky).

---

## 2. EEnum (Ecore enum) attribute values

When a node attribute has an **EEnum** type (e.g. `direction` of type `TurnDirection`, `condition` of type `SensorDirection`), use one of these:

### Option A: Integer literal (recommended for generated files)

Use the **numeric value** of the EEnum literal as defined in the Ecore model. The Henshin/EMF runtime accepts integer for EEnum attributes.

- In Ecore, the first literal usually has value 0, the next 1, etc. (see the `.ecore` file).
- Example: `direction = 0` (LEFT), `direction = 1` (RIGHT); `condition = 0` (AHEAD), `condition = 1` (LEFT), `condition = 2` (RIGHT).

**Pros:** No `javaImport` needed; no "X doesn't exist" from the Java scope. **Cons:** You must know the literal order/value from the metamodel.

### Option B: JavaAttributeValue (EnumType.Literal)

Use the form `EnumType.Literal` (e.g. `TurnDirection.LEFT`, `SensorDirection.AHEAD`). The grammar parses this as **JavaAttributeValue** (ID '.' ID).

- You **must** add `javaImport <package>` to the rule, where `<package>` is the Java package that contains the generated enum class (e.g. `blocky`).
- The validator resolves the class and field in that package. If the package is not on the editor’s classpath, you get "X doesn't exist."

**Use Option A** when generating files so that they validate without depending on the Java package being on the classpath.

---

## 3. Delete rules: no duplicate nodes, no preserve-edges touching delete-nodes

### One declaration per node

Each graph element (node) must be declared **once** with a single action type. Do **not** declare the same node name twice (e.g. both `node block : Block` and `delete node block : Block`). That causes **"Duplicate GraphElements 'block'"**.

**Correct:** Declare the node to be deleted once with the delete action:
```
delete node block : blocky.Block
```

**Wrong:** Declaring it as preserve and again as delete:
```
node block : blocky.Block
...
delete node block : blocky.Block
```

### No preserve-edges between preserve-nodes and delete-nodes

A **preserve** edge (default when no action is given) is **not allowed** between a preserve-node and a delete-node. The validator reports: "A preserve-edge is not allowed between a preserve-node and a delete-node."

**Correct:** For edges that touch the delete-node, use only **delete** (or **create** for the new edge when reparenting). Do **not** list those edges as preserve.

- When deleting a block that has no successor: list only `(delete container->block:ref)` for the containment edge, not `(container->block:ref)`.
- When deleting a block that has a successor (reparent): list `(create container->rest:ref)`, `(delete container->block:ref)`, `(delete block->rest:next)`. Do **not** list `(container->block:ref)` or `(block->rest:next)` as preserve.

Preserve-edges that do **not** touch the delete-node are fine (e.g. `(game->level:levels)`).

---

## 4. Edges: one list per graph

Put all edge specifications for a graph in **one** `edges [ ... ]` list. Multiple edge specs (preserve, create, delete, forbid) can be combined in that single list with commas.

**Valid:**
```
edges [(game->level:levels), (forbid block->nextBlock:next), (delete level->block:solution)]
```

Avoid multiple separate `edges [ ... ]` (and `create edges [ ... ]`) blocks in the same graph; that can lead to parser errors like "mismatched input 'edges' expecting 'node'".

---

## 5. Compiled .henshin: use package nsURI at runtime

The Henshin **compiler** (Transform to Henshin) writes type references in the generated `.henshin` XML using the Ecore file path (e.g. `../model/blocky.ecore#//MoveForward`). At **runtime**, the MoMoT/Henshin interpreter often cannot resolve that path, so types become `null` and you get **"Missing factory for 'Node newBlock:null'"**.

**Fix:** Replace Ecore path hrefs in the compiled `.henshin` with the **package nsURI** so the interpreter resolves types from the registered EPackage:

- In the `.henshin` file, replace every `../model/blocky.ecore#` (or similar path) with `http://www.example.org/blocky#` (use the **nsURI** from your `.ecore` package).
- Do this after each "Transform to Henshin", or use a script: e.g. `python generate_add_block_rules.py <henshin_text_path> <henshin_path>` to regenerate text and then rewrite the compiled `.henshin` (see blocky_model transformations README).

---

## 6. MoMoT-friendly rules (codegen)

If the rules will be used by **MoMoT** (search-based transformation), generate rules that work without extra setup:

1. **Avoid IN parameters and attribute conditions** unless you will configure `parameterValues` in the .momot file. If rules declare `IN allowConditionals:EBoolean` and `conditions [allowConditionals == true]`, MoMoT must set those parameters; if not set, you get **"IN Parameter ... not set"**. Either omit parameters/conditions in the generator, or list those units in **ignoreUnits** in the .momot config.
2. **Empty container / root context:** When adding to a slot on an object that is only reachable from the **root** (e.g. `Level.solution` when the input graph root is `Game`), the LHS must include the root and the containment edge so the matcher can find the container. Example: `node game : blocky.Game`, `node level : blocky.Level`, `edges [(game->level:levels)]`, then forbid/create as usual. Otherwise the search may find no matches (e.g. "Infinity Infinity" when the solution is empty).

---

## 7. Checklist for generated files

- [ ] **ePackageImport**: Qualified name only (e.g. `blocky`). No quoted URI.
- [ ] **EEnum attributes**: Prefer integer literals (value from Ecore). If using `EnumType.Literal`, add `javaImport` for the generated model package.
- [ ] **Delete rules**: Each node declared once (e.g. only `delete node block`). No preserve-edges between preserve-node and delete-node; use delete/create for edges touching the delete-node.
- [ ] **Edges**: Single `edges [ ... ]` per graph with all edge specs comma-separated.
- [ ] **Encoding**: UTF-8 without BOM (avoids "required loop did not match" at line 1).
- [ ] **Extension**: File must have extension `.henshin_text`.
- [ ] **MoMoT**: No IN parameters/conditions unless parameterValues will be set; for slots on objects under the root (e.g. Level.solution), include root + containment edge in LHS.
- [ ] **Compiled .henshin**: After Transform to Henshin, replace Ecore path hrefs with package nsURI in the .henshin file (or run the script’s two-arg mode).

---

## 8. References

- [01-file-structure-and-grammar](01-file-structure-and-grammar.md) — Model, ePackageImport, rule/unit syntax.
- [02-rules-and-graphs](02-rules-and-graphs.md) — Nodes, edges, action types.
- [05-expressions-and-types](05-expressions-and-types.md) — Expressions, atomic values, parameters.
- [06-validation-rules](06-validation-rules.md) — All validator constraints.
- blocky_model **transformations/README.md** — Python generator usage and two-arg .henshin fix.
