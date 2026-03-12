# Validation rules

This document lists the main **validator** constraints so agents can avoid errors and produce valid `.henshin_text` files. The authoritative implementation is in the project at `plugins/org.eclipse.emf.henshin.text/src/org/eclipse/emf/henshin/text/validation/Henshin_textValidator.xtend`.

## File and model

- **Extension**: File must have extension `.henshin_text`.
- **EPackage**: Use at least one `ePackageImport`; node types and edge types are resolved from imported EPackages.

## Rules

- **Graph count**: Exactly **one** `graph` per rule. No rule without a graph; no rule with multiple graphs.
- **Optional elements (at most one each per rule)**: `checkDangling`, `injectiveMatching`, `conditions`, `javaImport`. Duplicates cause errors (or warnings for duplicate javaImport).

## Graph: nodes

- **Node type**: `nodetype` must be an EClass from an imported EPackage. Otherwise: "Nodetype X is not imported."
- **Abstract create**: A node with action type **create** must not have an **abstract** EClass as type. Otherwise: "Node of abstract type cannot be created."
- **Attributes**: Each attribute name must be an EAttribute of the node's EClass (or a supertype). Otherwise: "X has no attribute 'y'."
- **Duplicate attribute**: The same attribute (same name and same update kind) must not appear twice on a node. Otherwise: "'x' can only be used once."
- **Set and match**: For every `set name=value` there must be a matching preserve (or default) attribute for *name* on the same node. Do not duplicate update (e.g. two set for the same attribute). Otherwise: "Preserve-attribute x needed." / "Duplicate update."
- **Node action type vs attributes**: In **create** nodes only create-attributes are allowed; no preserve/delete/forbid/require, no set. In **delete** nodes only delete-attributes; no set. In **forbid**/**require** nodes no set. Otherwise: "X-attributes are not allowed in Y-nodes." / "set-attributes are not allowed in Y-nodes."

## Graph: edges

- **Edge type**: The edge `type` must be an EReference of the source node's EClass. The target node's EClass must match the reference's type (or be a subtype). Otherwise: "Edgetype X does not exist." / "Edge X->Y:Z does not exist."
- **Edge vs node action types**: The combination of edge action type and source/target node action types must be allowed (e.g. preserve-edge only between preserve nodes; create-edge only between preserve/create nodes; forbid/require edges have specific constraints). Otherwise: "A X-edge is not allowed between a Y-node and a Z-node."
- **Preserve-edge and delete-node**: A **preserve** edge (default when no action is given) is **not** allowed between a preserve-node and a delete-node (or the reverse). Use **delete** or **create** for edges that touch a delete-node. Otherwise: "A preserve-edge is not allowed between a preserve-node and a delete-node."
- **Duplicate graph elements**: Each node name must be declared at most **once** per graph (one declaration with one action type). Do not declare the same node twice (e.g. both `node x` and `delete node x`). Otherwise: "Duplicate GraphElements 'x'."

## MultiRule and reuse

- **Reuse only in multiRule**: **Reuse** nodes (MultiRuleReuseNode) are only allowed inside a **multiRule** graph. In the main rule graph, do not use reuse. Otherwise: "Reuse-Nodes are only allowed in multiRules."
- **Reuse only preserve/create/delete**: Forbid and require nodes of the parent graph must not be reused in a multiRule. Otherwise: "X-Node 'Y' is not allowed in MultiRules."
- **No set in reuse**: Do not use `set` in a MultiRuleReuseNode. Otherwise: "Set-attributes are not allowed in MultiRuleReuseNodes."
- **Reuse attribute action**: Attributes in a reuse node must match the original node's action type (e.g. only create-attributes when reusing a create node).
- **Forbid/require not in edges**: Forbid and require nodes must not be used as source or target of edges inside a multiRule graph. Otherwise: "X-nodes are not allowed to be reused in edges in MultiRuleReuseNodes."
- **No duplicate reuse**: A parent node may be reused at most once in one multiRule graph. Otherwise: "X is already reused."
- **No self-reuse in same graph**: A graph cannot reuse its own nodes (reuse must refer to a node in a containing graph). Otherwise: "Graph cannot reuse its own nodes."
- **No name shadowing**: Node names in a multiRule graph must not duplicate node names in a containing graph. Otherwise: "Duplicate Node 'X'."

## Formula and conditionGraph

- **One matchingFormula per graph**: At most one `matchingFormula` in a graph (or in a conditionGraph). Otherwise: "matchingFormula can only use once."
- **ConditionGraph refs**: Every conditionGraph name used in the formula must be defined in the **same** matchingFormula block. Otherwise: "ConditionGraph 'X' does not exist."
- **No duplicate conditionGraph in formula**: Each conditionGraph may appear at most once in the formula tree. Otherwise: "Duplicate ConditionGraph 'X'."
- **Condition reuse LHS only**: ConditionReuseNode and condition edges may only reference nodes that are in the LHS (preserve or delete). Forbid/require nodes must not be referenced. Otherwise: "Node 'X' is not in LHS."
- **ConditionGraph node names**: ConditionGraph node names must not duplicate nodes in the containing graph or sibling conditionGraphs. ConditionGraph cannot reuse its own nodes. Otherwise: "Duplicate Node 'X'." / "ConditionGraph cannot reuse its own nodes."
- **Condition edge type**: Same as main graph — type must be EReference of source, target must match. Condition node types must be imported.

## Units and calls

- **Call parameter count**: The number of arguments in a call must equal the number of **non-VAR** parameters of the called rule/unit. Otherwise: "Bad Parameter Count."
- **Call parameter types**: The type of each argument must match the type of the corresponding (non-VAR) parameter. Otherwise: "Call expected X type, but was Y."
- **IteratedUnit iterations**: The expression in `for ( ... )` must be of **number** type and must not be negative. Otherwise: "IteratedUnit expected number type, but was X." / "Negative values are not allowed."
- **Strict / Rollback**: At most one `strict` and one `rollback` per unit and per each nested scope (sub-sequence, independent list, if/then/else branch, priority list, for body, while body). Otherwise: "Strict can only be used once." / "Rollback can only be used once."

## Expression and attribute types

- **AND/OR**: Both operands must be boolean. Otherwise: "Expression expected bool type, but was X."
- **NOT**: Operand must be boolean.
- **Equality/Comparison**: Left and right must have the same type; comparison does not allow boolean or complex types. Otherwise: "Expression expected the same type, but was X and Y." / "Value cannot be compared."
- **Plus/Minus/Mul/Div**: Operands must be number type. Otherwise: "Expression expected number type, but was X."
- **Attribute value**: The expression assigned to an attribute (or set) must match the Ecore EAttribute type. Otherwise: "Attribute expected X type, but was Y."
- **Match value**: Same for conditionGraph Match. Otherwise: "Attribute expected X type, but was Y."

## Java

- **javaImport**: The package in `javaImport` must exist in the project. Otherwise: "Package X doesn't exist."
- **Java attribute**: When using JavaAttributeValue (e.g. `MyClass.CONST`), the class and field must exist in an imported package. Otherwise: "X doesn't exist."
- **Java class call**: When using JavaClassValue (e.g. `MyClass.method(args)`), the method must exist and parameter count/types must match. Otherwise: "X doesn't exist." / "Bad Parameter Count." / "Methode expected X type, but was Y."

## Parameter kind

- **Specify kind**: Prefer **IN**, **OUT**, **INOUT**, or **VAR**. Omitting kind may produce a warning: "Parameter X should have a parameter kind of IN, INOUT, OUT or VAR. Specifying no parameter kind is deprecated."

## Summary

Ensure: one graph per rule; node/edge types from imported EPackages; valid edge types and action-type compatibility; no preserve-edges between preserve- and delete-nodes; no duplicate graph elements (each node declared once); no duplicate or invalid attributes; set only with matching preserve; multiRule reuse only for preserve/create/delete and without set; formula and conditionGraph refs and LHS reuse; call parameter count and types; non-negative number for for-iterations; at most one strict/rollback per scope; expression and attribute types as above; valid Java imports and calls.

**For agents and code generators:** See [09-generating-henshin-text](09-generating-henshin-text.md) for a concise guide to generating valid henshin_text (ePackageImport, EEnum attributes, delete rules, edges, encoding).
