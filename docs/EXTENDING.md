# Adding support for other mods' enchantments

You do **not** need to write Java to teach this mod about another mod's enchantment. Drop a small
JSON file into a resource pack (best for modpacks and servers) or into the config folder (best for a
single player tweak). Both use the same schema.

## Where the file goes

| Carrier | Path | Use it when |
|---|---|---|
| Resource pack | `assets/<any namespace>/enchantment_descriptions/<name>.json` | you ship a modpack / server resource pack |
| Config folder | `config/detailed_enchantment_descriptions/enchantments/<name>.json` | you want a local override |

Load order: resource packs first, then the config folder. **The config folder wins** when both
describe the same enchantment. Files are re-read on `F3+T`, or with `/ded reload` (config folder
only).

## Minimal example

```json
{
  "schema": 1,
  "enchantment": "somemod:fire_touch",
  "lines": [
    { "type": "text", "translate": "somemod.enchantment.fire_touch.desc" }
  ]
}
```

## Full example

```json
{
  "schema": 1,
  "enchantment": "somemod:fire_touch",
  "mode": "replace",
  "lines": [
    { "type": "text",  "text": "Sets the target on fire." },
    { "type": "value", "component": "minecraft:damage", "format": "ded.line.value", "scale": 1.0 },
    { "type": "attribute", "attribute": "minecraft:mining_efficiency", "scale": 1.0 },
    { "type": "value", "component": "somemod:charge_bonus", "scale": 100.0 }
  ]
}
```

## Lines with more than one number

Use `type: "template"`: every `%s` in `text` (or in the translation key) is replaced, in order, by
the values listed in `values`. Numbers are coloured automatically.

```json
{
  "schema": 1,
  "enchantment": "minecraft:soul_speed",
  "mode": "replace",
  "lines": [
    {
      "type": "template",
      "text": "在灵魂方块上速度 +%s，耐久消耗 %s。",
      "values": [
        { "attribute": "minecraft:movement_speed", "scale": 1000.0 },
        { "component": "minecraft:item_damage", "scale": 400.0 }
      ]
    }
  ]
}
```

* `values` entries accept `component` (value effects are summed) or `attribute`, plus `scale`.
* For built-in lines the translation keys already use positional arguments, so a resource pack can
  reorder them with `%1$s` / `%2$s`.

## Named sources

Some numbers are not stored as plain components (they live in loot conditions or effect options).
Use `type: "source"` with one of these keys:

| Enchantment | `source` | Meaning |
|---|---|---|
| `minecraft:unbreaking` | `unbreaking_chance` | chance to not consume durability (percent) |
| `minecraft:thorns` | `thorns_chance` | reflect chance (percent) |
| `minecraft:fire_aspect` | `fire_seconds` | burn seconds |
| `minecraft:flame` | `flame_seconds` | burn seconds (always 5) |
| `minecraft:frost_walker` | `frost_radius` | ice radius |
| `minecraft:wind_burst` | `wind_burst_knockback` | explosion knockback multiplier |
| `minecraft:respiration` | `air_seconds` | extra underwater seconds |
| `minecraft:swift_sneak` | `sneak_speed` | sneaking speed factor (percent) |
| `minecraft:soul_speed` | `durability_chance` | durability use chance per block (percent) |

```json
{ "type": "source", "source": "unbreaking_chance", "format": "ded.line.value" }
```

## Exporting a starter file

`/ded export <enchantment id>` writes a template to
`config/detailed_enchantment_descriptions/enchantments/<namespace>_<path>.json` containing one line
per detected effect component. Edit it and run `/ded reload`.

## Fields

### Top level

| Field | Type | Default | Meaning |
|---|---|---|---|
| `schema` | int | `1` | Format version. Always `1` for now. |
| `enchantment` | string | — | Enchantment id, e.g. `minecraft:sharpness`, `somemod:fire_touch`. |
| `mode` | `"replace"` / `"append"` | `"replace"` | Replace the built-in description, or append these lines after it. |
| `disable` | boolean | `false` | Show nothing for this enchantment. |
| `lines` | array | `[]` | Lines in display order. |

### Line

| Field | Applies to | Meaning |
|---|---|---|
| `type` | all | `text`, `value` or `attribute`. Default `text`. |
| `translate` | text | Translation key, e.g. `somemod.enchantment.fire_touch.desc`. |
| `text` | text | Literal fallback when no translation key is given. |
| `component` | value | Data component id whose value effects are summed, e.g. `minecraft:damage`. |
| `attribute` | attribute | Attribute id, e.g. `minecraft:mining_efficiency`. |
| `format` | value/attribute | Translation key for the template; the number is passed as `%s`. Defaults to `ded.line.value` (`%s`). |
| `scale` | value/attribute | Multiplier applied before formatting. Use `100` to show a ratio as a percentage. |

The number itself is computed from the enchantment's own data (`LevelBasedValue`), so it always
matches what the game does — you only decide the wording and the unit.

## Notes

* Unknown `component` / `attribute` ids are ignored silently (enable `verboseUnknown` in the config
  to see a warning).
* `mode: "append"` is the safest choice when you only want to add one extra line.
* Use `/ded dump <enchantment id>` in game to see whether a built-in rule or an override is active.

## For mod authors (Java)

Not needed for descriptions. If your enchantment cannot be expressed with the standard effect
components (for example it reads NBT directly), open an issue and we can discuss adding a small
API entrypoint — we deliberately keep the JSON path as the primary extension mechanism.
