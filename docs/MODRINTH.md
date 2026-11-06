# Modrinth / GitHub release copy

> **AI disclosure**: this mod was written by an AI agent (DeepSeek Harness) driving a checkout on the
> author's machine. A human decided the scope, tested every build in-game on a real 1.21.8 server,
> and approved the result. The value formulas were verified against the 1.21.8 bytecode, not copied
> from another mod.

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
game's own enchantment data, so they match what the game actually does — and work for datapack and
modded enchantments too. English and 简体中文. 1.21.8 Fabric, client only.

---

## Full description

**Detailed Enchantment Descriptions** puts one extra line under every enchantment in an item
tooltip, telling you exactly what it does at its current level:

* Sharpness V → *Increases melee damage by 3.*
* Protection IV → *Reduces incoming damage by 16%.*
* Unbreaking III → *75% chance to not consume durability.*
* Looting III → *Increases drops by 0-3, and equipment drop chance by 3%.*

### Why the numbers are trustworthy

Since Minecraft 1.21, enchantments are data-driven: their effects live in
`data/<namespace>/enchantment/*.json` and the game evaluates them with
`EnchantmentLevelBasedValue`. This mod reads those same values, so the tooltip cannot drift from the
game's behaviour, datapack changes are picked up automatically, and enchantments added by other mods
(that use the standard effect components) are described without any extra work.

All 42 vanilla enchantments are covered: 36 with computed values and 6 description-only
(Binding/Vanishing Curse, Channeling, Fortune, Infinity, Silk Touch) because the game does not expose
a number for them.

### Features

* 42 vanilla enchantments, plus modded ones that use standard effect components
* Current level is reflected; values update when the enchantment level changes
* Works on enchanted books
* English and Simplified Chinese
* In-game settings (Cloth Config when installed, built-in screen otherwise): toggle descriptions,
  numbers, modded enchantments, books, and the number colour
* Client-side only — servers do not need it

### Requirements

* Minecraft 1.21.8
* Fabric Loader 0.17.0+
* Fabric API
* Optional: Cloth Config (recommended settings UI), Mod Menu (settings entry)

### Notes

* Remove other enchantment description mods (e.g. Enchantment Descriptions) to avoid duplicate lines.
* The mod only displays information; it never changes enchantment behaviour.

### Credits

Inspired by [Exact Enchantments](https://modrinth.com/mod/exact-enchantments) by Ronidas (MIT).
This is an independent reimplementation for Fabric 1.21.8; no code was copied from it.

### Source / license

MIT. Source, issues and changelog: <repository link>.
