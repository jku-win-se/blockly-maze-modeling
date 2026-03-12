# File structure and grammar

This document describes the top-level structure of a `.henshin_text` file and the syntax for rules and units.

## Model

A henshin_text file is a **Model** with two parts, in order:

1. **ePackageImport+** — At least one EPackage import.
2. **transformationsystem** — Zero or more model elements (rules and units).

There is no explicit "model" keyword; the file content is the model.

## EPackage import

Use `ePackageImport` to reference an EPackage from your Ecore metamodel. All EClass and EReference names used in the file must be resolvable from one of these packages.

**Syntax:**

```
ePackageImport <EString>
```

**EString** is a qualified name: one or more identifiers separated by dots (e.g. `bank`, `ecore`, `bank.meta`). It must resolve to an `ecore::EPackage` in the project.

**Valid:**

```
ePackageImport bank
ePackageImport ecore
ePackageImport my.package.name
```

**Invalid:** Do **not** use a quoted URI or string (e.g. `ePackageImport blocky "platform:/resource/..."`). The grammar expects only a qualified name; a quoted string causes a parse error (e.g. "missing EOF"). See [09-generating-henshin-text](09-generating-henshin-text.md).

**Rule:** Use at least one `ePackageImport` so that node types and edge types can be validated.

**If the editor reports "Couldn't resolve reference to EPackage 'blocky'":** The package is not registered in the running Eclipse. See [README](README.md#troubleshooting-couldnt-resolve-reference-to-epackage-blocky) for how to fix it (file location, build, blocky_henshin_fragment or run configuration).

## Model elements: rule vs unit

Each **ModelElement** is either a **rule** or a **unit**.

### Rule

```
rule <name> ( <parameters>? ) {
  <ruleElements>+
}
```

- **name**: ID (single identifier).
- **parameters**: Optional comma-separated list of parameters. See [05-expressions-and-types](05-expressions-and-types.md).
- **ruleElements**: One or more of: javaImport, checkDangling, injectiveMatching, conditions, graph. **Exactly one** must be a `graph`; the rest are optional and at most once each. See [02-rules-and-graphs](02-rules-and-graphs.md).

### Unit

```
unit <name> ( <parameters>? ) {
  <unitElements>+
}
```

- **name**: ID.
- **parameters**: Optional comma-separated list.
- **unitElements**: One or more calls, sequential blocks, or control-flow units. See [03-units-and-control-flow](03-units-and-control-flow.md).

## EString (qualified names)

Where the grammar expects **EString**, use a qualified name: `ID ( '.' ID )*`. Examples:

- `bank` — single segment
- `bank.Bank` — package-qualified class (when needed for disambiguation)
- `ecore.EClassifier` — for Ecore types

Node types and reference types in graphs use EString to refer to `ecore::EClass` and `ecore::EReference`; they must be imported via `ePackageImport`.

## Order and repetition

- **ePackageImport**: One or more at the top; order does not matter for validation.
- **Rules and units**: Any order; names must be unique (same name used for call targets).
- **Parameters**: Order matters for calls: the caller passes arguments by position (excluding VAR parameters of the callee).

## Minimal valid file

Smallest valid file has at least one import and can have no rules/units:

```
ePackageImport bank
```

A minimal file with one rule:

```
ePackageImport bank

rule myRule() {
	graph {
		node n:Bank
		edges []
	}
}
```

See [02-rules-and-graphs](02-rules-and-graphs.md) for graph syntax and [07-examples-and-patterns](07-examples-and-patterns.md) for more examples.
