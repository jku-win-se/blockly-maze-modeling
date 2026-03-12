# Formulas and multirules

This document covers **matchingFormula** (with conditionGraph and Logic) and **MultiRule** (amalgamation with reuse nodes).

## matchingFormula

A **matchingFormula** constrains how the rule's LHS is matched by combining conditions over **conditionGraphs** with logical operators.

**Syntax:**

```
matchingFormula {
  formula <Logic>
  conditionGraph <name1> { ... }
  conditionGraph <name2> { ... }
  ...
}
```

- **formula**: A Logic expression referencing conditionGraph names (see below).
- **conditionGraphs**: One or more conditionGraph definitions. Every name used in *formula* must be defined here. **Rule:** At most one matchingFormula per graph. Each conditionGraph name may appear at most once in the formula tree (no duplicate refs). See [06-validation-rules](06-validation-rules.md).

## Logic (formula)

**Logic** is built from:

- **ConditionGraphRef**: Reference to a conditionGraph by name (the atomic case).
- **NOT**: `!` *Primary*
- **AND**: *Primary* `AND` *Primary* (chained)
- **OR** / **XOR**: *AND* `OR` *AND* or *AND* `XOR` *AND* (chained)
- **Brackets**: `(` Logic `)`

**Valid:**

```
formula !accountGraph
formula !graph1 AND !graph2
formula ( graphA OR graphB ) AND !graphC
```

**Rule:** Every conditionGraph name used in the formula must be defined in the same matchingFormula block. Do not reference a conditionGraph from a different (e.g. parent) formula.

## conditionGraph

A **conditionGraph** describes a graph pattern used in the formula. It can contain:

- **ConditionNode** — new node in the condition
- **ConditionReuseNode** — reuse a node from the main rule graph or from this conditionGraph
- **ConditionEdges** — edges between condition nodes/reused nodes
- **Formula** — nested matchingFormula (conditionGraphs can be nested)

**Syntax of conditionGraph:**

```
conditionGraph <name> {
  <conditionGraphElements>*
}
```

**Rule:** Node names in a conditionGraph must not duplicate names of nodes in the containing graph or in sibling conditionGraphs (no shadowing). A conditionGraph cannot reuse its own nodes (reuse name must refer to a node defined outside this conditionGraph).

## ConditionNode

**Syntax:**

```
node <name> : <type> ( { <match>* } )?
```

- **name**: ID; unique within the conditionGraph.
- **type**: EClass (EString) from an imported EPackage.
- **match**: Attribute constraints: `name=value` (Match). No `set`; conditionGraphs only constrain matches.

**Valid:**

```
conditionGraph accountGraph {
  node forbidNode:Node
  edges [(root->forbidNode:nodes), (forbidNode->unnamed:ver)]
}
conditionGraph accountGraph {
  node ForbidNode:bank.Account { id=accountId }
  edges [(BankNode->ForbidNode:accounts)]
}
```

## ConditionReuseNode

**Syntax:**

```
reuse <nodeRef> ( { <match>* } )?
```

**nodeRef** is a node from the **main rule graph** or from the same conditionGraph's parent scope. **Rule:** Only nodes that are in the LHS (preserve or delete) may be reused. Forbid and require nodes of the main graph cannot be reused in conditionGraph. See [06-validation-rules](06-validation-rules.md).

## ConditionEdge

**Syntax:**

```
edges [ ( source -> target : referenceName ), ... ]
```

Source and target are ConditionNode or ConditionReuseNode (or main graph Node when reusing). The reference must exist on the source type and the target type must match. Condition edges do not have an action type; they describe structure that must (or must not, depending on formula) exist.

## MultiRule

A **multiRule** is an amalgamation: a sub-rule that can match multiple times, with a graph that **reuses** nodes from the parent rule graph.

**Syntax:**

```
multiRule <name> {
  <multiruleElements>+
}
```

Multirule elements are the same as rule elements (javaImport, checkDangling, injectiveMatching, conditions, graph). Typically the multiRule has one **graph** containing new nodes and edges that reference **parent graph nodes** by name (for endpoints). Reuse of parent nodes is done via **MultiRuleReuseNode** or by referencing parent node names in edges, depending on tooling/scoping.

**Rule:** Reuse nodes (MultiRuleReuseNode) are only allowed **inside** a multiRule graph. The main rule graph must not contain reuse nodes. See [06-validation-rules](06-validation-rules.md).

## MultiRuleReuseNode

**Syntax:**

```
reuse <nodeName> ( { <attribute>* } )?
```

- **nodeName**: A node defined in the **parent** (containing) rule graph.
- **attribute**: Optional attribute constraints (match or create/delete as per the original node's action type).

**Rule:** Only **preserve**, **create**, or **delete** nodes of the parent graph may be reused. **Forbid** and **require** nodes must not be reused in multiRule. A given parent node may be reused at most once in one multiRule graph. Do not use **set** in a reuse node. Node names in the multiRule graph must not duplicate parent graph node names. See [06-validation-rules](06-validation-rules.md).

**Valid (multiRule with inner nodes and edges to parent nodes):**

```
rule deleteAllAccounts(client:EString) {
  graph {
    node clientNode:Client { name=client }
    node bankNode:Bank
    node managerNode:Manager
    edges [ (bankNode->managerNode:managers), (bankNode->clientNode:clients), (managerNode->clientNode:clients) ]
    multiRule deleteAccount {
      graph {
        delete node accountNode:Account
        edges [
          (delete bankNode->accountNode:accounts),
          (delete accountNode->clientNode:owner)
        ]
      }
    }
  }
}
```

The multiRule graph defines **accountNode** (delete) and edges that connect it to **bankNode** and **clientNode**; those names refer to nodes in the parent graph. Use explicit `reuse parentNodeName` when the grammar/scoping requires it to reference a parent node.

## Summary

- **matchingFormula**: One per graph; formula references conditionGraphs defined in the same block; no duplicate conditionGraph ref in formula.
- **conditionGraph**: Nodes and edges; reuse only LHS nodes; no name shadowing.
- **MultiRule**: Nested graph; reuse only preserve/create/delete parent nodes; at most one reuse per parent node; no set in reuse nodes.
