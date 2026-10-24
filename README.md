# InGameInfoXML (reloaded)

[English](README.md) | [日本語](README_ja.md)

A Forge / NeoForge port of [InGameInfoXML](https://github.com/Lunatrius/InGame-Info-XML).
It reuses the 1.12.2 HUD definitions (XML / JSON / TXT) as far as possible and renders
them on Minecraft 1.20.1, 1.21.1 and 26.1.2.

Maintained by [code-onigiri](https://github.com/code-onigiri).

![Default HUD](docs/render/hud-1.20.1.png)

## Features

- Ported 1.12.2 XML / JSON / TXT definitions, tag names, nine alignments and scaling
- HUD text, icons, items, potion effects and armor durability
- Config screen, tag list, HUD editor and debug element inspector
- Arithmetic, functions and formatting with the `{{ ... }}` expression syntax, alongside the existing `{tag}` syntax
- Optional mod integrations for Create, Curios, AE2, Mekanism, Cobblemon and others
- Nine languages (en_us, ja_jp, de_de, fr_fr, no_no, pl_pl, ru_ru, zh_cn, zh_tw)

## Supported versions

| Minecraft | Loader | Java | Status |
|---|---|---|---|
| 1.20.1 | Forge 47.4.28 | 17 | Build and HUD / GUI rendering verified |
| 1.21.1 | NeoForge 21.1.264 | 21 | Build and HUD / GUI rendering verified |
| 26.1.2 | NeoForge 26.1.2.122 | 25 | Build and HUD / GUI rendering verified |

## Building and installation

With Nix (flakes enabled), the development shells provide the JDK, the Java runtime for
Gradle and the libraries needed to run the GUI.

```bash
nix develop .#mc1201 -c bash -c 'cd versions/1.20.1 && ./gradlew build'
nix develop .#mc1211 -c bash -c 'cd versions/1.21.1 && ./gradlew build'
nix develop .#mc2612 -c bash -c 'cd versions/26.1.2 && ./gradlew build'
```

The jar is written to `versions/<version>/build/libs/ingameinfoxml-2.9.2.102.jar`. Put it
in the `mods/` directory of the matching Minecraft version. Without Nix, install the JDK
required by that version and use the bundled Gradle wrapper.

## Usage

On first launch, the loader configuration `config/ingameinfoxml-client.toml` and the HUD
definition `config/InGameInfo.xml` are generated. The HUD definition uses the same file
name and format as 1.12.2.

### Commands

| Command | Description |
|---|---|
| `/igi reload` | Reload the HUD definition |
| `/igi load <file>` | Load another HUD definition |
| `/igi save <file>` | Save the current HUD definition (XML / JSON / TXT by extension) |
| `/igi enable` / `/igi disable` | Temporarily enable or disable the HUD |
| `/igi taglist` | Open the registered tag list GUI |
| `/igi debug` | Open the debug element inspector (default key `F8`) |
| `/igi editor` | Open the HUD editor (default key `F9`) |
| `/igi alignment get <ALIGN>` | Show the offset of an alignment |
| `/igi alignment set <ALIGN> [x y]` | Change the offset of an alignment |
| `/igi config <key> <value>` | Change a setting |

### GUI

- Open the config screen from Config in the Mods screen. It changes the HUD definition
  file name, scale, vanilla HUD visibility, alignments and other settings.
- The HUD editor (`F9`) opens at the bottom of the screen. It adds, removes and reorders
  lines, inserts tags, selects one of the nine alignments and adjusts X / Y offsets.
- The debug element inspector (`F8`) lists every registered tag and its current value by
  category. Clicking a tag name copies `<tag>`.

## HUD definitions

Write display lines under `<lines at="...">` in `config/InGameInfo.xml`. A 1.12.2
definition works without changes.

```xml
<config>
  <lines at="topleft">
    <line><str>HP: {health}/{maxhealth}</str></line>
    <line><str>Pos: {{ round(x, 1) }}, {{ round(z, 1) }}</str></line>
  </lines>
</config>
```

`{tag}` is replaced by the value of the tag. Color codes `$0` to `$f` and `$r` are
available. JSON and TXT definitions can be loaded, and `/igi save` writes the format
selected by the file extension.

## Expressions `{{ ... }}`

Inside `{{ ... }}`, tag names can be used as values in arithmetic, parentheses and
function calls.

```xml
<str>Damage: {{ attackdamage * 2 + 1 }}</str>
<str>HP: {{ format("%.1f", health / maxhealth * 100) }}%</str>
```

- Operators: `+` `-` `*` `/` `%`, parentheses and unary `+` / `-`
- Functions: `round`, `min`, `max`, `abs`, `floor`, `ceil`, `format`
- `format("format", value)` takes its arguments in the same order as Java
  `String.format(Locale.ENGLISH, ...)`

If a tag is unknown or the syntax is invalid, the whole `{{ ... }}` is displayed as
written. In XML text, write `<` as `&lt;`. See
[docs/EXPRESSIONS.md](docs/EXPRESSIONS.md) for details.

![Expressions](docs/render/expressions-1.20.1.png)

## Mod integration

Tags are registered only when the matching mod is installed. Without the mod no tags are
registered, so other HUD lines are unaffected. There are no compile-time dependencies.

| Category | Mods | Supported versions |
|---|---|---|
| Equipment / status | Curios, Tough As Nails, Cold Sweat, Thirst Was Taken, Vampirism, Paragliders | All |
| Technology | Create, Applied Energistics 2, Mekanism | All |
| Energy | Forge / NeoForge Energy | All |
| Magic | Botania | 1.20.1 |
| Magic | Ars Nouveau, Iron's Spells 'n Spellbooks, Goety | All |
| World / misc | Serene Seasons, Cobblemon | All |
| World / misc | Tinkers' Construct | 1.20.1 |

"All" means that tags are registered on 1.20.1, 1.21.1 and 26.1.2. The range of values
actually verified is summarized in [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md).

Tags whose primary source is the block under the crosshair use an `over` suffix
(`createrpmover`, `ae2networkover` and so on). Tag names, old-name aliases and
verification status are listed in [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md).

## Documentation

Most of the detailed documentation is currently written in Japanese. The table below
summarizes what each document covers.

| Document | Contents |
|---|---|
| [docs/README.md](docs/README.md) | Reading order by purpose and document index |
| [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md) | Differences from 1.12.2, config keys, tags and compatibility values |
| [docs/EXPRESSIONS.md](docs/EXPRESSIONS.md) | Expression grammar, functions, formatting and failure behavior |
| [docs/DEBUG_SCREEN.md](docs/DEBUG_SCREEN.md) | Debug element inspector usage |
| [docs/DEBUG_MODS.md](docs/DEBUG_MODS.md) | Enabling and disabling development mods |
| [docs/RENDER_TEST.md](docs/RENDER_TEST.md) | Headless render test procedure and results |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Structure, processing flow and the mod integration SPI |

## Development

### Repository layout

```
.
├── flake.nix                  # Dev shells with JDK / Mesa / Xvfb / ImageMagick
├── LICENSE                    # MIT
├── README.md                  # English README
├── README_ja.md               # Japanese README
├── common/                    # Version-independent design notes
├── docs/                      # User and developer documentation
├── gradle/debug-mods.gradle   # Development mod selection and deobfuscation
├── scripts/render-test.sh     # Capture the HUD with Xvfb + llvmpipe
└── versions/
    ├── 1.20.1/                # Forge 1.20.1
    ├── 1.21.1/                # NeoForge 1.21.1
    └── 26.1.2/                # NeoForge 26.1.2
```

Each version is a separate Gradle project. Start the development client with
`cd versions/<version> && ./gradlew runClient`. The dev shell leaves the host GPU
enabled, so the client runs on hardware. Only `scripts/render-test.sh` forces Mesa's
llvmpipe for reproducible headless captures.

### Development mods

Place jars in `versions/<version>/libs/debug-mods/` and set the matching entry in
`ext.debugMods` to true to launch with that mod installed.

```groovy
ext.debugMods = [
        tconstruct: true,   // Mantle is added automatically
        cobblemon: false,
        mekanism: false,
        ae2: false,
]
```

Run `./gradlew listDebugMods` to list the entries, or `./gradlew syncDebugMods` to only
synchronize the selected jars. See [docs/DEBUG_MODS.md](docs/DEBUG_MODS.md) for details.

### Render tests

A real client runs under Xvfb with Mesa llvmpipe and saves the HUD as a PNG.

```bash
GRADLE_USER_HOME=/tmp/gradle-home \
  nix develop .#mc1201 -c scripts/render-test.sh 1.20.1 RenderTest
```

See [docs/RENDER_TEST.md](docs/RENDER_TEST.md) for test world setup, checks and
per-version results.

## License

MIT. Original implementation Copyright 2014-2016 Jadran "Lunatrius" Kotnik. The 1.20.1,
1.21.1 and 26.1.2 ports are based on work Copyright 2025 DF11ZomGraves. The full text is
in [LICENSE](LICENSE).
