# Input XMI models

Place your Blocky Game XMI file here and set its path in **blocky.momot** (search.model.file).

- Default path used in the config: **game.xmi**
- You can copy an XMI from **blocky_game** (e.g. **save.xmi** or **load.xmi**) and rename it to **game.xmi**, or use any Game-root XMI that contains at least one Level with a valid map (START and GOAL cells).

The model must have **Game** as root and at least one **Level** with a **GridMap** and cells. The level’s **solution** may be null (search will build it) or an existing program (search will extend it within the defined rules).
