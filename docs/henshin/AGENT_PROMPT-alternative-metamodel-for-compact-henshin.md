# Agent prompt: Alternative Blocky metamodel for compact Henshin rules

Use this document as the **full briefing** for an investigation agent. The goal is to determine whether we can **remodel** the Blocky game program representation so that **very few Henshin rules** suffice to cover **all** valid edit operations (insert, delete, substitute, and structural changes), instead of the current **combinatorial explosion** of rules over multiple list heads and concrete block types.

---

## 1. Context (read first)

### 1.1 Current situation

- Programs are **EMF models** conforming to `blocky` (`blocky_model/model/blocky.ecore`).
- **Current (as of Apr 2026)**: programs are represented as **container-linked lists**:
  - `Level.solution : Body` owns the top-level body
  - `Body.firstContainer` / `Container.next` form the containment list
  - `Container.statement : Statement` holds the payload (`AtomicStatement`, `Loop`, `IfStmt`)
- **Henshin** rules must spell out **concrete** `EClass` on `create` nodes and **concrete** `EReference` names on edges. There is no “insert at any list head” abstraction in the rule language itself.
- Result: to cover “every change” naively requires **many** rules (locations × operations × concrete types × last/has-next variants). See [10-rule-combinatorics-block-lists.md](10-rule-combinatorics-block-lists.md).

### 1.2 Product constraints (non-negotiable semantics)

The **surface language** the player sees must remain conceptually:

1. **Three leaf actions only**
   - `MOVE FORWARD`
   - `TURN LEFT`
   - `TURN RIGHT`

2. **Conditionals** may only test whether there is a **path** (or “way”) in a direction:
   - **FORWARD**
   - **LEFT**
   - **RIGHT**  
   (Aligned with current `ConditionKind`: CHECK_FORWARD, CHECK_LEFT, CHECK_RIGHT in the metamodel.)

3. **Higher-level constructs** (e.g. loops and conditionals like `Loop` / `IfStmt`) may exist in the metamodel **only if** they are still expressible in terms of the above semantics and remain valid for the game engine / UI.

The investigation may propose **internal** representations that differ from the current tree-of-blocks, as long as **mapping to/from** the player-visible program (or to the current model for migration) is defined.

---

## 2. Objective

**Primary question:** Can we redesign the **program model** (and optionally a thin view model) so that:

- A **small, fixed set** of Henshin rules (give a target band: e.g. **≤ 15–30 rules**, or justify a different number) can express **all** edit operations we care about for search / neighborhood exploration (e.g. Levenshtein-style neighbors, or MOMoT move operators).

**Secondary questions:**

- What is the **minimal** set of **structural** node/edge kinds if leaves are only the three actions?
- How do we represent **`if`** and **`if_else`** (and loops, if kept) without multiplying rule patterns?
- How do we avoid **per-reference** rules (e.g. `Body.firstContainer` vs `Container.next`, and nested `Body` references like `Loop.body`, `IfStmt.thenBody`, `IfStmt.elseBody`)?
- Can **substitute** be unified with **typed slot + value** so one rule family covers “change what’s there”?

---

## 3. What to investigate (dimensions)

### 3.1 Uniform list / program counter

Consider replacing multiple containment heads with:

- A single **`Program`** or **`Script`** owning **one** ordered list (`EList` or one head + `next` only), and represent nesting via **explicit markers** (see below), **or**
- A **flat sequence of instructions** with **stack/PC semantics** (bytecode-like), **or**
- A **single** `Container.next` chain under `Level.solution` only, with **no** nested bodies—nesting expressed only through marker nodes.

For each option, analyze:

- **Henshin rule count** for: insert at index, delete at index, replace at index (or equivalent).
- **Mapping** to current `IfStmt` / `Loop` / nested bodies.
- (If you refer to current metamodel elements: use `IfStmt` / `Loop` / `Body` / `Container` naming.)
- **Validity constraints** (e.g. balanced markers, well-formed if/loop).

### 3.2 Marker / opcode representation

Evaluate a representation where non-leaves are **only**:

- `IF_OPEN(condition)` / `IF_ELSE` / `IF_CLOSE` (or similar), and optionally `LOOP_OPEN` / `LOOP_CLOSE`, **or**
- A small fixed set of **enum-driven** “frame” nodes with **no** per-kind EClass explosion.

