package dev.chenjl.detailedench.data;

import com.google.gson.GsonBuilder;
import dev.chenjl.detailedench.DetailedEnchantmentDescriptions;
import dev.chenjl.detailedench.tooltip.EnchantmentValues;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import dev.chenjl.detailedench.compat.Attrs;
import dev.chenjl.detailedench.tooltip.EnchantmentDescriber;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/** Detects the numbers an enchantment uses and can write a starter JSON file for it. */
public final class DescriptionExporter {
    private static final GsonBuilder GSON = new GsonBuilder().setPrettyPrinting();

    private static final List<ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>>> COMPONENTS = List.of(
            net.minecraft.component.EnchantmentEffectComponentTypes.DAMAGE,
            net.minecraft.component.EnchantmentEffectComponentTypes.DAMAGE_PROTECTION,
            net.minecraft.component.EnchantmentEffectComponentTypes.KNOCKBACK,
            net.minecraft.component.EnchantmentEffectComponentTypes.PROJECTILE_PIERCING,
            net.minecraft.component.EnchantmentEffectComponentTypes.PROJECTILE_COUNT,
            net.minecraft.component.EnchantmentEffectComponentTypes.ARMOR_EFFECTIVENESS);

    private static final List<Identifier> ATTRIBUTES = List.of(
            Identifier.of("minecraft", "mining_efficiency"),
            Identifier.of("minecraft", "water_movement_efficiency"),
            Identifier.of("minecraft", "oxygen_bonus"),
            Identifier.of("minecraft", "sneaking_speed"),
            Identifier.of("minecraft", "sweeping_damage_ratio"),
            Identifier.of("minecraft", "movement_speed"));

    /** Number that is not a plain component/attribute (probability, seconds, radius, ...). */
    private record Extra(String key, float scale, BiFunction<Enchantment, Integer, Float> fn) {
    }

    private static final Map<Identifier, List<Extra>> EXTRAS = new HashMap<>();

    static {
        EXTRAS.put(Identifier.of("minecraft", "unbreaking"), List.of(new Extra("unbreaking_chance", 100.0F,
                (ench, level) -> EnchantmentDescriber.unbreakingChance(ench, level, ItemStack.EMPTY))));
        EXTRAS.put(Identifier.of("minecraft", "thorns"), List.of(new Extra("thorns_chance", 100.0F,
                (ench, level) -> 0.15F * level)));
        EXTRAS.put(Identifier.of("minecraft", "fire_aspect"), List.of(new Extra("fire_seconds", 1.0F,
                EnchantmentDescriber::igniteSeconds)));
        EXTRAS.put(Identifier.of("minecraft", "flame"), List.of(new Extra("flame_seconds", 1.0F,
                (ench, level) -> 5.0F)));
        EXTRAS.put(Identifier.of("minecraft", "frost_walker"), List.of(new Extra("frost_radius", 1.0F,
                EnchantmentDescriber::frostRadius)));
        EXTRAS.put(Identifier.of("minecraft", "wind_burst"), List.of(new Extra("wind_burst_knockback", 1.0F,
                EnchantmentDescriber::windBurstKnockback)));
        EXTRAS.put(Identifier.of("minecraft", "respiration"), List.of(new Extra("air_seconds", 1.0F,
                (ench, level) -> EnchantmentValues.attribute(ench, Attrs.OXYGEN_BONUS, level) * 15.0F)));
        EXTRAS.put(Identifier.of("minecraft", "swift_sneak"), List.of(new Extra("sneak_speed", 100.0F,
                (ench, level) -> 0.3F + EnchantmentValues.attribute(ench, Attrs.SNEAKING_SPEED, level))));
        EXTRAS.put(Identifier.of("minecraft", "soul_speed"), List.of(new Extra("durability_chance", 100.0F,
                (ench, level) -> 0.04F * level)));
    }

