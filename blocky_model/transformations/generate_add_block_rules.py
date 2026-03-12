#!/usr/bin/env python3
"""
Generate add_block_to_empty_slot.henshin_text: rules to add any block to any empty slot
and to delete any block from anywhere.
Run: python generate_add_block_rules.py [henshin_text_path] [henshin_path]
- No args: print .henshin_text to stdout.
- One arg: write .henshin_text to that path (UTF-8, no BOM).
- Two args: write .henshin_text to the first path, then rewrite the compiled .henshin at the
  second path so Ecore refs use the package nsURI (fixes "Missing factory for Node newBlock:null"
  when the Henshin compiler wrote href="../model/blocky.ecore#/...").
"""
BLOCKY_NSURI = "http://www.example.org/blocky"
ECORE_PATH_PREFIX = "../model/blocky.ecore#"

# (ref_name, container_var, container_type, ref, label)
SLOTS = [
    ("solution", "level", "blocky.Level", "solution", "Solution"),  # first block when level has empty solution
    ("next", "parent", "blocky.Block", "next", "Next"),
    ("body", "loop", "blocky.RepeatUntilGoal", "body", "Body"),
    ("thenBranch", "ifStmt", "blocky.IfStatement", "thenBranch", "ThenBranch"),
    ("elseBranch", "ifStmt", "blocky.IfStatement", "elseBranch", "ElseBranch"),
]

# (name_suffix, eclass, attr_line, extra_create, params, condition)
# Enum attributes: use integer literal (EEnum literal value from blocky.ecore) to avoid Java/EEnumLiteral resolution errors.
# TurnDirection: LEFT=0, RIGHT=1. SensorDirection: AHEAD=0, LEFT=1, RIGHT=2.
# No IN params/conditions: MoMoT does not set rule parameters, so we generate simple rules only.
BLOCKS = [
    ("MoveForward", "blocky.MoveForward", "", "", "", ""),
    ("TurnLeft", "blocky.Turn", "direction = 0", "", "", ""),   # LEFT
    ("TurnRight", "blocky.Turn", "direction = 1", "", "", ""),   # RIGHT
    ("IfAhead", "blocky.IfStatement", "condition = 0", "then", "", ""),   # AHEAD
    ("IfLeft", "blocky.IfStatement", "condition = 1", "then", "", ""),    # LEFT
    ("IfRight", "blocky.IfStatement", "condition = 2", "then", "", ""),   # RIGHT
    ("RepeatUntilGoal", "blocky.RepeatUntilGoal", "", "body", "", ""),
]

# Delete: (ref_name, container_var, container_type, ref, label, need_level_context)
DELETE_SLOTS = [
    ("solution", "level", "blocky.Level", "solution", "Solution", True),
    ("next", "parent", "blocky.Block", "next", "Next", False),
    ("thenBranch", "ifStmt", "blocky.IfStatement", "thenBranch", "ThenBranch", False),
    ("elseBranch", "ifStmt", "blocky.IfStatement", "elseBranch", "ElseBranch", False),
    ("body", "loop", "blocky.RepeatUntilGoal", "body", "Body", False),
]


def add_rule(slot, block):
    ref_name, container_var, container_type, ref, label = slot
    name_suffix, eclass, attr_line, extra_create, params, condition = block
    rule_name = f"Add{name_suffix}To{label}"
    param_part = f"({params})" if params else "()"
    cond_line = f"\tconditions [{condition}]\n" if condition else ""
    attr_part = f" {{ {attr_line} }}" if attr_line else ""
    # Solution slot: Level must be found via Game so the matcher sees it when the graph root is Game.
    need_game_level_context = ref_name == "solution"
    if need_game_level_context:
        lines = [
            f"rule {rule_name} {param_part}{{",
            cond_line,
            "\tgraph {",
            "\t\tnode game : blocky.Game",
            f"\t\tnode {container_var} : {container_type}",
            "\t\tedges [(game->level:levels)]",
            "\t\tforbid node block : blocky.Block",
            f"\t\tedges [(forbid {container_var}->block:{ref})]",
            f"\t\tcreate node newBlock : {eclass}{attr_part}",
            f"\t\tedges [(create {container_var}->newBlock:{ref})]",
        ]
    else:
        lines = [
            f"rule {rule_name} {param_part}{{",
            cond_line,
            "\tgraph {",
            f"\t\tnode {container_var} : {container_type}",
            "\t\tforbid node block : blocky.Block",
            f"\t\tedges [(forbid {container_var}->block:{ref})]",
            f"\t\tcreate node newBlock : {eclass}{attr_part}",
            f"\t\tedges [(create {container_var}->newBlock:{ref})]",
        ]
    if extra_create == "then":
        lines.extend([
            "\t\tcreate node thenBlock : blocky.MoveForward",
            "\t\tedges [(create newBlock->thenBlock:thenBranch)]",
        ])
    elif extra_create == "body":
        lines.extend([
            "\t\tcreate node bodyBlock : blocky.MoveForward",
            "\t\tedges [(create newBlock->bodyBlock:body)]",
        ])
    lines.extend(["\t}", "}", ""])
    return "\n".join(l for l in lines if l)


