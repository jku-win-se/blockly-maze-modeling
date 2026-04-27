# Why block-list edits need many Henshin rules (and what we do about it)

This note explains why covering **all** insertions, deletions, and substitutions over our **linked lists of `Block`** instances tends to produce a **large number** of Henshin rules, even when the *idea* of each edit is simple (Levenshtein-style distance on a sequence).

## How blocks are structured in the metamodel

In `blocky`, programs are not a single flat `EList` of blocks. They are several **separate containment-linked lists**, all using the same `Block.next` chain inside each list:

- **`Level.solution`** — head of the main program list.
- **`RepeatUntilGoal.body`** — head of the loop body list.
- **`IfStatement.thenBranch`** — head of the “then” list (shared by simple `If` and `IfElse`).
- **`IfElse.elseBranch`** — head of the “else” list (only on `IfElse`).

`Block.next` is **containment**. That matters for deletes and substitutes: you cannot simply “remove a node” without **rewiring** the predecessor (or head reference) so the successor is not deleted with the subtree by accident.

So one conceptual operation — “insert one statement” — has **different graph patterns** depending on *where* the list lives in the model.

## Why Henshin multiplies rules

### 1. Reference names are fixed in the rule graph

Henshin matches and rewrites **concrete graph patterns**. The **EReference** used in an edge (`solution`, `body`, `thenBranch`, `elseBranch`, `next`) is part of that pattern. There is no built-in way to say “insert at **any** list head reference” in a single rule; each distinct head needs its own pattern (or a separate sub-rule / variant).

### 2. Concrete types are fixed on create nodes

A `create` node must have a **concrete** `EClass`. You cannot write “create one of {MoveForward, Turn, If, …}” as a type variable. To offer several insertable block kinds, you typically need **one rule per concrete type** (or one rule per type after refactoring), then compose alternatives in a **unit** (`independent`, `priority`, etc.).

### 3. Head insert vs middle insert are different patterns

- **Before an existing head**: delete/create edges on the **owner → head** containment reference, plus `newBlock.next → oldHead`.
- **Between two blocks**: match `parent → curr : next`, then delete that edge and create `parent → newBlock` and `newBlock → curr`.

So “insert” splits into **at least** these families per location family.

### 4. Delete and substitute need safe containment rewiring

For containment chains, delete/substitute rules usually come in **pairs** (or equivalent branching):

- **Last in chain** (no `next`): drop the containment link from owner/parent.
- **Has successor**: **create** the reparent edge first, **delete** old containment edges touching the removed node, following the validator rules (no illegal preserve-edges between preserve- and delete-nodes).

That doubles patterns again for many locations.

### 5. Metamodel constraints add more variants

Examples from our domain:

- **`If` vs `IfElse`**: only `IfElse` has `elseBranch`. Rules that touch the else list must match an `IfElse` owner, not a plain `If`.
- **`Level.allowLoops` / `Level.allowConditionals`**: if we encode them as match constraints on `Level`, rules that create loops or conditionals need a **`Game → levels → Level`** path for nested edits, or we accept that some guards only apply where `Level` is in the LHS.

Each such constraint tends to add **another rule shape** or duplicate context, not “one extra line” in the same rule.

## Rough combinatorics (order of magnitude)

If we enumerate naively:

- **Locations** (main head, body head, then head, else head, between `next`) ≈ **5** location families for inserts (and similar for deletes at heads / delete after parent).
- **Operations** insert / delete / substitute ≈ **3** (if we model substitute explicitly).
- **Concrete block kinds** to insert or substitute to (MoveForward, Turn×2, If×3 conditions, RepeatUntilGoal, …) ≈ **O(10)** and grows with the language.

Even before pairing “last vs hasNext”, we are already in the hundreds of distinct **rule shapes**. Pairing and guards pushes toward **thousands of lines** if written out longhand in `.henshin_text`.

That is not because Henshin is “wrong”; it is because we are asking for a **fully explicit** transformation system over a **heterogeneous** graph (many list heads, containment, multiple concrete types).

## What we do in this project

1. **Treat the large rule set as generated data**  
   We keep a **small Python program** (e.g. `blocky_model/transformations/generate_levenshtein_neighbors.py`) that iterates over *where* and *what* and emits `.henshin_text`. The **source of truth** is the generator + metamodel, not a hand-maintained thousand-line file.

2. **Narrow the operator set when possible**  
   For example, if “substitute” can be modeled as **delete + insert** (two steps), we can omit explicit substitute rules and shrink the generated module.

3. **Use units for “where” at the call site**  
   Units such as “insert MoveForward anywhere” wrap `independent` calls over a **small** set of location-specific rules, so callers (and MOMoT) see one operator, even though the engine still has multiple underlying rules.

4. **Accept that some duplication is fundamental**  
   Until the metamodel exposes a **single** uniform list abstraction (or we introduce a **placeholder** concrete block type and a second phase to specialize it), Henshin will keep needing **distinct patterns** per reference and per concrete create type.

## Related reading

- [09-generating-henshin-text.md](09-generating-henshin-text.md) — codegen pitfalls (EEnum ints, delete edges, MoMoT-friendly rules).
- [02-rules-and-graphs.md](02-rules-and-graphs.md) — nodes, edges, action types.
- [03-units-and-control-flow.md](03-units-and-control-flow.md) — `independent`, `priority`, `for`, etc.

## Summary

The “huge number of rules” problem is the product of:

1. **Several list heads** + **`next` chains** + **containment**,  
2. **Fixed EReferences and EClasses** in Henshin rules,  
3. **Delete/substitute** safety splitting **last vs has successor**,  
4. **Language features** (`If` / `IfElse`, flags on `Level`).

We manage it by **generating** rules from a compact description and by **composing** location alternatives in units—not by pretending one or two hand-written rules can cover all edits.
