## Summary (256 characters or fewer)

A fully customizable HUD for Minecraft 1.20.1, 1.21.1 and 26.1.2. Define every line in XML, JSON or TXT, edit it in game, and add optional tags from other mods.

## Description

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

## Features

- **Visual HUD editor.** Press `F9` (or run `/igi editor`) to add, remove and reorder lines, insert tags, switch alignment and adjust X / Y offsets. A **Panel** button moves the editor between the bottom and top edges.
- **Tag browser and debug inspector.** `/igi taglist` opens the searchable tag list. `F8` (or `/igi debug`) opens an inspector that shows every registered tag and its current value by category; clicking a tag copies `<tag>`.
- **Expressions.** `{{ ... }}` evaluates arithmetic, parentheses and functions such as `round`, `min`, `max`, `abs`, `floor`, `ceil` and `format`, so a line can contain a small calculation.
- **Mod integration.** Optional tags for Applied Energistics 2, Ars Nouveau, Botania (1.20.1), Cobblemon, Cold Sweat, Create, Curios, Goety, Iron's Spells 'n Spellbooks, Mekanism, Paragliders, Serene Seasons, Thirst Was Taken, Tinkers' Construct (1.20.1), Tough As Nails and Vampirism, plus built-in Forge / NeoForge energy tags for the held item.
- **1.12.2 compatibility.** The XML, JSON and TXT formats, the `{tag}` syntax, the nine alignments and the scale setting are retained; a 1.12.2 HUD file that only uses supported tags loads as-is. The config file is now a TOML file with an in-game config screen, and the HUD definition lives in `config/InGameInfo.xml`.
- **Nine languages.** English, Japanese, German, French, Norwegian, Polish, Russian, Simplified Chinese and Traditional Chinese.

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
