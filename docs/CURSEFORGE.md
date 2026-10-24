# CurseForge listing copy

This file contains the text for the CurseForge project page. The description is in English so it can be used as the primary listing text; the Japanese notes at the end are for the upload work and are not part of the description.

## Summary (Summary field, 256 characters or fewer)

A fully customizable HUD for Minecraft 1.20.1, 1.21.1 and 26.1.2. Define every line in XML, JSON or TXT, edit it in game, and add optional tags from other mods.

## Description (Description field)

# InGameInfoXML (Reloaded)

**A fully customizable, data-driven HUD for Minecraft 1.20.1, 1.21.1 and 26.1.2.**

InGameInfoXML adds an information overlay that you design yourself. Instead of a fixed
list of coordinates or a single style, you write the lines you want in a small XML, JSON
or TXT definition. Each line can mix plain text, color codes, live game values, item
icons, potion effects, armor durability and values supplied by other mods.

The overlay can sit in any of nine screen alignments, at a scale from 0.5x to 2x. It is
rendered client-side, adds no blocks or items to the world, and uses no hard dependencies
on the mods whose data it can read.

## About this project

This is an unofficial community port of **InGameInfoXML** by Lunatrius. The original mod
targeted Minecraft 1.12.2 and is licensed under the MIT license. This project keeps the
original file format, tag names and HUD behavior wherever the modern game still provides
the same information, and ports the result to Forge 1.20.1, NeoForge 1.21.1 and NeoForge
26.1.2.

It is a fork, not a copy of the original project page. The original description was not
reused. The sections below describe what this port adds and what differs from 1.12.2.

## What is different from the 1.12.2 original

- **Modern versions.** Separate builds for Forge 1.20.1, NeoForge 1.21.1 and NeoForge
  26.1.2. Use the jar that matches your Minecraft version.
- **Same HUD definitions.** The XML, JSON and TXT formats, the `{tag}` syntax, the nine
  alignments and the scale setting are retained. Most 1.12.2 HUD files can be loaded
  without editing.
- **Visual HUD editor.** Press `F9` (or run `/igi editor`) to add, remove and reorder
  lines, insert tags, switch alignment and adjust X / Y offsets without editing XML by
  hand. A **Panel** button moves the editor between the bottom and top edges so either
  side of the overlay stays visible.
- **Tag browser and debug inspector.** `/igi taglist` opens the searchable tag list.
  `F8` (or `/igi debug`) opens an inspector that shows every registered tag and its
  current value by category; clicking a tag copies `<tag>` for pasting into a HUD line.
- **Expressions.** The new `{{ ... }}` syntax evaluates arithmetic, parentheses and
  functions such as `round`, `min`, `max`, `abs`, `floor`, `ceil` and
  `format` inside a line.
- **Modern mod integrations.** Optional tags for Create, Curios, Applied Energistics 2,
  Mekanism, Botania, Tinkers' Construct, Cobblemon, Paragliders and several other mods.
  Tags are registered only when the matching mod is installed.
- **Modern configuration.** A TOML client config and an in-game config screen replace the
  old config file. The HUD definition still lives in `config/InGameInfo.xml`.
- **Nine languages.** English, Japanese, German, French, Norwegian, Polish, Russian,
  Simplified Chinese and Traditional Chinese.

## What you can display

The built-in tags cover, among other things:

- **Player:** health, max health, absorption, hunger, saturation, armor, air, experience,
  level, game mode, score, team, effects and attributes.
- **World:** biome, dimension, coordinates, facing direction, light level, time, day,
  weather, moon phase, difficulty, seed-related values and server information where the
  client can obtain it.
- **Inventory and equipment:** main-hand and off-hand items, armor slots, durability,
  item counts, item names, icons and hovered-item information.
- **Potion effects:** name, amplifier, duration and icons for the active effects.
- **Performance:** FPS, MSPT and TPS where the client exposes them.
- **Other mods:** energy, AE2 networks and cells, Create kinetics, Mekanism radiation,
  Botania mana, Cobblemon party data, Tinkers' tool stats, Paragliders stamina and more.

The full tag list, including aliases kept for 1.12.2 compatibility, is available in game
through `/igi taglist` and in the project documentation.

## A HUD definition

Lines are grouped under an alignment in `config/InGameInfo.xml`:

```xml
<config>
  <lines at="topleft">
    <line><str>HP: {health}/{maxhealth}</str></line>
    <line><str>Biome: {biome}</str></line>
    <line><str>Position: {{ round(x, 1) }}, {{ round(z, 1) }}</str></line>
  </lines>
</config>
```

`{tag}` is replaced with the tag value. Color codes `$0` to `$f` and the reset code
`$r` are supported. The same definition can be saved as XML, JSON or TXT with
`/igi save <file>`; the extension selects the format.

## Expressions

The `{{ ... }}` form lets a line contain a small calculation:

```xml
<str>Damage: {{ attackdamage * 2 + 1 }}</str>
<str>Health: {{ format("%.1f", health / maxhealth * 100) }}%</str>
```