Assess whether **one** insert rule can target “between any two sequence elements” without knowing `solution` vs `body`.

### 3.3 Parametric nodes instead of many EClasses

If the metamodel allowed **one** concrete class per “family” with **EAttributes** (e.g. `kind`, `direction`, `sensor`), Henshin could use **`set`** on preserve nodes for substitute, and **one** create pattern with attribute initialization.

**Caution:** Henshin still requires **concrete** EClass on `create`; the win is **fewer** EClasses, not zero rules. Quantify how many rules remain for:

- create with different attribute literals (may still need one rule per literal, unless the tool supports parameterized creation—check Henshin limits).

### 3.4 Placeholder + fill (two-phase edits)

Evaluate introducing **`Hole` or `UnknownBlock`** (concrete `Block` subclass):

- Phase 1: rules only insert/delete **holes** in a **uniform** list.
- Phase 2: rules **specialize** hole → `MoveForward` / `Turn` / `If` / …

Trade-off: **two-step** Levenshtein vs **single-step** “insert real statement”. Is total rule count smaller? Is search behavior acceptable?

### 3.5 Graph vs tree

Consider **basic blocks** or **CFG** (control-flow graph) instead of a tree:

- Fewer patterns for “insert edge” vs “insert in nested containment”?
- Cost: harder serialization to “blocks UI”, need a canonical serialization for the game.

### 3.6 Loops (`Loop`)

Decide whether loops stay, become **sugar** for a bounded unroll (probably bad), or map to **one** loop node with **single** body reference that could be merged into a uniform list representation. (In the current metamodel, the loop node is `Loop` with `Loop.body : Body`.)

---

## 4. Henshin-specific acceptance criteria

For each proposed metamodel sketch, produce:

1. **Estimated rule count** broken down by:
   - insert (one rule or many?)
   - delete (one rule or many? containment rewiring?)
   - substitute / specialize
   - well-formedness repair (if any)

2. **Concrete example** of **one** insert and **one** delete rule in **henshin_text** pseudocode or real snippets (valid patterns only).

3. **MoMoT / search implications**: IN parameters, root context (`Game` vs `Level`), need for `ignoreUnits`.

4. **Migration**: how to convert **existing** `.xmi` / current `Block` trees to the new shape and back.

---

## 5. Deliverables (what the agent must output)

1. **Shortlist (2–4)** of viable alternative metamodels with **pros/cons** and **estimated Henshin rule cardinality**.

2. **One recommended** design with:
   - UML-style class diagram or bullet structure (EClasses, references, containment).
   - **Invariant** text (what makes a valid program).
   - **Rule inventory** (numbered list of rules; aim for compactness).

3. **Explicit statement** whether **true** “single rule for all inserts everywhere” is achievable in **EMF + Henshin** or **fundamentally** blocked (and why).

4. **Risks**: engine changes, UI block editor, serialization, backward compatibility.

---

## 6. References in this repo

- [10-rule-combinatorics-block-lists.md](10-rule-combinatorics-block-lists.md) — why the current model multiplies rules.
- [09-generating-henshin-text.md](09-generating-henshin-text.md) — MoMoT, delete edges, enums.
- `blocky_model/model/blocky.ecore` — current metamodel.
- `blocky_model/transformations/generate_levenshtein_neighbors.py` — current codegen approach.

---

## 7. Instructions for the executing agent

1. **Do not** assume the current `Block` tree is sacred; challenge it against the **leaf-action** and **sensor** constraints.
2. **Quantify** “compact”: rule count, lines of `.henshin_text`, or number of **distinct graph shapes**.
3. **Separate** “player model” vs “editor/search model” if a dual representation reduces rules.
4. If a design **reduces** rules but **breaks** a constraint in §1.2, **discard** it or show an equivalent encoding.
5. End with a **clear go/no-go** recommendation for a **metamodel refactor** vs **keep current model + codegen**.

---

## 8. Success criteria summary

| Criterion | Target |
|-----------|--------|
| Leaf actions in player semantics | Move forward, turn left, turn right only |
| Conditions | Path/sensor forward, left, right only |
| Henshin rules | Orders of magnitude **smaller** than naive cross-product; justify with count |
| Feasibility | Migration + engine/UI impact assessed |
| Honesty | If “few rules for everything” is impossible, say so with a proof sketch |

---

*Document version: 1.0 — agent briefing for alternative Blocky modeling and compact Henshin coverage.*
