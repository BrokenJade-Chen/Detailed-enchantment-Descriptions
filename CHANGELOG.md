# Changelog

## 1.0.0

* First release: Fabric 1.21.1 / 1.21.4 / 1.21.8 / 1.21.11 and NeoForge 1.21.1.
* 42 vanilla enchantments, level-accurate values read from the game's own data.
* Settings screen with HSV colour picker, hold-a-key mode, JSON overrides and `/ded` commands.
* Respects the vanilla "hide enchantments" flag.


## 0.2.10

* Removed the in-game description editor (screens and its config entry): numbers that live outside
  plain components made the editing experience unreliable. Player overrides stay available as JSON
  files (config folder or resource pack) plus `/ded export`, `/ded reload` and `/ded dump`.


## 0.2.9

* New `source` line type for numbers that are not plain components/attributes, and the editor now
  offers them: Unbreaking (chance), Thorns, Fire Aspect, Flame, Frost Walker, Wind Burst
  (knockback), Respiration (seconds), Swift Sneak, Soul Speed (durability chance).


## 0.2.8

* JSON `attribute` lines now fall back to `location_changed` attributes, so Soul Speed shows a value
  instead of 0.
* The editor/export pick sensible units per attribute (movement speed x1000, ratios x100, oxygen x15),
  matching the built-in text.
* Fixed the Chinese "too many placeholders" warning printing raw `%s`.


## 0.2.7

* The enchantment browser now uses paged buttons instead of a list widget (the list widget's entry
  renderer differs on 1.21.11 and left the page almost empty).
* The editor detects numbers from `location_changed` attributes too (Soul Speed's movement speed was
  missed), and shows a warning when the template has more `%s` than detected numbers.


## 0.2.6

* Fixed `/ded export` and JSON `value` lines: they looked components up in the wrong registry
  (`DATA_COMPONENT_TYPE` instead of `ENCHANTMENT_EFFECT_COMPONENT_TYPE`), which made the export fail
  and value lines silently resolve to nothing.
* New in-game editor: Settings -> **Enchantment descriptions (overrides)** lists every enchantment,
  opens an editor where `%s` placeholders are filled with the detected numbers, supports
  replace/append/hide, and saves straight to the config folder.


## 0.2.5

* `/ded export <id>` writes a starter JSON file for any enchantment.
* New `template` line type: multiple `%s` values in one line (see docs/EXTENDING.md).


## 0.2.4

* Soul Speed: the speed bonus is now **actually** relative to the 0.1 base speed
  (40.5% / 51% / 61.5%). 0.2.3 shipped without this change because an automated text replacement
  did not match; the built class is now verified with `javap` to contain the x10 factor.


## 0.2.3

* Soul Speed: the speed bonus is now shown relative to the 0.1 base movement speed
  (40.5% / 51% / 61.5% instead of 4.1% / 5.1% / 6.1%); durability cost is 4% per level.
* Sweeping Edge: wording now says the ratio is *extra* weapon damage (50% / 67% / 75%),
  not total attack damage.
* Fixed build script writing jars to the wrong dist directory.


## 0.2.2

* Fixed the "only while holding key" option: the tooltip now checks the physical key state instead
  of `KeyBinding#isPressed()`, which vanilla resets while a screen is handling input.
* Added the missing translation for the "show descriptions" key binding.
* Added `tools/check-jar.mjs`, a build-artifact sanity check (classes, version, lang keys, texture).
* **Multi-version build**: the same source now compiles for 1.21.1, 1.21.4, 1.21.8 and 1.21.11.
  Version specific code lives in `overlays/<version>/` and is selected by `tools/build-version.mjs`.

## 0.2.1

* New option **"Only while holding key"**: when enabled, descriptions are only added to the tooltip
  while the new (rebindable, default Left Alt) "Show descriptions" key is held.

## 0.2.0

* **JSON descriptions for any enchantment** (modded included): drop a file into a resource pack
  (`assets/<ns>/enchantment_descriptions/*.json`) or `config/detailed_enchantment_descriptions/enchantments/`.
  Config folder wins over resource packs; `mode: replace|append` and `disable` are supported.
  See [docs/EXTENDING.md](docs/EXTENDING.md).
* **Generic fallback**: enchantments without a hand written rule now get a value line derived from
  the standard effect components (`damage`, `damage_protection`, `knockback`, ...).
* New commands: `/ded reload`, `/ded dump <id>`.
* Builds for 1.21.8 (`-Pmcver=1.21.8`); other targets still need per-version adaptations.

## 0.1.9

* The colour picker preview now matches the tooltip: when editing the number colour only the number
  is tinted (the description keeps its own colour), and vice versa.
* The picker also offers 12 preset swatches and a `#RRGGBB` input field, next to the wheel and the
  brightness slider.

## 0.1.8

* Fixed the colour picker being blank: screen text was drawn with `0xFFFFFF`, which has a zero alpha
  channel and is therefore invisible — text colours now include `0xFF`.
* Re-laid out the picker so the title, hex value and both preview lines stay on screen on small
  GUI scales.

## 0.1.7

* The settings screen is now fully built in (vanilla widgets), and the colour options open a real
  **HSV colour wheel** with a brightness slider and a live preview. Cloth Config is no longer used
  (its `startColorField` only exposes a hex box in this version, with no picker).
* Dropped the optional Cloth Config dependency; Mod Menu stays optional.

## 0.1.6

* Dropped the opacity slider: vanilla text colours are 24-bit (`TextColor` masks the alpha channel
  away), so real transparency is not renderable. The colour picker is now a plain RGB picker for
  the description and the numbers, and any 0.1.5 ARGB value is converted to RGB on load.

## 0.1.5

* Colours now use a full ARGB colour picker (Cloth Config's `startAlphaColorField`) with an opacity
  slider, for both the description and the numbers.
* Vanilla renders text colours as 24-bit, so opacity is applied by blending the colour against the
  dark tooltip background — visually the same, no real alpha needed.
* Old `numberColor` / `descriptionColor` names are migrated automatically.

## 0.1.4

* The description text is now **dark gray** by default (it was too bright on a dark tooltip), and
  its colour is configurable separately from the number colour.

## 0.1.3

* Respect the vanilla "hide enchantments" tooltip flag
  (`TooltipDisplayComponent#hiddenComponents`), so items that hide their enchantments no longer get
  description lines.

## 0.1.2

* Fixed the settings command: opening a screen directly from a chat command was undone by the chat
  screen closing. The command now requests the screen and the client tick opens it on the next
  frame. Both `/dedconfig` and `/ded` work.
* The number colour selector now shows each option in its own colour (Cloth Config and the built-in
  screen).

## 0.1.1

* Protection values are shown as percentages again (each point of `damage_protection` is 4%; a
  double conversion made them 100x too large).
* Breach no longer shows a negative percentage; Soul Speed reads its movement bonus from the right
  effect (`location_changed`), so it no longer shows 0%.
* Descriptions are drawn in gray, the numbers keep the configured colour.
* Added Cloth Config integration (settings page + Mod Menu entry) with the built-in screen as a
  fallback, and a mod icon.

## 0.1.0

* First build: description + value line for all 42 vanilla enchantments, values computed from the
  enchantment data, bilingual `en_us` / `zh_cn`, client-side tooltip hook via
  `ItemTooltipCallback`.
