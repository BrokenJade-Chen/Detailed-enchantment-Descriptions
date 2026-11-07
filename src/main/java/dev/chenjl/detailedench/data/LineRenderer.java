package dev.chenjl.detailedench.data;

import dev.chenjl.detailedench.config.ModConfig;
import dev.chenjl.detailedench.tooltip.EnchantmentValues;
import dev.chenjl.detailedench.tooltip.Numbers;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Turns the JSON line definitions into tooltip lines. */
public final class LineRenderer {
    private LineRenderer() {
    }

    public static List<Text> render(DescriptionFile file, Enchantment enchantment, int level) {
        List<Text> out = new ArrayList<>();
        Identifier enchantmentId = file.enchantment == null ? null : Identifier.tryParse(file.enchantment);
        for (DescriptionFile.Line line : file.lines) {
            Text rendered = renderLine(line, enchantment, level, enchantmentId);
            if (rendered != null) {
                out.add(rendered.copy().styled(style -> style.withColor(ModConfig.get().descriptionColorRgb)));
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Text renderLine(DescriptionFile.Line line, Enchantment enchantment, int level,
                                   Identifier enchantmentId) {
        String type = line.type == null ? "text" : line.type.toLowerCase();
        switch (type) {
            case "value" -> {
                Identifier componentId = line.component == null ? null : Identifier.tryParse(line.component);
                if (componentId == null) {
                    return null;
                }
                ComponentType<?> componentType = Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.get(componentId);
                if (componentType == null) {
                    return null;
                }
                float value = EnchantmentValues.sum(enchantment,
                        (ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>>) (ComponentType<?>) componentType,
                        level) * line.scale;
                return format(line, value);
            }
            case "attribute" -> {
                Identifier attributeId = line.attribute == null ? null : Identifier.tryParse(line.attribute);
                if (attributeId == null) {
                    return null;
                }
                EntityAttribute attribute = Registries.ATTRIBUTE.get(attributeId);
                if (attribute == null) {
                    return null;
                }
                RegistryEntry<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(attribute);
                float value = EnchantmentValues.attribute(enchantment, entry, level) * line.scale;
                return format(line, value);
            }
            case "source" -> {
                if (enchantmentId == null || line.source == null) {
                    return null;
                }
                float value = DescriptionExporter.sourceValue(enchantmentId, line.source, enchantment, level)
                        * line.scale;
                return format(line, value);
            }
            case "template" -> {
                java.util.List<Text> args = new java.util.ArrayList<>();
                for (DescriptionFile.ValueSpec spec : line.values) {
                    Text arg = renderValue(spec, enchantment, level);
                    if (arg == null) {
                        return null;
                    }
                    args.add(arg);
                }
                if (line.translate != null) {
                    return Text.translatable(line.translate, args.toArray());
                }
                return fromPattern(line.text == null ? "" : line.text, args);
            }
            default -> {
                if (line.translate != null) {
                    return Text.translatable(line.translate);
                }
                return Text.literal(line.text == null ? "" : line.text);
            }
        }
    }

    private static Text renderValue(DescriptionFile.ValueSpec spec, Enchantment enchantment, int level) {
        if (spec.component != null) {
            Identifier id = Identifier.tryParse(spec.component);
            if (id == null) {
                return null;
            }
            ComponentType<?> type = Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.get(id);
            if (type == null) {
                return null;
            }
            float value = EnchantmentValues.sum(enchantment,
                    (ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>>) (ComponentType<?>) type,
                    level) * spec.scale;
            return Numbers.coloured(value);
        }
        if (spec.attribute != null) {
            Identifier id = Identifier.tryParse(spec.attribute);
            if (id == null) {
                return null;
            }
            EntityAttribute attribute = Registries.ATTRIBUTE.get(id);
            if (attribute == null) {
                return null;
            }
            var entry = Registries.ATTRIBUTE.getEntry(attribute);
            float bonus = EnchantmentValues.attribute(enchantment, entry, level);
            if (Math.abs(bonus) < 0.0001F) {
                // e.g. Soul Speed applies its speed through location_changed effects
                bonus = EnchantmentValues.locationAttribute(enchantment, entry, level);
            }
            return Numbers.coloured(bonus * spec.scale);
        }
        return null;
    }

    /** Splits a literal template on {@code %s} and interleaves the coloured numbers. */
    private static Text fromPattern(String pattern, java.util.List<Text> args) {
        String[] parts = pattern.split("%s", -1);
        var result = Text.empty();
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(Text.literal(parts[i]));
            }
            if (i < args.size()) {
                result.append(args.get(i));
            }
        }
        return result;
    }

    private static Text format(DescriptionFile.Line line, float value) {
        String key = line.format == null ? "ded.line.value" : line.format;
        return Text.translatable(key, Numbers.coloured(value));
    }
}