    /** Evaluates a named extra source, or 0 when unknown. */
    public static float sourceValue(Identifier enchantment, String key, Enchantment instance, int level) {
        for (Extra extra : EXTRAS.getOrDefault(enchantment, List.of())) {
            if (extra.key().equals(key)) {
                return extra.fn().apply(instance, level);
            }
        }
        return 0.0F;
    }

    private DescriptionExporter() {
    }

    /** @return one spec per number the enchantment exposes, in a stable order */
    public static List<DescriptionFile.ValueSpec> detectValues(Enchantment enchantment, Identifier id) {
        List<DescriptionFile.ValueSpec> values = new ArrayList<>();
        List<Extra> extras = EXTRAS.getOrDefault(id, List.of());
        if (!extras.isEmpty() && !"soul_speed".equals(id.getPath())) {
            for (Extra extra : extras) {
                DescriptionFile.ValueSpec spec = new DescriptionFile.ValueSpec();
                spec.source = extra.key();
                spec.scale = extra.scale();
                values.add(spec);
            }
            return values;
        }
        for (ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> component : COMPONENTS) {
            if (Math.abs(EnchantmentValues.sum(enchantment, component, 1)) < 0.0001F) {
                continue;
            }
            Identifier componentId = Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.getId(component);
            if (componentId == null) {
                continue;
            }
            DescriptionFile.ValueSpec spec = new DescriptionFile.ValueSpec();
            spec.component = componentId.toString();
            values.add(spec);
        }
        for (Identifier attributeId : ATTRIBUTES) {
            EntityAttribute attribute = Registries.ATTRIBUTE.get(attributeId);
            if (attribute == null) {
                continue;
            }
            var entry = Registries.ATTRIBUTE.getEntry(attribute);
            float normal = EnchantmentValues.attribute(enchantment, entry, 1);
            float located = EnchantmentValues.locationAttribute(enchantment, entry, 1);
            if (Math.abs(normal) < 0.0001F && Math.abs(located) < 0.0001F) {
                continue;
            }
            DescriptionFile.ValueSpec spec = new DescriptionFile.ValueSpec();
            spec.attribute = attributeId.toString();
            spec.scale = defaultScale(attributeId);
            values.add(spec);
        }
        return values;
    }

    /** Same units the built-in descriptions use (percent for ratios, seconds for oxygen). */
    private static float defaultScale(Identifier attributeId) {
        return switch (attributeId.getPath()) {
            case "movement_speed" -> 1000.0F;      // relative to the 0.1 base speed
            case "water_movement_efficiency", "sweeping_damage_ratio" -> 100.0F;
            case "oxygen_bonus" -> 15.0F;          // seconds per level
            case "sneaking_speed" -> 100.0F;
            default -> 1.0F;
        };
    }

    public static Path export(Enchantment enchantment, Identifier id) throws IOException {
        DescriptionFile file = new DescriptionFile();
        file.enchantment = id.toString();
        file.mode = "replace";

        for (DescriptionFile.ValueSpec spec : detectValues(enchantment, id)) {
            DescriptionFile.Line line = new DescriptionFile.Line();
            if (spec.source != null) {
                line.type = "source";
                line.source = spec.source;
                line.format = "ded.line.value";
                line.scale = spec.scale;
                file.lines.add(line);
                continue;
            }
            if (spec.component != null) {
                line.type = "value";
                line.component = spec.component;
            } else {
                line.type = "attribute";
                line.attribute = spec.attribute;
            }
            line.format = "ded.line.value";
            file.lines.add(line);
        }

        Path dir = FabricLoader.getInstance().getConfigDir()
                .resolve(DetailedEnchantmentDescriptions.MOD_ID)
                .resolve("enchantments");
        Files.createDirectories(dir);
        Path out = dir.resolve(id.getNamespace() + "_" + id.getPath() + ".json");
        Files.writeString(out, GSON.create().toJson(file), StandardCharsets.UTF_8);
        return out;
    }
}
