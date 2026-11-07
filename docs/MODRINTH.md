# Modrinth / GitHub release copy

> **AI disclosure**: this mod was written by an AI agent (DeepSeek Harness) driving a checkout on the
> author's machine. A human decided the scope and tested every build in-game. Value formulas were
> verified against the game's own data and bytecode, not copied from another mod.

---

## Gallery (upload these, in this order)

| File | Caption |
|---|---|
| `docs/screenshots/tooltip-sword.png` | Sword: knockback and unbreaking at their current level |
| `docs/screenshots/tooltip-boots.png` | Boots: soul speed and unbreaking, plus the vanilla attribute block |
| `docs/screenshots/tooltip-enchanted-book.png` | Works on enchanted books too |
| `docs/screenshots/color-picker.png` | Colour picker: wheel, 12 presets, `#RRGGBB` input, live preview |

> Caption note for the gallery: *the rounded tooltip frame and smoother font rendering come from
> other client mods, not from this one.*

---

## Short description (≤ 350 chars)

Adds precise, level-scaled description lines to enchantment tooltips. Values are read from the
game's own enchantment data, so they match what the game actually does — and cover datapack and
modded enchantments too. English and 简体中文. Fabric 1.21.1–1.21.11 and NeoForge 1.21.1, client
only.

---

## Full description

**Detailed Enchantment Descriptions** puts one extra line under every enchantment in an item
tooltip, telling you exactly what it does at its current level:

* Sharpness V → *Increases melee damage by 3.*
* Protection IV → *Reduces incoming damage by 16%.*
* Unbreaking III → *75% chance to not consume durability.* (30% on armour)
* Looting III → *Increases drops by 0-3, and equipment drop chance by 3%.*

### Why the numbers are trustworthy

Since Minecraft 1.21, enchantments are data-driven: their effects live in
`data/<namespace>/enchantment/*.json` and the game evaluates them with `LevelBasedValue`. This mod
reads those same values, so the tooltip cannot drift from the game's behaviour, datapack changes are
picked up automatically, and enchantments added by other mods (that use the standard effect
components) are described without extra work.

All 42 vanilla enchantments are covered: 36 with computed values and 6 description-only
(Binding/Vanishing Curse, Channeling, Fortune, Infinity, Silk Touch) because the game does not expose
a number for them.

### Features

* 42 vanilla enchantments, plus modded ones that use standard effect components
* Current level is reflected; values update when the enchantment level changes
* Works on enchanted books; respects the vanilla "hide enchantments" flag
* English and Simplified Chinese
* In-game settings (`/ded`, key bind or Mod Menu): toggle descriptions, numbers, books, and a full
  HSV colour picker (wheel, presets, `#RRGGBB`) for description/number colours
* Optional "only while holding a key" mode
* JSON overrides for any enchantment (resource pack or config folder), with `/ded reload`,
  `/ded dump` and `/ded export`
* Client-side only — servers do not need it

### Requirements

| Loader | Minecraft | Notes |
|---|---|---|
| Fabric | 1.21.1 / 1.21.4 / 1.21.8 / 1.21.11 | Fabric API required, Mod Menu optional |
| NeoForge | 1.21.1 (21.1.265+) | no extra dependency |

### Notes

* Remove other enchantment description mods (e.g. Enchantment Descriptions) to avoid duplicate lines.
* The mod only displays information; it never changes enchantment behaviour.

### Credits

Inspired by [Exact Enchantments](https://modrinth.com/mod/exact-enchantments) by Ronidas (MIT).
This is an independent reimplementation; no code was copied from it.

### Source / license

MIT. Source, issues and changelog: https://github.com/BrokenJade-Chen/detailed-enchantment-descriptions.
