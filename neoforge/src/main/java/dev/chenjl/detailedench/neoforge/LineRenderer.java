package dev.chenjl.detailedench.neoforge;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

import java.util.ArrayList;
import java.util.List;

/** Turns the JSON line definitions into tooltip lines. */
public final class LineRenderer {
    private LineRenderer() {
    }

    public static List<Component> render(DescriptionFile file, Enchantment enchantment, int level) {
        List<Component> out = new ArrayList<>();
        ResourceLocation enchantmentId = file.enchantment == null ? null : ResourceLocation.tryParse(file.enchantment);
        for (DescriptionFile.Line line : file.lines) {
            Component rendered = renderLine(line, enchantment, level, enchantmentId);
            if (rendered != null) {
                int color = NeoConfig.get().descriptionColorRgb;
                out.add(rendered.copy().withStyle(Style.EMPTY.withColor(color)));
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Component renderLine(DescriptionFile.Line line, Enchantment enchantment, int level,
                                        ResourceLocation enchantmentId) {
        String type = line.type == null ? "text" : line.type.toLowerCase();
        switch (type) {
            case "value" -> {
                if (line.component == null) {
                    return null;
                }
                ResourceLocation id = ResourceLocation.tryParse(line.component);
                if (id == null) {
                    return null;
                }
                DataComponentType<?> type1 = BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.get(id);
                if (type1 == null) {
                    return null;
                }
                float value = EnchantValues.sum(enchantment,
                        (DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>) (DataComponentType<?>) type1,
                        level) * line.scale;
                return format(line, value);
            }
            case "attribute" -> {
                if (line.attribute == null) {
                    return null;
                }
                ResourceLocation id = ResourceLocation.tryParse(line.attribute);
                Attribute attribute = id == null ? null : BuiltInRegistries.ATTRIBUTE.get(id);
                if (attribute == null) {
                    return null;
                }
                Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
                float bonus = EnchantValues.attribute(enchantment, holder, level);
                if (Math.abs(bonus) < 0.0001F) {
                    bonus = EnchantValues.locationAttribute(enchantment, holder, level);
                }
                return format(line, bonus * line.scale);
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
                List<Component> args = new ArrayList<>();
                for (DescriptionFile.ValueSpec spec : line.values) {
                    Component arg = renderValue(spec, enchantment, level);
                    if (arg == null) {
                        return null;
                    }
                    args.add(arg);
                }
                if (line.translate != null) {
                    return Component.translatable(line.translate, args.toArray());
                }
                return fromPattern(line.text == null ? "" : line.text, args);
            }
            default -> {
                if (line.translate != null) {
                    return Component.translatable(line.translate);
                }
                return Component.literal(line.text == null ? "" : line.text);
            }
        }
    }

    private static Component renderValue(DescriptionFile.ValueSpec spec, Enchantment enchantment, int level) {
        if (spec.source != null) {
            return null;
        }
        if (spec.component != null) {
            ResourceLocation id = ResourceLocation.tryParse(spec.component);
            DataComponentType<?> type = id == null ? null : BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.get(id);
            if (type == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            float value = EnchantValues.sum(enchantment,
                    (DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>) (DataComponentType<?>) type,
                    level) * spec.scale;
            return number(value);
        }
        if (spec.attribute != null) {
            ResourceLocation id = ResourceLocation.tryParse(spec.attribute);
            Attribute attribute = id == null ? null : BuiltInRegistries.ATTRIBUTE.get(id);
            if (attribute == null) {
                return null;
            }
            Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
            float bonus = EnchantValues.attribute(enchantment, holder, level);
            if (Math.abs(bonus) < 0.0001F) {
                bonus = EnchantValues.locationAttribute(enchantment, holder, level);
            }
            return number(bonus * spec.scale);
        }
        return null;
    }

    private static Component number(float value) {
        return Component.literal(Describer.format(value))
                .withStyle(Style.EMPTY.withColor(NeoConfig.get().numberColorRgb));
    }

    private static Component format(DescriptionFile.Line line, float value) {
        String key = line.format == null ? "ded.line.value" : line.format;
        return Component.translatable(key, number(value));
    }

    /** Splits a literal template on {@code %s} and interleaves the coloured numbers. */
    private static Component fromPattern(String pattern, List<Component> args) {
        String[] parts = pattern.split("%s", -1);
        MutableComponent result = Component.empty();
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(Component.literal(parts[i]));
            }
            if (i < args.size()) {
                result.append(args.get(i));
            }
        }
        return result;
    }

    private static Style grey() {
        return Style.EMPTY.applyFormat(ChatFormatting.GRAY);
    }
}
