# Rules and graphs

This document describes rule structure, rule elements, and the transformation graph (nodes, edges, action types, attributes).

## Rule structure

A **rule** has a name, optional parameters, and a body of **RuleElements**:

```
rule <name> ( <parameters>? ) {
  <ruleElements>+
}
```

**RuleElements** (each at most once per rule, except graph which is exactly once):

| Element | Syntax | Purpose |
|--------|--------|---------|
| javaImport | `javaImport` *packagename* | Import Java package for expressions (e.g. JavaClassValue). |
| checkDangling | `checkDangling` true \| false | Enable/disable dangling edge check. |
| injectiveMatching | `injectiveMatching` true \| false | Require injective matching. |
| conditions | `conditions` `[` *Expression* (`,` *Expression*)* `]` | Attribute conditions (all must hold). |
| graph | `graph` `{` *graphElements* `}` | The transformation graph (required exactly once). |

See [05-expressions-and-types](05-expressions-and-types.md) for Expression and [06-validation-rules](06-validation-rules.md) for "exactly one graph" and "at most one of each optional element".

## Graph and graph elements

The **graph** block contains zero or more of:

- **Edges** — list of edges
- **Node** — a graph node
- **Formula** — matchingFormula (condition graphs)
- **MultiRule** — amalgamation sub-rule
- **MultiRuleReuseNode** — only inside a multiRule; reuses a node from the parent graph

See [04-formulas-and-multirules](04-formulas-and-multirules.md) for Formula and MultiRule.

## Action types

Nodes and edges can be marked with an **ActionType**. Default when omitted is **preserve**.

| ActionType | Meaning (nodes) | Meaning (edges) |
|------------|------------------|------------------|
| preserve | Match existing (LHS) | Edge must exist (LHS) |
| create | Create new (RHS) | Create new edge (RHS) |
| delete | Delete matched (RHS) | Delete edge (RHS) |
| forbid | NAC: must not exist | NAC: edge must not exist |
| require | PAC: must exist | PAC: edge must exist |

**Rule:** Action types on edges must be consistent with the action types of source and target nodes (see [06-validation-rules](06-validation-rules.md)).

## Nodes

**Syntax:**

```
( <actiontype>? ) node <name> : <nodetype> ( { <attribute>* } )?
```

- **actiontype**: Optional. One of: preserve, create, delete, forbid, require. Default: preserve.
- **name**: ID; unique in the rule graph (and in nested multiRule graphs; no shadowing).
- **nodetype**: EClass from an imported EPackage (EString, e.g. `Bank`, `bank.Client`).
- **attribute**: Zero or more attribute assignments or set-clauses.

**Valid:**

```
node bankNode:Bank
create node accountNode:Account { id=accountId }
forbid node forbidAccountNode:Account { id=accountId }
node clientNode:Client { name=client }
node xAccountNode:Account { id=fromId, credit=x, set credit=x-amount }
```

**Rule:** Create-nodes cannot have abstract EClass type. Forbid/require nodes cannot be reused in multiRule edges.

## Node attributes

Inside `{ ... }` on a node you can use:

1. **Match attribute** (preserve/forbid/require): `name=value` or `(actiontype)? name=value`. Binds or constrains the attribute to *value* (an Expression).
2. **Set attribute** (preserve only): `set name=value`. Updates the attribute to *value* after match.

**Rule:** For every `set name=...` there must be a matching preserve (or default) attribute for *name* in the same node. Duplicate attribute (same name and same update kind) is invalid.

**Rule:** In create-nodes only create-attributes are allowed; no set, no forbid/require. In delete-nodes only delete-attributes. In forbid/require nodes no set. See [06-validation-rules](06-validation-rules.md).

## Edges

**Syntax:**

```
edges [
  ( (actiontype)? sourceNode -> targetNode : referenceName ),
  ...
]
```

- **actiontype**: Optional; default preserve.
- **sourceNode**, **targetNode**: Node names (or, inside multiRule, reuse nodes). Must be defined in the same graph (or parent graph for reuse).
- **referenceName**: EReference of the source node’s EClass (EString). The reference’s type must match the target node’s EClass (or a supertype).

**Valid:**

```
edges [
  (bankNode->clientNode:clients),
  (create bankNode->accountNode:accounts),
  (forbid bankNode->forbidAccountNode:accounts)
]
```

**Rule:** The reference must exist on the source EClass and the target EClass must match the reference’s type. Edge action type must be compatible with source and target node action types.

## Conditions

Use **conditions** to add attribute conditions (all must hold):

```
conditions [ x>amount, amount>0 ]
```

Each element is an Expression (boolean). See [05-expressions-and-types](05-expressions-and-types.md). At most one `conditions` element per rule (and per multiRule).

## javaImport, checkDangling, injectiveMatching

- **javaImport** *packagename*: Required if you use Java class/attribute values in expressions; the package must exist in the project.
- **checkDangling** true|false: Use for example `checkDangling false` when deleting nodes and allowing dangling edges.
- **injectiveMatching** true|false: Constrains matching to be injective.

Each of these can appear at most once per rule. See [06-validation-rules](06-validation-rules.md).

## Summary

- Every rule has exactly one **graph**; optionally one each of javaImport, checkDangling, injectiveMatching, conditions.
- Graph contains **nodes** (with optional action type and attributes) and **edges** (source->target:reference), plus optionally **matchingFormula** and **multiRule**.
- Use **preserve** (default), **create**, **delete**, **forbid**, **require** consistently on nodes and edges; validator enforces compatibility.
- Node attributes: match with `name=value`, update with `set name=value`; types must match the Ecore EAttribute.
