# Quick reference

One-page syntax cheat for `.henshin_text` files.

## File

- Extension: **`.henshin_text`**
- Start with: **ePackageImport** *packageName* (at least one)

## Model

```
ePackageImport <EString>+

rule <name> ( <params>? ) { <ruleElements>+ }
unit <name> ( <params>? ) { <unitElements>+ }
```

## Parameters

```
( IN | OUT | INOUT | VAR )? <name> : <type>
```

Types: **EString**, **EInt**, **EDouble**, **EBoolean**, **ELong**, … or EClass (e.g. `Bank`).

## Rule elements (order free, at most one each except graph)

| Keyword | Example |
|---------|--------|
| javaImport | `javaImport my.package` |
| checkDangling | `checkDangling false` |
| injectiveMatching | `injectiveMatching true` |
| conditions | `conditions [ expr1, expr2 ]` |
| graph | `graph { ... }` — **exactly one** |

## Graph: nodes

```
( preserve | create | delete | forbid | require )? node <name> : <EClass> ( { <attrs> } )?
```

Attributes: `name=value` (match) or `set name=value` (update). Default action: **preserve**.

## Graph: edges

```
edges [ ( (actionType)? source -> target : referenceName ), ... ]
```

## Formula

```
matchingFormula {
  formula <Logic>
  conditionGraph <id> { node ... ; edges [ ... ] }
  ...
}
```

Logic: **!** conditionGraphRef | conditionGraphRef **AND** conditionGraphRef | **OR** / **XOR** | **(** Logic **)**.

## MultiRule

```
multiRule <name> { graph { ... } }
```

Inside multiRule graph: define new nodes; in edges, reference parent graph nodes by name. Reuse only preserve/create/delete nodes.

## Unit elements

| Construct | Syntax |
|-----------|--------|
| Call | `ruleOrUnitName( arg1, arg2, ... )` |
| Block | `{ e1 e2 ... }` |
| strict / rollback | `strict true` / `rollback false` |
| independent | `independent [ list1, list2, ... ]` |
| conditional | `if ( e+ ) then { e+ } else { e+ }?` |
| priority | `priority [ list1, list2, ... ]` |
| for | `for ( numericExpr ) { e+ }` |
| while | `while { e+ }` |

## Expressions (main)

- **Logic (formula)**: conditionGraphRef, `!`, AND, OR, XOR, `( )`.
- **Conditions / attributes**: **OR**, **AND**, `==`, `!=`, `>=`, `<=`, `>`, `<`, `+`, `-`, `*`, `/`, `!`, `( )`, parameter, literal (STRING, number, true/false), Java call/attribute.
- Literals: **true** / **false**, **INT**, **NEGATIVE**, **DECIMAL**, **STRING**.

## Action types

**preserve** | **create** | **delete** | **forbid** | **require** (default: preserve).

## Common types

**EString**, **EInt**, **EDouble**, **EBoolean**, **ELong**, **EFloat**, **EShort**, **EByte**, **EDate**, **EJavaClass**, **EJavaObject**, **EEnumerator**.

---

See [01-file-structure-and-grammar](01-file-structure-and-grammar.md) through [07-examples-and-patterns](07-examples-and-patterns.md) for details, [06-validation-rules](06-validation-rules.md) for constraints, and [09-generating-henshin-text](09-generating-henshin-text.md) when **generating** henshin_text (ePackageImport, enums, delete rules, encoding).
