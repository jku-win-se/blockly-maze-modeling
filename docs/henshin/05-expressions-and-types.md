# Expressions and types

This document describes the **Expression** grammar, **Parameter** and **ParameterType**, and **parameter kinds**. Type consistency is enforced by the validator; see [06-validation-rules](06-validation-rules.md).

## Expression grammar (overview)

Expressions are used in: **conditions**, attribute **value** and **set** values, **match** values, **for** iterations, and formula/Java calls. Operator precedence (low to high):

| Level | Operators | Type requirement |
|-------|-----------|------------------|
| OR | `OR` | Boolean |
| AND | `AND` | Boolean |
| Equality | `==`, `!=` | Same type on both sides |
| Comparison | `>=`, `<=`, `>`, `<` | Same type; not boolean/complex |
| Add/Sub | `+`, `-` | Number |
| Mul/Div | `*`, `/` | Number |
| Unary | `!` | Boolean |
| Primary | `( Expression )`, atomic | — |

**Atomic** expressions:

- **Parameter reference**: A parameter name (same rule/unit). Type = parameter's type.
- **Java class call**: `EString ( ( Expression ( ',' Expression )* )? )` — e.g. `MyClass.staticMethod(arg1, arg2)`. Requires **javaImport** for the package. Argument count and types must match the Java method.
- **Java attribute**: `ID '.' ID` — e.g. `MyClass.CONST`. Requires javaImport; the field must exist.
- **String**: `STRING` (quoted).
- **Number**: `DECIMAL` (e.g. `3.14`, `-2.5`).
- **Integer**: `NEGATIVE` (e.g. `-42`) or `INT` (non-negative integer).
- **Boolean**: `true` or `false`.

**Rule:** AND/OR operands must be boolean. Equality and comparison require the same type on both sides; comparison does not allow boolean or complex types. Plus/minus/mul/div require number types. NOT requires boolean. Attribute and match values must match the Ecore EAttribute type. See [06-validation-rules](06-validation-rules.md).

## EEnum (Ecore enum) attribute values

When an attribute's type is an **EEnum** (e.g. `TurnDirection`, `SensorDirection`), two forms are possible:

1. **Integer literal (recommended for code generation):** Use the enum literal's numeric value from the Ecore model (first literal often 0, next 1, etc.). Example: `direction = 0`, `condition = 2`. No `javaImport` required; avoids "X doesn't exist" when the Java package is not on the classpath. See [09-generating-henshin-text](09-generating-henshin-text.md).
2. **JavaAttributeValue (EnumType.Literal):** Use `EnumType.Literal` (e.g. `TurnDirection.LEFT`). Parsed as Java attribute; requires **javaImport** for the package that contains the generated enum class. The validator resolves the class/field there; if the package is not visible, validation fails with "X doesn't exist."

## Parameter

**Syntax:**

```
( <kind> )? <name> : <parameterType>
```

- **kind**: Optional. One of: **IN**, **OUT**, **INOUT**, **VAR**. If omitted, a warning may be reported (deprecated).
- **name**: ID; unique in the rule/unit.
- **parameterType**: Either an **enum type** (e.g. EInt, EString) or an **EClass** (EString).

**Rule:** In a **call**, only non-VAR parameters of the callee are matched: argument count and types must match the callee's non-VAR parameters. VAR parameters are local to the callee. See [06-validation-rules](06-validation-rules.md).

## ParameterType

Two forms:

1. **Enum type** — Use a built-in type name. Common: **EString**, **EInt**, **EDouble**, **EBoolean**, **ELong**, **EFloat**, **EShort**, **EByte**, **EByteArray**, **EDate**, **EJavaClass**, **EJavaObject**, **EEnumerator**, etc. (See grammar enum Type for full list.)
2. **EClass** — Use an EClass from an imported EPackage (EString, e.g. `Bank`, `bank.Client`). Used for OUT/INOUT when passing graph elements.

**Valid:**

```
IN client:EString
IN accountId:EInt
IN amount:EDouble
VAR x:EDouble
OUT grid:Grid
```

## Parameter kinds

| Kind | Meaning |
|------|--------|
| IN | Input; passed in by caller. |
| OUT | Output; bound by the rule/unit and returned. |
| INOUT | Input and output. |
| VAR | Local variable; not part of the call signature. Call argument count ignores VAR. |

**Rule:** Specify a kind (IN, OUT, INOUT, VAR). Omitting kind is deprecated and may produce a warning.

## Types for expressions

- **number**: DECIMAL, NEGATIVE, INT, or parameter/attribute of numeric type (EInt, EDouble, etc.).
- **bool**: `true`/`false`, or parameter/attribute of type EBoolean, or comparison/equality expressions.
- **string**: STRING literal or EString parameter/attribute.
- **complex**: JavaClassValue, JavaAttributeValue, or EClass-typed parameter (e.g. OUT grid:Grid). Cannot be used in comparison operators.

Attribute and match values are checked against the Ecore EAttribute type. Java class calls are checked against the Java method's parameter and return types.

## Summary

- Use **Expression** for conditions, attribute values, iterations, and Java calls; respect type rules (boolean for AND/OR/NOT, number for +,-,*,/, same type for ==,!=,>=,<=,>,<).
- **Parameter**: optional **IN**|**OUT**|**INOUT**|**VAR**, name, and **ParameterType** (enum type or EClass).
- **Call** argument count and types match the callee's non-VAR parameters.