def delete_rule_when_last(slot):
    ref_name, container_var, container_type, ref, label, need_level = slot
    rule_name = f"DeleteBlockFrom{label}WhenLast"
    # Declare block once with delete action. No preserve-edges touching delete-node (validator disallows).
    if need_level:
        return f"""rule {rule_name} () {{
	graph {{
		node game : blocky.Game
		node level : blocky.Level
		delete node block : blocky.Block
		forbid node nextBlock : blocky.Block
		edges [(game->level:levels), (forbid block->nextBlock:next), (delete level->block:{ref})]
	}}
}}
"""
    return f"""rule {rule_name} () {{
	graph {{
		node {container_var} : {container_type}
		delete node block : blocky.Block
		forbid node nextBlock : blocky.Block
		edges [(forbid block->nextBlock:next), (delete {container_var}->block:{ref})]
	}}
}}
"""


def delete_rule_when_has_next(slot):
    ref_name, container_var, container_type, ref, label, need_level = slot
    rule_name = f"DeleteBlockFrom{label}WhenHasNext"
    if need_level:
        return f"""rule {rule_name} () {{
	graph {{
		node game : blocky.Game
		node level : blocky.Level
		delete node block : blocky.Block
		node rest : blocky.Block
		edges [(game->level:levels), (create level->rest:{ref}), (delete level->block:{ref}), (delete block->rest:next)]
	}}
}}
"""
    return f"""rule {rule_name} () {{
	graph {{
		node {container_var} : {container_type}
		delete node block : blocky.Block
		node rest : blocky.Block
		edges [(create {container_var}->rest:{ref}), (delete {container_var}->block:{ref}), (delete block->rest:next)]
	}}
}}
"""


def main():
    out = []
    out.append("ePackageImport blocky")
    out.append("")
    out.append("// =============================================================================")
    out.append("// Add any fittable block to any empty slot (next, body, thenBranch, elseBranch)")
    out.append("// =============================================================================")
    out.append("")
    for slot in SLOTS:
        for block in BLOCKS:
            out.append(add_rule(slot, block))
    out.append("// =============================================================================")
    out.append("// Delete any block from anywhere (reparent successor when block has next)")
    out.append("// =============================================================================")
    out.append("")
    for slot in DELETE_SLOTS:
        out.append(delete_rule_when_last(slot))
        out.append(delete_rule_when_has_next(slot))
    return "\n".join(out)


def fix_henshin_ecore_refs(henshin_path: str) -> None:
    """Replace Ecore file hrefs with package nsURI so the interpreter can resolve types at runtime."""
    import sys
    with open(henshin_path, "r", encoding="utf-8") as f:
        content = f.read()
    if ECORE_PATH_PREFIX not in content:
        return
    content = content.replace(ECORE_PATH_PREFIX, BLOCKY_NSURI + "#")
    with open(henshin_path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Fixed Ecore refs in {henshin_path}", file=sys.stderr)


if __name__ == "__main__":
    import sys
    out = main()
    if len(sys.argv) > 1:
        with open(sys.argv[1], "w", encoding="utf-8") as f:
            f.write(out)
        if len(sys.argv) > 2:
            import os
            if os.path.isfile(sys.argv[2]):
                fix_henshin_ecore_refs(sys.argv[2])
            else:
                print(f"Optional .henshin file not found: {sys.argv[2]}", file=sys.stderr)
    else:
        print(out)
