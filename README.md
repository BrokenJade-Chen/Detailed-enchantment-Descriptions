# Detailed Enchantment Descriptions

A client-side Fabric mod for **Minecraft 1.21.8** that adds a precise, level-scaled description line
under every enchantment in item tooltips, and replaces the need for a separate description mod.

Unlike the usual approach, the numbers are **not hard-coded**: since 1.21, enchantments are
data-driven (`data/<namespace>/enchantment/*.json`), so this mod evaluates the same
`EnchantmentLevelBasedValue` formulas the game itself uses. That means:

* values are always the real in-game values for the current version and pack,
* datapack and modded enchantments that use standard effect components are supported automatically,
* a vanilla data change does not require a mod update.

| Enchantment | Tooltip line |
|---|---|
| Sharpness V | Increases melee damage by 3. |
| Smite V | Deals 12.5 extra damage to undead. |
| Protection IV | Reduces incoming damage by 16%. |
| Unbreaking III | 75% chance to not consume durability. |
| Looting III | Increases drops by 0-3, and equipment drop chance by 3%. |

## Features

* Description + exact value for all 42 vanilla enchantments (6 of them are description-only because
  the game does not expose a number for them).
* Level-aware: the value shown is the value for the enchantment's current level.
* Works on enchanted books.
* Bilingual: English (`en_us`) and Simplified Chinese (`zh_cn`).
* Configurable in-game: uses **Cloth Config** when installed (recommended, gives the familiar
  settings page and is reachable from Mod Menu), otherwise falls back to a small built-in screen.
  Options: enable/disable, descriptions, numbers, modded enchantments, books and the number colour.
* Client side only; nothing is required on the server.
* Respects the vanilla "hide enchantments" flag.

## Screenshots

| Sword | Boots |
|---|---|
| ![Sword tooltip](docs/screenshots/tooltip-sword.png) | ![Boots tooltip](docs/screenshots/tooltip-boots.png) |
| **Enchanted book** | **Colour picker** |
| ![Enchanted book tooltip](docs/screenshots/tooltip-enchanted-book.png) | ![Colour picker](docs/screenshots/color-picker.png) |

> The rounded tooltip frame and the smoother text rendering in these screenshots come from other
> client-side mods (tooltip styling / font rendering). This mod only adds the description lines and
> provides the colour picker.

## Requirements

* Minecraft **1.21.8**
* Fabric Loader **0.17.0+**
* Fabric API (client)
* Optional: Mod Menu (settings entry point)

## Installation

1. Install Fabric Loader for 1.21.8.
2. Drop `detailed_enchantment_descriptions-<version>.jar` and Fabric API into `mods/`.
3. Remove other enchantment description mods to avoid duplicated lines.

## Settings

Open the settings from **Mod Menu → Detailed Enchantment Descriptions**, with `/dedconfig`
(or `/ded`), or by binding a key in *Options → Controls → Key Binds* (unbound by default).
Settings are stored in `config/detailed_enchantment_descriptions.json`.

## Building

```bash
./gradlew build
# jar: build/libs/detailed_enchantment_descriptions-<version>.jar
```

Requires JDK 21.

## How the values are read

* Value effects (`damage`, `damage_protection`, `knockback`, ...): `Enchantment#getEffect` +
  `EnchantmentLevelBasedValue#getValue(level)`.
* Attribute effects (`efficiency`, `depth_strider`, `sweeping_edge`, ...):
  `EnchantmentEffectComponentTypes.ATTRIBUTES` + the same level based value.
* Damage reduction: every point of `damage_protection` is 4% (`DamageUtil#getInflictedDamage`,
  capped at 20 points = 80%).
* A few values live in code rather than in the enchantment data and are documented in
  `EnchantmentDescriber` (for example the 5 second fire duration of Flame).

## Credits

Inspired by [Exact Enchantments](https://modrinth.com/mod/exact-enchantments) by Ronidas (MIT).
This is an independent reimplementation for Fabric 1.21.8; no code was copied from it.

## License

MIT — see [LICENSE](LICENSE).
