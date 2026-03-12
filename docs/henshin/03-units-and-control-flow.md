# Units and control flow

This document describes **units**: how to call rules and units, sequential blocks, and control-flow constructs (independent, conditional, priority, iterated, loop). It also covers **strict** and **rollback**.

## Unit structure

A **unit** has a name, optional parameters, and a body of **UnitElements**:

```
unit <name> ( <parameters>? ) {
  <unitElements>+
}
```

Unit elements can be:

- **Call** — invoke a rule or another unit
- **Sequential block** — `{ unitElement+ }`
- **SequentialProperties** — `strict` or `rollback` (at most one of each per scope)
- **IndependentUnit** — `independent [ list, list, ... ]`
- **ConditionalUnit** — `if ( ... ) then { ... } else { ... }?`
- **PriorityUnit** — `priority [ list, list, ... ]`
- **IteratedUnit** — `for ( expression ) { ... }`
- **LoopUnit** — `while { ... }`

## Calls

**Syntax:**

```
<ruleOrUnitName> ( <arguments>? )
```

Arguments are passed by position. The callee’s parameters are matched in order; **VAR** parameters of the callee are not counted for the call (they are local). The number and types of non-VAR parameters must match. See [05-expressions-and-types](05-expressions-and-types.md) and [06-validation-rules](06-validation-rules.md).

**Valid:**

```
createAccount(client, accountId)
transferMoney(client, fromId, toId, amount, x, y)
initGrid(grid)
extendFirstColumn()
```

## Sequential block

Group unit elements into a sequence:

```
{ unitElement1 unitElement2 ... }
```

Use this to structure then/else branches or sub-sequences inside control-flow units.

## Strict and Rollback

- **strict** *true* | *false* — Sequential unit strictness.
- **rollback** *true* | *false* — Whether to rollback on failure.

**Rule:** At most one `strict` and one `rollback` per unit and per each nested scope (sub-sequence, independent list, if/then/else branch, priority list, iterated body, loop body). See [06-validation-rules](06-validation-rules.md).

## Independent unit

Execute several **lists** in parallel (independent branches); order between lists is unspecified.

**Syntax:**

```
independent [ list1, list2, ... ]
```

Each *list* is a sequence of unit elements (no extra brackets in the grammar; a list is one or more consecutive unit elements before the next comma). In practice, use blocks to group: `independent [ { a b }, { c d } ]`.

**Valid:**

```
independent [ win(client1, accountId1, amount1), lose(), win(client2, accountId2, amount2) ]
```

## Conditional unit

**Syntax:**

```
if ( ifElements+ ) then { thenElements+ } ( else { elseElements+ } )?
```

- **if**: One or more unit elements (often a single rule/unit call) used as condition; if any match is found, the condition holds.
- **then**: One or more unit elements executed when the condition holds.
- **else**: Optional; executed when the condition does not hold.

**Valid:**

```
if (clientNotExist(client)) then {
  createNewClient(client)
  createAccount(client, accountId)
} else {
  createAccount(client, accountId)
}
```

## Priority unit

**Syntax:**

```
priority [ list1, list2, ... ]
```

Lists are tried in order; the first list that can be applied is executed. Same list structure as independent (comma-separated sequences).

## Iterated unit

**Syntax:**

```
for ( iterations ) { unitElements+ }
```

- **iterations**: An Expression that must evaluate to a **number** type. **Rule:** Non-negative; negative values are invalid. See [05-expressions-and-types](05-expressions-and-types.md) and [06-validation-rules](06-validation-rules.md).

**Valid:**

```
for (height - 2) {
  extendFirstColumn()
}
for (n) {
  someRule()
}
```

## Loop unit

**Syntax:**

```
while { unitElements+ }
```

Repeatedly execute the body until no rule/unit in the body is applicable.

**Valid:**

```
while {
  swap(x, y, i, j)
}
```

## Summary

- **Calls**: `ruleOrUnit( arg1, arg2, ... )`; match parameter count (excluding VAR) and types.
- **Blocks**: `{ e1 e2 ... }` for sequencing.
- **strict** / **rollback**: At most once per scope (unit and each nested scope).
- **independent** `[ ... ]**: Parallel lists.
- **if** ( ... ) **then** { ... } **else** { ... }?: Conditional.
- **priority** `[ ... ]`: Ordered choice.
- **for** ( *numericExpr* ) { ... }: Fixed iteration (non-negative number).
- **while** { ... }: Loop until no match.
