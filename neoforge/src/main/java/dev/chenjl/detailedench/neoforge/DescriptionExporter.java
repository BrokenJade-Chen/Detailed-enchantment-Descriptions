package dev.chenjl.detailedench.neoforge;

import com.google.gson.GsonBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/** Detects the numbers an enchantment uses and writes a starter JSON file for it. */
public final class DescriptionExporter {
    private static final GsonBuilder GSON = new GsonBuilder().setPrettyPrinting();

    private record Extra(String key, float scale, BiFunction<Enchantment, Integer, Float> fn) {
    }

    private static final Map<ResourceLocation, List<Extra>> EXTRAS = new HashMap<>();

    static {
        EXTRAS.put(id("minecraft", "unbreaking"), List.of(new Extra("unbreaking_chance", 100.0F,
                (ench, level) -> unbreaking(ench, level, ItemStack.EMPTY))));
        EXTRAS.put(id("minecraft", "thorns"), List.of(new Extra("thorns_chance", 100.0F,
                (ench, level) -> 0.15F * level)));
        EXTRAS.put(id("minecraft", "fire_aspect"), List.of(new Extra("fire_seconds", 1.0F,
                (ench, level) -> igniteSeconds(ench, level))));
        EXTRAS.put(id("minecraft", "flame"), List.of(new Extra("flame_seconds", 1.0F,
                (ench, level) -> 5.0F)));
        EXTRAS.put(id("minecraft", "frost_walker"), List.of(new Extra("frost_radius", 1.0F,
                (ench, level) -> frostRadius(ench, level))));
        EXTRAS.put(id("minecraft", "wind_burst"), List.of(new Extra("wind_burst_knockback", 1.0F,
                (ench, level) -> windBurst(ench, level))));
        EXTRAS.put(id("minecraft", "respiration"), List.of(new Extra("air_seconds", 1.0F,
                (ench, level) -> EnchantValues.attribute(ench, attributeHolder("oxygen_bonus"), level) * 15.0F)));
        EXTRAS.put(id("minecraft", "swift_sneak"), List.of(new Extra("sneak_speed", 100.0F,
                (ench, level) -> 0.3F + EnchantValues.attribute(ench, attributeHolder("sneaking_speed"), level))));
        EXTRAS.put(id("minecraft", "soul_speed"), List.of(new Extra("durability_chance", 100.0F,
                (ench, level) -> 0.04F * level)));
    }

    private DescriptionExporter() {
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static Holder<Attribute> attributeHolder(String path) {
        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.withDefaultNamespace(path));
        return BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
    }

    public static float sourceValue(ResourceLocation enchantment, String key, Enchantment instance, int level) {
        for (Extra extra : EXTRAS.getOrDefault(enchantment, List.of())) {
            if (extra.key().equals(key)) {
                return extra.fn().apply(instance, level);
            }
        }
        return 0.0F;
    }

    public static List<DescriptionFile.ValueSpec> detectValues(Enchantment enchantment, ResourceLocation id) {
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
        for (DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> component : List.of(
                EnchantmentEffectComponents.DAMAGE,
                EnchantmentEffectComponents.DAMAGE_PROTECTION,
                EnchantmentEffectComponents.KNOCKBACK,
                EnchantmentEffectComponents.PROJECTILE_PIERCING,
                EnchantmentEffectComponents.PROJECTILE_COUNT,
                EnchantmentEffectComponents.ARMOR_EFFECTIVENESS)) {
            if (Math.abs(EnchantValues.sum(enchantment, component, 1)) < 0.0001F) {
                continue;
            }
            ResourceLocation componentId = BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.getKey(component);
            if (componentId == null) {
                continue;
            }
            DescriptionFile.ValueSpec spec = new DescriptionFile.ValueSpec();
            spec.component = componentId.toString();
            values.add(spec);
        }
        for (String path : List.of("mining_efficiency", "water_movement_efficiency", "oxygen_bonus",
                "sneaking_speed", "sweeping_damage_ratio", "movement_speed")) {
            Holder<Attribute> holder = attributeHolder(path);
            float normal = EnchantValues.attribute(enchantment, holder, 1);
            float located = EnchantValues.locationAttribute(enchantment, holder, 1);
            if (Math.abs(normal) < 0.0001F && Math.abs(located) < 0.0001F) {
                continue;
            }
            DescriptionFile.ValueSpec spec = new DescriptionFile.ValueSpec();
            spec.attribute = "minecraft:" + path;
            spec.scale = switch (path) {
                case "movement_speed" -> 1000.0F;
                case "water_movement_efficiency", "sweeping_damage_ratio", "sneaking_speed" -> 100.0F;
                case "oxygen_bonus" -> 15.0F;
                default -> 1.0F;
            };
            values.add(spec);
        }
        for (Extra extra : extras) {
            DescriptionFile.ValueSpec spec = new DescriptionFile.ValueSpec();
            spec.source = extra.key();
            spec.scale = extra.scale();
            values.add(spec);
        }
        return values;
    }

