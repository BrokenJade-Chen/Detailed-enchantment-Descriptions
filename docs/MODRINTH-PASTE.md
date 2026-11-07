> **AI disclosure**: this mod was written with AI assistance (DeepSeek Harness) on the author's machine. The author reviewed the code and tested every release in-game.

# Detailed Enchantment Descriptions

Adds a short description under every enchantment in an item tooltip, with the numbers computed from the enchantment's own data — so it always matches what the game actually does at that level.

Works on enchanted books, respects the vanilla "hide enchantments" flag, and supports JSON overrides for any enchantment (including other mods').

## Supported versions

| Loader | Minecraft |
|---|---|
| Fabric | 1.21.1, 1.21.4, 1.21.8, 1.21.11 |
| NeoForge | 1.21.1 |

Client side only. On Fabric, **Fabric API** is required. Mod Menu is optional (adds a settings entry).

## Features

* **42 vanilla enchantments** covered, each with a one-line description of its current level.
* **Numbers come from the game**, not from a hand-written table: the mod evaluates the same enchantment data the game uses, so values cannot drift and datapack changes are picked up automatically.
* **Works with modded enchantments** that use the standard effect components, and any enchantment can be described through a JSON file.
* **Config screen** — open with `/ded`, `/dedconfig`, a rebindable key, or Mod Menu on Fabric:
  * toggle descriptions, numbers, enchanted books
  * colour picker with an HSV wheel, 12 presets and a `#RRGGBB` field, for the description colour and the number colour
* **Hold-a-key mode** — optionally only show descriptions while a key is held.
* **JSON overrides** — drop a file into a resource pack (`assets/<namespace>/enchantment_descriptions/*.json`) or the config folder (`config/detailed_enchantment_descriptions/enchantments/*.json`). Modes: `replace`, `append`, `disable`. Commands: `/ded reload`, `/ded dump <id>`, `/ded export <id>`.
* English and Simplified Chinese, numbers formatted without a trailing `.0`.
* Respects the vanilla "hide enchantments" flag.

## What it looks like

* Sharpness V → *Increases melee damage by 3.*
* Protection IV → *Reduces incoming damage by 16%.*
* Unbreaking III → *75% chance to not consume durability.* (30% on armour)
* Soul Speed III → *Increases movement speed on soul blocks by 61.5%, durability use by 12%.*

## Notes

* Remove other enchantment description mods (for example Enchantment Descriptions) to avoid duplicate lines.
* The mod only displays information; it never changes enchantment behaviour.
* The rounded tooltip frame and smoother font rendering in the screenshots come from other client mods, not from this one.

## Credits

Inspired by [Exact Enchantments](https://modrinth.com/mod/exact-enchantments) by Ronidas (MIT). This is an independent reimplementation; no code was copied from it.

## License

MIT. Source and issue tracker: [github.com/BrokenJade-Chen/Detailed-enchantment-Descriptions](https://github.com/BrokenJade-Chen/Detailed-enchantment-Descriptions)
