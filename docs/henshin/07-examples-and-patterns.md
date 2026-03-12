# Examples and patterns

This document gives minimal valid snippets and patterns and points to example files in the repository.

## Minimal valid file

```
ePackageImport bank

rule minimalRule() {
	graph {
		node n:Bank
		edges []
	}
}
```

## Rule: create node and edges

```
ePackageImport bank

rule createAccount(IN client:EString, IN accountId:EInt) {
	graph {
		node bankNode:Bank
		node clientNode:Client { name=client }
		node managerNode:Manager
		create node accountNode:Account { id=accountId }
		edges [
			(bankNode->clientNode:clients),
			(create bankNode->accountNode:accounts),
			(create clientNode->accountNode:accounts)
		]
	}
}
```

## Rule: forbid node (NAC)

```
forbid node forbidAccountNode:Account { id=accountId }
...
edges [ (forbid bankNode->forbidAccountNode:accounts) ]
```

## Rule: set attribute (preserve + set)

```
node xAccountNode:Account {
	id=fromId
	credit=x
	set credit=x-amount
}
```

## Rule: conditions

```
conditions [ x>amount, amount>0 ]
```

## Unit: if / then / else

```
unit newClient(client:EString, accountId:EInt) {
	if (clientNotExist(client)) then {
		createNewClient(client)
		createAccount(client, accountId)
	} else {
		createAccount(client, accountId)
	}
}
```

## Unit: independent

```
unit winInLottery(...) {
	independent [
		win(client1, accountId1, amount1),
		lose(),
		win(client2, accountId2, amount2)
	]
}
```

## Unit: for loop

```
unit buildGrid(IN width:EInt, IN height:EInt, OUT grid:Grid) {
	initGrid(grid)
	for (height - 2) {
		extendFirstColumn()
	}
	for (width - 1) {
		startNextColumn()
		for (height - 1) {
			extendNextColumn()
		}
	}
}
```

## Unit: while loop

```
unit sort(x:EString, y:EString, i:EInt, j:EInt) {
	while {
		swap(x, y, i, j)
	}
}
```

## matchingFormula with conditionGraph

```
matchingFormula {
	formula !accountGraph
	conditionGraph accountGraph {
		node ForbidNode:bank.Account { id=accountId }
		edges [(BankNode->ForbidNode:accounts)]
	}
}
```

With two condition graphs:

```
matchingFormula {
	formula !graph1 AND !graph2
	conditionGraph graph1 {
		node forbidNode:Node
		edges [(root->forbidNode:nodes), (forbidNode->unnamed:ver)]
	}
	conditionGraph graph2 {
		node forbidNode:Node
		edges [(root->forbidNode:nodes), (unnamed->forbidNode:hor), (root->unnamed:nodes)]
	}
}
```

## MultiRule with reuse

Parent graph declares nodes; multiRule graph declares new nodes and edges that reference parent nodes by name:

```
rule deleteAllAccounts(client:EString) {
	checkDangling false
	graph {
		node clientNode:Client { name=client }
		node bankNode:Bank
		node managerNode:Manager
		edges [(bankNode->managerNode:managers), (bankNode->clientNode:clients), (managerNode->clientNode:clients)]
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

## Repository example files

| Path | Description |
|------|-------------|
| `plugins/org.eclipse.emf.henshin.text.transformation.tests/testCases/bank/bank.henshin_text` | Rules (create, conditions, set, multiRule), full bank example. |
| `plugins/org.eclipse.emf.henshin.text.examples/Units/units.henshin_text` | Units: if/then/else, independent; rules used by units. |
| `plugins/org.eclipse.emf.henshin.text.examples/MultiRule/multiRule.henshin_text` | MultiRule with delete and reuse of parent nodes. |
| `plugins/org.eclipse.emf.henshin.text.examples/Formula/conditionGraph.henshin_text` | matchingFormula and conditionGraph (!accountGraph). |
| `plugins/org.eclipse.emf.henshin.text.examples/Formula/forbid_require.henshin_text` | Forbid and require nodes and edges. |
| `plugins/org.eclipse.emf.henshin.text.examples/SetAttribut/setAttribut.henshin_text` | set attribute (name=newName) and conditions. |
| `plugins/org.eclipse.emf.henshin.text.examples/sort/sort.henshin_text` | while unit and conditions (i<j) AND (x>y). |
| `plugins/org.eclipse.emf.henshin.text.transformation.tests/testCases/grid-full/grid-full.henshin_text` | for loops, matchingFormula with two conditionGraphs, OUT parameter. |
| `plugins/org.eclipse.emf.henshin.text.transformation.tests/testCases/grid-full_nestedUnits/grid-full.henshin_text` | Nested for loops. |
| `plugins/org.eclipse.emf.henshin.text.transformation.tests/testCases/movies/movies.henshin_text` | multiRule with require nodes, create rules, unit with for. |

Use these as references for valid structure and patterns when generating or editing `.henshin_text` files.