    public static Path export(Enchantment enchantment, ResourceLocation id) throws IOException {
        DescriptionFile file = new DescriptionFile();
        file.enchantment = id.toString();
        file.mode = "replace";
        for (DescriptionFile.ValueSpec spec : detectValues(enchantment, id)) {
            DescriptionFile.Line line = new DescriptionFile.Line();
            if (spec.source != null) {
                line.type = "source";
                line.source = spec.source;
                line.scale = spec.scale;
            } else if (spec.component != null) {
                line.type = "value";
                line.component = spec.component;
            } else {
                line.type = "attribute";
                line.attribute = spec.attribute;
                line.scale = spec.scale;
            }
            line.format = "ded.line.value";
            file.lines.add(line);
        }
        Path dir = FMLPaths.CONFIGDIR.get().resolve(DetailedEnchantmentDescriptionsNeoForge.MOD_ID)
                .resolve("enchantments");
        Files.createDirectories(dir);
        Path out = dir.resolve(id.getNamespace() + "_" + id.getPath() + ".json");
        Files.writeString(out, GSON.create().toJson(file), StandardCharsets.UTF_8);
        return out;
    }

    // -- helpers used by the EXTRAS table (same formulas as Describer) --

    private static float unbreaking(Enchantment enchantment, int level, ItemStack stack) {
        List<ConditionalEffect<EnchantmentValueEffect>> entries =
                enchantment.getEffects(EnchantmentEffectComponents.ITEM_DAMAGE);
        if (entries.isEmpty()) {
            return 0.0F;
        }
        boolean armor = stack.getItem() instanceof ArmorItem;
        int index = Math.min(armor ? 0 : 1, entries.size() - 1);
        return entries.get(index).effect() instanceof net.minecraft.world.item.enchantment.effects.RemoveBinomial binomial
                ? binomial.chance().calculate(level) : 0.0F;
    }

    private static float igniteSeconds(Enchantment enchantment, int level) {
        for (var effect : EnchantValues.postAttackEffects(enchantment)) {
            if (effect instanceof net.minecraft.world.item.enchantment.effects.Ignite ignite) {
                return ignite.duration().calculate(level);
            }
        }
        return 0.0F;
    }

    private static float frostRadius(Enchantment enchantment, int level) {
        for (var effect : EnchantValues.locationEffects(enchantment)) {
            if (effect instanceof net.minecraft.world.item.enchantment.effects.ReplaceDisk disk) {
                return disk.radius().calculate(level);
            }
        }
        return 0.0F;
    }

    private static float windBurst(Enchantment enchantment, int level) {
        for (var effect : EnchantValues.postAttackEffects(enchantment)) {
            if (effect instanceof net.minecraft.world.item.enchantment.effects.ExplodeEffect explode) {
                return explode.knockbackMultiplier().map(value -> value.calculate(level)).orElse(0.0F);
            }
        }
        return 0.0F;
    }
}
