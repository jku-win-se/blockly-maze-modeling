# Blocky Henshin transformations (canonical location)

This folder follows the **Henshin bank example** layout: the **metamodel** (`model/blocky.ecore`) and the **transformation** (`.henshin_text` / `.henshin`) live in the **same project** (`blocky_model`). The Henshin Text editor can resolve `ePackageImport blocky` because the Ecore is in the same project.

## Files

| File | Description |
|------|-------------|
| **add_block_to_empty_slot.henshin_text** | Generated: add any block to any empty slot (solution, next, body, thenBranch, elseBranch) and delete any block. Regenerate with the Python script (see below). |
| **add_block_to_empty_slot_henshin_text.henshin** | Compiled XMI module; MoMoT and the Henshin interpreter use this. Generate from the .henshin_text (Transform to Henshin), then fix Ecore refs (see below). |
| **generate_add_block_rules.py** | Script that generates **add_block_to_empty_slot.henshin_text** and can fix the compiled .henshin so types resolve at runtime. |

## Generate and use the add-block rules

1. **Regenerate .henshin_text**  
   From the repo root or this folder:
   ```bash
   python generate_add_block_rules.py add_block_to_empty_slot.henshin_text
   ```
   Writes UTF-8 without BOM so the Henshin compiler accepts it.

2. **Compile to .henshin**  
   In Eclipse: open **add_block_to_empty_slot.henshin_text** in the **Henshin Text** editor → Right‑click → **Transform to Henshin**. This creates/updates **add_block_to_empty_slot_henshin_text.henshin**.

3. **Fix the compiled .henshin for runtime**  
   The compiler writes type hrefs like `../model/blocky.ecore#/...`. The Henshin interpreter often cannot resolve that path, leading to **"Missing factory for 'Node newBlock:null'"**. Replace those with the package nsURI:
   - Either edit the .henshin: replace `../model/blocky.ecore#` with `http://www.example.org/blocky#` (use your package’s nsURI from the .ecore).
   - Or run the script with a second argument (path to the compiled .henshin); it will rewrite that file:
     ```bash
     python generate_add_block_rules.py add_block_to_empty_slot.henshin_text add_block_to_empty_slot_henshin_text.henshin
     ```
   Run step 3 after every recompile from .henshin_text.

## MoMoT

The MoMoT configuration in **blocky_momot** references this module as:
`../blocky_model/transformations/add_block_to_empty_slot_henshin_text.henshin`.

## References

- [Henshin bank example](https://wiki.eclipse.org/Henshin/Getting_started)
- [docs/henshin/README.md](../../docs/henshin/README.md) — Henshin docs index
- [docs/henshin/09-generating-henshin-text.md](../../docs/henshin/09-generating-henshin-text.md) — How to generate valid .henshin_text and MoMoT-friendly rules (nsURI, no IN params, root context for empty solution)
