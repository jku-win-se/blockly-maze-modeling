# Reference

Compact reference for .momot grammar and allowed names. Use with [02-syntax-and-structure](02-syntax-and-structure.md)–[04-experiment-analysis-results](04-experiment-analysis-results.md) for full details.

## Grammar summary (BNF-like)

```
MOMoTSearch :
    ("package" QualifiedName)?
    importSection?
    VariableDeclaration*
    ("initialization" "=" XBlockExpression)?
    "search" ValidID? "=" SearchOrchestration
    "experiment" "=" ExperimentOrchestration
    ("analysis" "=" AnalysisOrchestration)?
    ("results" "=" ResultManagement)?
    ("finalization" "=" XBlockExpression)?

SearchOrchestration : "{" 
    "model" "=" InputModel
    "solutionLength" "=" XExpression
    "transformations" "=" ModuleOrchestration
    "fitness" "=" FitnessFunctionSpecification
    "algorithms" "=" AlgorithmList
    ("equalityHelper" "=" EqualityHelper)?
"}"

InputModel : "{" "file" "=" XExpression ("adapt" "=" XBlockExpression)? "}"

ModuleOrchestration : "{" 
    "modules" "=" ArrayLiteral
    ("ignoreUnits" "=" ArrayLiteral)?
    ("ignoreParameters" "=" ArrayLiteral)?
    ("parameterValues" "=" "{" ParmeterValueSpecification* "}")?
"}"

FitnessFunctionSpecification : (XConstructorCall)? "{" 
    ("preprocess" "=" XBlockExpression)?
    "objectives" "=" "{" FitnessDimensionSpecification+ "}"
    ("constraints" "=" "{" FitnessDimensionSpecification+ "}")?
    ("postprocess" "=" XBlockExpression)?
    ("solutionRepairer" "=" XConstructorCall)?
"}"

FitnessDimensionSpecification : 
    ValidID ":" ("minimize"|"maximize") XConstructorCall   // FitnessDimensionConstructor
  | ValidID ":" ("minimize"|"maximize") XBlockExpression    // FitnessDimensionXBase
  | ValidID ":" ("minimize"|"maximize") StringLiteral ("{" DefExpression* "}")?  // FitnessDimensionOCL

AlgorithmList : "{" (ValidID ":" XExpression)+ "}"

ExperimentOrchestration : "{" 
    "populationSize" "=" XExpression
    "maxEvaluations" "=" XExpression
    "nrRuns" "=" XNumberLiteral
    ("referenceSet" "=" XExpression)?
    ("progressListeners" "=" "[" XConstructorCall ("," XConstructorCall)* "]")?
    ("collectors" "=" "[" CollectorArray ("," XConstructorCall)* "]")?
"}"

AnalysisOrchestration : "{" 
    "indicators" "=" IndicatorArray
    "significance" "=" XNumberLiteral
    "show" "=" ShowArray
    ("grouping" "=" "{" (ValidID "=" "[" AlgorithmReferences "]")+ "}")?
    ("outputFile" "=" StringLiteral)?
    ("boxplotDirectory" "=" StringLiteral)?
    ("printOutput")?
"}"

ResultManagement : "{" 
    ("adaptModels" "=" XBlockExpression)?
    (ObjectivesCommand | SolutionsCommand | ModelsCommand)+
"}"
```

ArrayLiteral = `"[" XExpression ("," XExpression)* "]"`.  
AlgorithmReferences = `"[" [AlgorithmSpecification ("," AlgorithmSpecification)*] "]"` (algorithm names from search.algorithms).

---

## Indicators (analysis.indicators)

| Name |
|------|
| hypervolume |
| generationalDistance |
| invertedGenerationalDistance |
| spacing |
| additiveEpsilonIndicator |
| contribution |
| R1 |
| R2 |
| R3 |
| maximumParetoFrontError |

Use in a list: `[ hypervolume, invertedGenerationalDistance ]`.

---

## Collectors (experiment.collectors)

| Name |
|------|
| hypervolume |
| generationalDistance |
| invertedGenerationalDistance |
| spacing |
| additiveEpsilonIndicator |
| contribution |
| R1 |
| R2 |
| R3 |
| adaptiveMultimethodVariation |
| adaptiveTimeContinuation |
| approximationSet |
| epsilonProgress |
| elapsedTime |
| populationSize |

Plus custom constructor calls.

---

## Show (analysis.show)

| Name |
|------|
| individualValues |
| aggregateValues |
| statisticalSignificance |

Use in a list: `[ aggregateValues, statisticalSignificance, individualValues ]`.

---

## Common imports (Java/Xbase types)

| Purpose | Import (use ^ for keyword segments) |
|---------|-------------------------------------|
| Solution length objective | `at.ac.tuwien.big.momot.^search.^fitness.dimension.TransformationLengthDimension` |
| Solution repairer | `at.ac.tuwien.big.momot.^search.solution.repair.TransformationPlaceholderRepairer` |
| Placeholder mutation | `at.ac.tuwien.big.momot.^search.algorithm.operator.mutation.TransformationPlaceholderMutation` |
| Parameter mutation | `at.ac.tuwien.big.momot.^search.algorithm.operator.mutation.TransformationParameterMutation` |
| Progress listener | `at.ac.tuwien.big.moea.^experiment.executor.listener.SeedRuntimePrintListener` |
| Tournament selection | `org.moeaframework.core.operator.TournamentSelection` |
| One-point crossover | `org.moeaframework.core.operator.OnePointCrossover` |
| Random integer parameter | `at.ac.tuwien.big.momot.problem.unit.parameter.random.RandomIntegerValue` |
| Random double parameter | `at.ac.tuwien.big.momot.problem.unit.parameter.random.RandomDoubleValue` |
| Ecore package | (project-specific, e.g. `my.project.metamodel.MyPackage`) |

Algorithm creation uses the **moea** factory provided by the orchestration (no import for `moea` itself): e.g. `moea.createNSGAII(...)`, `moea.createNSGAIII(...)`, `moea.createEpsilonMOEA()`, `moea.createRandomSearch()`.

---

## Result commands (results block)

| Command    | Optional keys |
|-----------|----------------|
| objectives | algorithms, neighborhoodSize or maxNeighborhoodSize, outputFile, printOutput |
| solutions  | algorithms, neighborhoodSize or maxNeighborhoodSize, outputFile, outputDirectory, printOutput |
| models     | algorithms, neighborhoodSize or maxNeighborhoodSize, outputDirectory, printOutput |

---

## Keywords (do not use as identifiers without ^ in imports)

In imports, escape with **^** if a segment is a keyword: **search**, **fitness**, **experiment**, and other Xtext/Xbase keywords. Example: `at.ac.tuwien.big.momot.^search.^fitness.dimension.TransformationLengthDimension`.