- Operators: `+`, `-`, `*`, `/`, `%`, parentheses and unary `+` / `-`
- Functions: `round(value[, digits])`, `min(...)`, `max(...)`, `abs(value)`,
  `floor(value)`, `ceil(value)`, `format("java format", value, ...)`
- `format` uses the same argument order as Java `String.format`
- Whole numbers print without a decimal part; other numbers are rounded to at most three
  decimals and trailing zeros are removed
- If a tag is unknown, the syntax is invalid or the format fails, the original
  `{{ ... }}` text is displayed unchanged

This syntax is an addition in the port. A 1.12.2 client does not evaluate it and shows the
braces literally, so remove expression lines if the same file is shared with 1.12.2.

## HUD editor and inspector

- **F9** or `/igi editor`: opens the visual editor. Select a line to edit its text,
  insert tags with **Tag...**, change the 3x3 alignment, adjust X / Y offsets and save.
  **Panel** flips the editor between the bottom and top of the screen.
- **F8** or `/igi debug`: opens the tag inspector. Tags are grouped by category and show
  their current value; click a name to copy `<tag>`.
- **Config** in the Mods screen: changes the HUD file name, scale, vanilla HUD visibility,
  chat/player-list behavior, potion and item icon overlays, decimal places and other
  settings.

The editor writes the HUD definition to `config/InGameInfo.xml` and alignment offsets to
`config/ingameinfoxml-client.toml`.

## Gallery

The Gallery tab shows the HUD, the in-game editor with its movable panel, the tag browser,
the config screen, expressions and optional mod integrations. The project avatar is the
same artwork at 400x400.

## Commands

| Command | What it does |
|---|---|
| `/igi reload` | Reload the current HUD definition |
| `/igi load <file>` | Load another XML, JSON or TXT definition |
| `/igi save <file>` | Save the current definition using the extension as the format |
| `/igi enable` / `/igi disable` | Temporarily show or hide the overlay |
| `/igi taglist` | Open the searchable tag list |
| `/igi editor` | Open the HUD editor |
| `/igi debug` | Open the tag inspector |
| `/igi alignment get <ALIGN>` | Print the X / Y offset of an alignment |
| `/igi alignment set <ALIGN> [x y]` | Change an alignment offset |
| `/igi config <key> <value>` | Change a supported client setting |

## Supported versions

| Minecraft | Loader | Java | Notes |
|---|---|---|---|
| 1.20.1 | Forge 47.4.28 | 17 | HUD, GUI and render tests verified |
| 1.21.1 | NeoForge 21.1.264 | 21 | HUD, GUI and render tests verified |
| 26.1.2 | NeoForge 26.1.2.122 | 25 | HUD, GUI and render tests verified; some optional mods have no 26.1.2 build |

Use the jar for the exact Minecraft version. The 1.20.1 jar will not load on 1.21.1 or
26.1.2, and the NeoForge jars will not load on Forge.

## Installation

1. Install the matching Forge or NeoForge version for your Minecraft version.
2. Put the matching `ingameinfoxml-*.jar` in the `mods` folder.
3. Start the game once. The mod creates `config/InGameInfo.xml` and
   `config/ingameinfoxml-client.toml`.
4. Press `F9` to open the editor, or edit `config/InGameInfo.xml` and run
   `/igi reload`.

No other mod is required. The optional integrations work only when their mod is present.

## Mod integration

Tags from other mods are registered only when that mod is installed. The mod does not
declare any of them as a required dependency, so a missing mod leaves the rest of the HUD
working.

| Mod | Data exposed as tags | Versions | Verification |
|---|---|---|---|
| Applied Energistics 2 | Network channels and mode, grid power, average usage, cell capacity and status | All | 1.20.1 network values and 1.21.1 HUD checked; 26.1.2 build |
| Mekanism | Personal and environmental radiation, severity, enabled state, percentage | All | 1.21.1 HUD checked; 1.20.1 and 26.1.2 build |
| Botania | Mana in the targeted pool, spreader or flower, including maximum and percentage | 1.20.1 | HUD checked with 500000/1000000, 400/1000 and 100/300 |
| Tinkers' Construct | Tool durability, mining speed, attack damage, harvest level, modifiers, broken state | 1.20.1 | HUD checked; values are minimal on a tool created with `/give` |
| Cobblemon | Party size and per-slot species, nickname, level, HP, status, shiny and type | All | 1.21.1 HUD checked; 1.20.1 and 26.1.2 build |
| Paragliders | Stamina, maximum stamina, percentage and exhausted state | All | 1.20.1 and 1.21.1 HUD checked; 26.1.2 build |
| Create | Kinetic speed, stress and contraption values for the targeted block or mounted contraption | All | Build checked |
| Curios | Curio slot information | All | Build checked |
| Serene Seasons | Season and related world values | All | Build checked |
| Ars Nouveau | Mana and spell-related values | All | Build checked |
| Iron's Spells 'n Spellbooks | Mana and spell-related values | All | Build checked |
| Tough As Nails | Temperature and thirst values | All | Build checked |
| Vampirism | Vampire-level and related values | All | Build checked |
| Cold Sweat | Temperature and body values | All | Build checked |
| Thirst Was Taken | Thirst values | All | Build checked |
| Goety | Soul energy and related values | All | Build checked |
| Forge / NeoForge Energy | Energy, maximum energy and percentage from the held item | All | Built in; no external mod needed |

