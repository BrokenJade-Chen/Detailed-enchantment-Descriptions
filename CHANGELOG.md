# Changelog

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
