# CurseForge listing copy

This file contains the English text for the CurseForge project page. A Japanese translation is in [CURSEFORGE_ja.md](CURSEFORGE_ja.md). The upload notes at the end are for the upload work and are not part of the description.

## Summary (Summary field, 256 characters or fewer)

A fully customizable HUD for Minecraft 1.20.1, 1.21.1 and 26.1.2. Define every line in XML, JSON or TXT, edit it in game, and add optional tags from other mods.

## Description (Description field)

# InGameInfoXML (Reloaded)

**A fully customizable, data-driven HUD for Minecraft 1.20.1, 1.21.1 and 26.1.2.**

InGameInfoXML adds an information overlay that you design yourself. Write the lines you want in XML, JSON or TXT. Each line can mix plain text, color codes, live game values, item icons, potion effects, armor durability and values supplied by other mods.

The overlay supports nine screen alignments and a scale from 0.5x to 2x, and the layout can be edited in game:

```xml
<config>
  <lines at="topleft">
    <line><str>HP: {health}/{maxhealth}</str></line>
    <line><str>Biome: {biome}</str></line>
    <line><str>Position: {{ round(x, 1) }}, {{ round(z, 1) }}</str></line>
  </lines>
</config>
```

## About this project

This is a fork of **InGameInfoXML** by Lunatrius. The original mod targeted Minecraft 1.12.2 under the MIT license.

The code was initially written with AI-assisted vibe coding and then reviewed and debugged by a human.

## Features

- **Visual HUD editor.** Press `F9` (or run `/igi editor`) to add, remove and reorder lines, insert tags, switch alignment and adjust X / Y offsets. A **Panel** button moves the editor between the bottom and top edges.
- **Tag browser and debug inspector.** `/igi taglist` opens the searchable tag list. `F8` (or `/igi debug`) opens an inspector that shows every registered tag and its current value by category; clicking a tag copies `<tag>`.
- **Expressions.** `{{ ... }}` evaluates arithmetic, parentheses and functions such as `round`, `min`, `max`, `abs`, `floor`, `ceil` and `format`, so a line can contain a small calculation.
- **1.12.2 compatibility.** The XML, JSON and TXT formats, the `{tag}` syntax, the nine alignments and the scale setting are retained; a 1.12.2 HUD file that only uses supported tags loads as-is. The config file is now a TOML file with an in-game config screen, and the HUD definition lives in `config/InGameInfo.xml`.
- **Nine languages.** English, Japanese, German, French, Norwegian, Polish, Russian, Simplified Chinese and Traditional Chinese.

## What you can display

Player values (health, hunger, armor, experience, effects and more), world values (biome, dimension, coordinates, time, weather, moon phase, difficulty, seed), inventory and equipment (items, durability, counts, icons), potion effects, performance values (FPS, MSPT, TPS) and values from other mods. The full tag list, including aliases kept for 1.12.2 compatibility, is available in game through `/igi taglist` and in the documentation on GitHub.

## Mod integration

Optional tags are provided for Applied Energistics 2, Ars Nouveau, Botania (1.20.1), Cobblemon, Cold Sweat, Create, Curios, Goety, Iron's Spells 'n Spellbooks, Mekanism, Paragliders, Serene Seasons, Thirst Was Taken, Tinkers' Construct (1.20.1), Tough As Nails and Vampirism, plus built-in Forge / NeoForge energy tags for the held item. The tag-by-tag details are in the compatibility documentation on GitHub.

## Commands

`/igi` opens the editor, inspector and tag list and can reload, load and save HUD definitions, show or hide the overlay, change alignment offsets and adjust client settings: `/igi editor`, `/igi debug`, `/igi taglist`, `/igi reload`, `/igi load <file>`, `/igi save <file>`, `/igi enable`, `/igi disable`, `/igi alignment ...`, `/igi config ...`.

## Gallery

The Gallery tab shows the HUD, the in-game editor with its movable panel, the tag browser, the config screen, expressions and optional mod integrations.

## Supported versions

| Minecraft | Loader | Java |
|---|---|---|
| 1.20.1 | Forge 47.4.28 | 17 |
| 1.21.1 | NeoForge 21.1.264 | 21 |
| 26.1.2 | NeoForge 26.1.2.122 | 25 |

The mod is client-side only; install it on clients.

## License

This project is licensed under the MIT License.

## Credits

- Original **InGameInfoXML** for Minecraft 1.12.2: **Lunatrius**
- Earlier **InGameInfo** work: **DaftPVF**, **bspkrs**
- 1.20.1 Forge fork used as a starting point: **DF11ZomGraves**
- 1.20.1 / 1.21.1 / 26.1.2 port and maintenance: **code-onigiri**

## Links

- Source and issue tracker: https://github.com/code-onigiri/InGameInfo-reloaded
- Documentation: https://github.com/code-onigiri/InGameInfo-reloaded/tree/master/docs
- License: https://github.com/code-onigiri/InGameInfo-reloaded/blob/master/LICENSE

InGameInfoXML is not affiliated with Mojang Studios, Microsoft, Forge, NeoForge or the original author. Minecraft is a trademark of Mojang Studios.

---

## Upload notes (not part of the description)

- The CurseForge **Summary** field should use the short text above. Keep it to one sentence; the moderation policy asks for a summary that is not a copy of the description.
- The project **avatar must be 400x400 pixels**. Use [docs/curseforge/avatar-400.png](curseforge/avatar-400.png).
- Upload the jars as separate files for 1.20.1 Forge, 1.21.1 NeoForge and 26.1.2 NeoForge. Do not put version numbers in the project name; use the file names and game-version tags instead.
- Add a changelog to each file. The moderation policy requires file updates to describe what changed.
- Gallery images are ready in [docs/curseforge/](curseforge/). Upload them in the order and with the English captions in [docs/curseforge/README.md](curseforge/README.md): HUD, editor, tag list, config, expressions, Botania and AE2. The description refers to the Gallery instead of hotlinking external images.
- Keep the English text first. If a Japanese description is added, put it after the English section.
- The policy allows project links and social links at the bottom of the description, but not external file downloads, personal donation links at the top, or shortened URLs. The Links section above follows that rule.
- State the license as MIT on the CurseForge project page and link the root LICENSE file.