"Build checked" means the integration compiles and registers its tags for that version,
but every value has not been verified in a live game. Botania and Tinkers' Construct are
1.20.1-only because their 1.21.1 / 26.1.2 APIs or builds are not available in the same
form.

AE2 network values are server-authoritative. The client requests them once per second
and the server replies with the network the player is looking at, or the network of a
linked wireless terminal in the inventory. If neither exists, the network tag reports
`none` and the power tags report zero.

## Compatibility with 1.12.2 HUD files

- The file structure, `{tag}` syntax, alignment names and color codes are retained.
- Tag names that still have a modern equivalent keep their old names. Common alternatives
  are registered as aliases.
- Tags that depended on removed concepts such as numeric item IDs, item metadata or
  client-side world size return a compatibility value or a registry ID instead of
  disappearing. The project documentation lists each case.
- The config file is now TOML and is managed by the config screen. HUD definitions remain
  XML, JSON or TXT.
- `{{ ... }}` expressions are new and are not understood by 1.12.2.

A 1.12.2 HUD file that only uses supported tags should load as-is. Back up the file before
switching versions.

## Languages

The interface and tag descriptions are available in:

- English (`en_us`)
- Japanese (`ja_jp`)
- German (`de_de`)
- French (`fr_fr`)
- Norwegian (`no_no`)
- Polish (`pl_pl`)
- Russian (`ru_ru`)
- Simplified Chinese (`zh_cn`)
- Traditional Chinese (`zh_tw`)

## Known limitations

- Optional mod values are only as complete as the checks listed above. Create, Curios,
  Serene Seasons, Ars Nouveau, Iron's Spells, Tough As Nails, Vampirism, Cold Sweat,
  Thirst Was Taken and Goety are build-verified, not value-verified in every version.
- Tinkers' Construct values were checked with a tool made by `/give`, which has no
  material data, so the displayed numbers are the minimum values. A material-built tool
  may show different numbers.
- 26.1.2 is a newer target. Some third-party mods do not publish a 26.1.2 build yet, so
  their integrations cannot be tested there.
- The overlay is a client-side display. It does not add server-side gameplay content.
- The 1.12.2 client cannot evaluate `{{ ... }}`; keep expression lines out of files shared
  with 1.12.2.

## Modpacks and permissions

This project is licensed under the **MIT License**. You may include it in CurseForge
modpacks without asking for separate permission. Credit to the original authors and this
port is appreciated. Do not reupload the jar as your own project.

## Credits

- Original **InGameInfoXML** for Minecraft 1.12.2: **Lunatrius**
- Earlier **InGameInfo** work: **DaftPVF**, **bspkrs**
- 1.20.1 Forge fork used as a starting point: **DF11ZomGraves**
- 1.20.1 / 1.21.1 / 26.1.2 port and maintenance: **code-onigiri**

## Links

- Source and issue tracker: https://github.com/code-onigiri/InGameInfo-reloaded
- Documentation: https://github.com/code-onigiri/InGameInfo-reloaded/tree/master/docs
- License: https://github.com/code-onigiri/InGameInfo-reloaded/blob/master/LICENSE

InGameInfoXML is not affiliated with Mojang Studios, Microsoft, Forge, NeoForge or the
original author. Minecraft is a trademark of Mojang Studios.

---

## Upload notes (not part of the description)

- The CurseForge **Summary** field should use the short text above. Keep it to one
  sentence; the moderation policy asks for a summary that is not a copy of the
  description.
- The project **avatar must be 400x400 pixels**. Use
  [docs/curseforge/avatar-400.png](curseforge/avatar-400.png); it is the 48x48 logo
  scaled 8x with matching padding. Do not use a solid-color image.
- Upload the jars as separate files for 1.20.1 Forge, 1.21.1 NeoForge and 26.1.2
  NeoForge. Do not put version numbers in the project name; use the file names and
  game-version tags instead.
- Add a changelog to each file. The moderation policy requires file updates to describe
  what changed.
- Gallery images are ready in [docs/curseforge/](curseforge/). Upload them in the order
  and with the English captions in [docs/curseforge/README.md](curseforge/README.md):
  HUD, editor, tag list, config, expressions, Botania and AE2. The description refers to
  the Gallery instead of hotlinking external images.
- Keep the English text first. If a Japanese description is added, put it after the
  English section.
- The policy allows project links and social links at the bottom of the description, but
  not external file downloads, personal donation links at the top, or shortened URLs.
  The Links section above follows that rule.
- State the license as MIT on the CurseForge project page and link the root LICENSE file.
