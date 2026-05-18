# Why list edits need many Henshin rules (and what we do about it)

This note explains why covering **all** insertions, deletions, and substitutions over our **container-linked lists** tends to produce a **large number** of Henshin rules, even when the *idea* of each edit is simple (Levenshtein-style distance on a sequence).

## How programs are structured in the current metamodel

In `blocky`, programs are not a single flat `EList`. They are represented as **containment-linked lists of containers**:

- **`Level.solution : Body`** — owns the top-level program body.
- **`Loop.body : Body`** — owns a loop body.
- **`IfStmt.thenBody : Body`** and optional **`IfStmt.elseBody : Body`** — own conditional bodies.

Each `Body` owns a chain:

- **`Body.firstContainer : Container`** (containment) — head
- **`Container.next : Container`** (containment) — rest of the chain
- **`Container.statement : Statement`** (containment) — payload (`AtomicStatement`, `Loop`, `IfStmt`, ...)

`Container.next` is **containment**. That matters for deletes and substitutes: you cannot simply “remove a container” without **rewiring** the predecessor (or head reference) so the successor container is not deleted with the subtree by accident.

So one conceptual operation — “insert one statement” — often becomes two conceptual operations at the model level:

- **Insert a container** at some list position (empty/head/middle/tail)
- **Populate the container** with a concrete statement kind

## Why Henshin multiplies rules

### 1. Reference names are fixed in the rule graph

Henshin matches and rewrites **concrete graph patterns**. The **EReference** used in an edge (`firstContainer`, `next`, `statement`, and nested body references like `Loop.body`, `IfStmt.thenBody`, `IfStmt.elseBody`) is part of that pattern. There is no built-in way to say “insert at **any** list head reference” in a single rule; each distinct head/edge shape needs its own pattern (or a separate sub-rule / variant).

In the current model, the list-head/reference set is smaller and more uniform (`Body.firstContainer` / `Container.next`), but you still have distinct patterns for:

- `Body.firstContainer` head insert/delete
- `Container.next` middle/tail insert/delete

### 2. Concrete types are fixed on create nodes

A `create` node must have a **concrete** `EClass`. You cannot write “create one of {AtomicStatement(kind=...), IfStmt, Loop, …}” as a type variable. To offer several insertable statement kinds, you typically need **one rule per concrete type** (or per family), then compose alternatives in a **unit** (`independent`, `priority`, etc.).

### 3. Head insert vs middle insert are different patterns

- **Before an existing head**: delete/create edges on `Body.firstContainer`, plus `newContainer.next → oldHead`.
- **Between two containers**: match `prev → curr : next`, then delete that edge and create `prev → newContainer` and `newContainer → curr`.

So “insert” splits into **at least** these families per location family.

### 4. Delete and substitute need safe containment rewiring

For containment chains, delete/substitute rules usually come in **pairs** (or equivalent branching):

- **Last in chain** (no `next`): drop the containment link from owner/parent.
- **Has successor**: **create** the reparent edge first, **delete** old containment edges touching the removed container, following the validator rules (no illegal preserve-edges between preserve- and delete-nodes).

That doubles patterns again for many locations.

### 5. Domain constraints add more variants

Examples from our domain (depending on how we encode them in rule LHS/conditions):

- **`IfStmt.elseBody` optional**: if we want operators that distinguish “if-only” vs “if-else”, we need variants that require/forbid the else body.
- **`Level.allowLoops` / `Level.allowConditionals`**:
  - If we encode them as match constraints on `Level`, rules that create loops or conditionals may need `Game → levels → Level` context for nested edits, or we accept that some guards only apply where `Level` is in the LHS.
  - Alternative: keep operators generic and enforce the flags in MoMoT via a **hard penalty objective** (e.g. `AllowControlFlowPenalty`) that assigns a large penalty when the synthesized program contains disallowed `Loop` / `IfStmt` nodes.

Each such constraint tends to add **another rule shape** or duplicate context, not “one extra line” in the same rule.

## Rough combinatorics (order of magnitude)

If we enumerate naively (for the container + payload split):

- **Container locations** (empty body, head, between `next`, after last) ≈ **4** insert families (and similar for deletes).
- **Payload kinds** (AtomicStatement, IfStmt, Loop, …) ≈ **O(3+)** (and grows with the language).
- **Enum variants** (AtomicStatementKind, ConditionKind) add multiplicative factors if encoded as separate rules instead of parameters.

Even before pairing “last vs hasNext”, we are already in the hundreds of distinct **rule shapes**. Pairing and guards pushes toward **thousands of lines** if written out longhand in `.henshin_text`.

That is not because Henshin is “wrong”; it is because we are asking for a **fully explicit** transformation system over a **heterogeneous** graph (many list heads, containment, multiple concrete types).

## What we do in this project

1. **Treat the large rule set as generated data (when needed)**  
   Even with the container-based metamodel, scaling to “all edits anywhere” still produces a lot of rules. When we need exhaustive operator sets, we generate them from the metamodel rather than maintaining them by hand.

2. **Narrow the operator set when possible**  
   For example, if “substitute” can be modeled as **delete + insert** (two steps), we can omit explicit substitute rules and shrink the generated module.

3. **Use units for “where” at the call site**  
   Units such as “insert a container anywhere” wrap `independent` calls over a **small** set of location-specific rules, so callers (and MOMoT) see one operator, even though the engine still has multiple underlying rules.

4. **Accept that some duplication is fundamental**  
   Until the metamodel exposes a **single** uniform list abstraction (or we introduce a **placeholder** concrete block type and a second phase to specialize it), Henshin will keep needing **distinct patterns** per reference and per concrete create type.

## Related reading

- [09-generating-henshin-text.md](09-generating-henshin-text.md) — codegen pitfalls (EEnum ints, delete edges, MoMoT-friendly rules).
- [02-rules-and-graphs.md](02-rules-and-graphs.md) — nodes, edges, action types.
- [03-units-and-control-flow.md](03-units-and-control-flow.md) — `independent`, `priority`, `for`, etc.

## Summary

The “huge number of rules” problem is the product of:

1. **Several bodies** + **container `next` chains** + **containment**,  
2. **Fixed EReferences and EClasses** in Henshin rules,  
3. **Delete/substitute** safety splitting **last vs has successor**,  
4. **Language features** (`IfStmt` else-body optionality, flags on `Level`).

We manage it by **generating** rules from a compact description and by **composing** location alternatives in units—not by pretending one or two hand-written rules can cover all edits.
