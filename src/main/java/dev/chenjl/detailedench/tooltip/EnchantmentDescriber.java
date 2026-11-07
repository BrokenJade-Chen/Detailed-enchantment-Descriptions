package dev.chenjl.detailedench.tooltip;

import dev.chenjl.detailedench.config.ModConfig;
import dev.chenjl.detailedench.data.DescriptionFile;
import dev.chenjl.detailedench.data.DescriptionRepository;
import dev.chenjl.detailedench.data.LineRenderer;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.enchantment.effect.entity.ExplodeEnchantmentEffect;
import net.minecraft.enchantment.effect.entity.IgniteEnchantmentEffect;
import net.minecraft.enchantment.effect.entity.ReplaceDiskEnchantmentEffect;
import net.minecraft.enchantment.effect.value.RemoveBinomialEnchantmentEffect;
import dev.chenjl.detailedench.compat.Compat;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.component.EnchantmentEffectComponentTypes.*;
import static dev.chenjl.detailedench.compat.Attrs.*;

/**
 * Turns an enchantment + level into the tooltip lines shown under its name.
 *
 * <p>Numbers come from {@link EnchantmentValues}, which evaluates the enchantment's data. A few
 * values are constants of the vanilla code rather than of the enchantment data (for example the
 * projectile fire duration); those are called out where they are used.</p>
 */
public final class EnchantmentDescriber {
    private static final Map<Identifier, LineProvider> RULES = new HashMap<>();

    static {
        registerDefaults();
    }

    private EnchantmentDescriber() {
    }

    /**
     * @return the description lines for this enchantment: a JSON override when one exists, otherwise
     *         the built-in rule, otherwise the generic fallback for modded enchantments
     */
    public static List<Text> describe(Enchantment enchantment, Identifier id, int level, ItemStack stack) {
        DescriptionFile override = DescriptionRepository.get(id);
        if (override != null && override.disable) {
            return List.of();
        }

        List<Text> base = builtin(enchantment, id, level, stack);
        if (override == null) {
            return base;
        }
        List<Text> custom = LineRenderer.render(override, enchantment, level);
        if (!override.isAppend()) {
            return custom;
        }
        List<Text> merged = new java.util.ArrayList<>(base);
        merged.addAll(custom);
        return merged;
    }

    /** @return true when a hand written rule exists (used by {@code /ded dump}) */
    public static boolean hasBuiltin(Identifier id) {
        return RULES.containsKey(id);
    }

    private static List<Text> builtin(Enchantment enchantment, Identifier id, int level, ItemStack stack) {
        LineProvider rule = RULES.get(id);
        if (rule != null) {
            return rule.lines(enchantment, level, stack);
        }
        return GenericDescriber.lines(enchantment, level);
    }

    private static void registerDefaults() {
        // ---- damage ----
        register("sharpness", (ench, lv, stack) -> line("sharpness", Numbers.coloured(EnchantmentValues.sum(ench, DAMAGE, lv))));
        register("smite", (ench, lv, stack) -> line("smite", Numbers.coloured(EnchantmentValues.sum(ench, DAMAGE, lv))));
        register("bane_of_arthropods", (ench, lv, stack) -> line("bane_of_arthropods", Numbers.coloured(EnchantmentValues.sum(ench, DAMAGE, lv))));
        register("impaling", (ench, lv, stack) -> line("impaling", Numbers.coloured(EnchantmentValues.sum(ench, DAMAGE, lv))));
        register("power", (ench, lv, stack) -> line("power", Numbers.coloured(EnchantmentValues.sum(ench, DAMAGE, lv))));
        register("knockback", (ench, lv, stack) -> line("knockback", Numbers.coloured(EnchantmentValues.sum(ench, KNOCKBACK, lv))));
        register("punch", (ench, lv, stack) -> line("punch", Numbers.coloured(EnchantmentValues.sum(ench, KNOCKBACK, lv))));
        register("piercing", (ench, lv, stack) -> line("piercing", Numbers.coloured(EnchantmentValues.sum(ench, PROJECTILE_PIERCING, lv))));
        register("density", (ench, lv, stack) -> line("density", Numbers.coloured(EnchantmentValues.sum(ench, SMASH_DAMAGE_PER_FALLEN_BLOCK, lv))));
        register("breach", (ench, lv, stack) -> line("breach", percentOf(Math.abs(EnchantmentValues.sum(ench, ARMOR_EFFECTIVENESS, lv)))));

        // ---- protection: every point of damage_protection is 4% (DamageUtil#getInflictedDamage, capped at 20) ----
        register("protection", (ench, lv, stack) -> line("protection", percentPoints(EnchantmentValues.sum(ench, DAMAGE_PROTECTION, lv) * 4.0F)));
        register("fire_protection", (ench, lv, stack) -> line("fire_protection",
                percentPoints(EnchantmentValues.sum(ench, DAMAGE_PROTECTION, lv) * 4.0F),
                percentOf(Math.abs(EnchantmentValues.attribute(ench, BURNING_TIME, lv)))));
        register("blast_protection", (ench, lv, stack) -> line("blast_protection",
                percentPoints(EnchantmentValues.sum(ench, DAMAGE_PROTECTION, lv) * 4.0F),
                percentOf(EnchantmentValues.attribute(ench, EXPLOSION_KNOCKBACK_RESISTANCE, lv))));
        register("projectile_protection", (ench, lv, stack) -> line("projectile_protection", percentPoints(EnchantmentValues.sum(ench, DAMAGE_PROTECTION, lv) * 4.0F)));
        register("feather_falling", (ench, lv, stack) -> line("feather_falling", percentPoints(EnchantmentValues.sum(ench, DAMAGE_PROTECTION, lv) * 4.0F)));

        // ---- attribute driven ----
        register("efficiency", (ench, lv, stack) -> line("efficiency", Numbers.coloured(EnchantmentValues.attribute(ench, MINING_EFFICIENCY, lv))));
        register("aqua_affinity", (ench, lv, stack) -> staticLine("aqua_affinity"));
        register("depth_strider", (ench, lv, stack) -> line("depth_strider", percentOf(EnchantmentValues.attribute(ench, WATER_MOVEMENT_EFFICIENCY, lv))));
        // oxygen bonus is a multiplier of the breathing time: value N means (N + 1) x 15s, i.e. +15s per level
        register("respiration", (ench, lv, stack) -> line("respiration", Numbers.coloured(EnchantmentValues.attribute(ench, OXYGEN_BONUS, lv) * 15.0F)));
        // sneaking_speed defaults to 0.3, so the absolute value is the fraction of walking speed
        register("swift_sneak", (ench, lv, stack) -> line("swift_sneak", percentOf(0.3F + EnchantmentValues.attribute(ench, SNEAKING_SPEED, lv))));
        register("sweeping_edge", (ench, lv, stack) -> line("sweeping_edge", percentOf(EnchantmentValues.attribute(ench, SWEEPING_DAMAGE_RATIO, lv))));
        register("soul_speed", (ench, lv, stack) -> line("soul_speed",
                // The attribute adds 0.0405 + 0.0105*(level-1) to the 0.1 base speed, so multiply by
                // 10 to express it as the percentage shown on the wiki (40.5% / 51% / 61.5%).
                percentOf(EnchantmentValues.locationAttribute(ench, MOVEMENT_SPEED, lv) * 10.0F),
                percentOf(0.04F * lv))); // durability cost chance is part of the random_chance condition

        // ---- probability / triggers ----
        register("unbreaking", (ench, lv, stack) -> line("unbreaking", percentOf(unbreakingChance(ench, lv, stack))));
        register("thorns", (ench, lv, stack) -> line("thorns", percentOf(0.15F * lv))); // chance lives in the loot condition
        register("fire_aspect", (ench, lv, stack) -> line("fire_aspect", Numbers.coloured(igniteSeconds(ench, lv))));
        // the arrow sets the victim on fire for a hard-coded 5s (PersistentProjectileEntity#onEntityHit)
        register("flame", (ench, lv, stack) -> staticLine("flame"));
        register("frost_walker", (ench, lv, stack) -> line("frost_walker", Numbers.coloured(frostRadius(ench, lv))));
        register("wind_burst", (ench, lv, stack) -> line("wind_burst", Numbers.coloured(windBurstKnockback(ench, lv))));
        register("lure", (ench, lv, stack) -> line("lure", Numbers.coloured(EnchantmentValues.sum(ench, FISHING_TIME_REDUCTION, lv))));
        register("luck_of_the_sea", (ench, lv, stack) -> line("luck_of_the_sea", Numbers.coloured(EnchantmentValues.sum(ench, FISHING_LUCK_BONUS, lv))));
        register("quick_charge", (ench, lv, stack) -> line("quick_charge", Numbers.coloured(Math.abs(EnchantmentValues.singleComponent(ench, CROSSBOW_CHARGE_TIME, lv)))));
        register("multishot", (ench, lv, stack) -> line("multishot", Numbers.coloured(1.0F + EnchantmentValues.sum(ench, PROJECTILE_COUNT, lv))));
        register("loyalty", (ench, lv, stack) -> line("loyalty", Numbers.coloured(EnchantmentValues.sum(ench, TRIDENT_RETURN_ACCELERATION, lv))));
        register("riptide", (ench, lv, stack) -> line("riptide", Numbers.coloured(EnchantmentValues.singleComponent(ench, TRIDENT_SPIN_ATTACK_STRENGTH, lv))));
        register("mending", (ench, lv, stack) -> staticLine("mending"));
        register("looting", (ench, lv, stack) -> line("looting",
                Numbers.coloured(lv),
                percentOf(EnchantmentValues.sumTargeted(ench, EQUIPMENT_DROPS, lv))));

        // ---- description only ----
        register("binding_curse", (ench, lv, stack) -> staticLine("binding_curse"));
        register("vanishing_curse", (ench, lv, stack) -> staticLine("vanishing_curse"));
        register("channeling", (ench, lv, stack) -> staticLine("channeling"));
        register("fortune", (ench, lv, stack) -> staticLine("fortune"));
        register("infinity", (ench, lv, stack) -> staticLine("infinity"));
        register("silk_touch", (ench, lv, stack) -> staticLine("silk_touch"));
    }

    private static void register(String path, LineProvider rule) {
        RULES.put(Identifier.of("minecraft", path), rule);
    }

    private static List<Text> line(String id, Object... arguments) {
        if (!ModConfig.get().showDescriptions) {
            return List.of();
        }
        if (ModConfig.get().showNumbers) {
            return List.of(Text.translatable("ded.desc." + id, arguments).styled(style -> style.withColor(ModConfig.get().descriptionColorRgb)));
        }
        return List.of(Text.translatable("ded.desc." + id + ".plain").styled(style -> style.withColor(ModConfig.get().descriptionColorRgb)));
    }

    private static List<Text> staticLine(String id) {
        if (!ModConfig.get().showDescriptions) {
            return List.of();
        }
        return List.of(Text.translatable("ded.desc." + id).styled(style -> style.withColor(ModConfig.get().descriptionColorRgb)));
    }

    /** For values that are already expressed in percent points (e.g. 16 -> "16%"). */
    private static Text percentPoints(float percent) {
        return Numbers.coloured(Numbers.of(percent) + "%");
    }

    /** For ratios (e.g. 0.16 -> "16%"). */
    private static Text percentOf(float ratio) {
        return Numbers.coloured(Numbers.percent(ratio) + "%");
    }

    /** Reads an attribute amount from {@code location_changed} effects (used by Soul Speed). */
    public static float unbreakingChance(Enchantment enchantment, int level, ItemStack stack) {
        List<EnchantmentEffectEntry<EnchantmentValueEffect>> entries = enchantment.getEffect(ITEM_DAMAGE);
        if (entries.isEmpty()) {
            return 0.0F;
        }
        // vanilla holds two entries: armour first, everything else second
        boolean armor = Compat.isArmor(stack);
        int index = Math.min(armor ? 0 : 1, entries.size() - 1);
        EnchantmentValueEffect effect = entries.get(index).effect();
        if (effect instanceof RemoveBinomialEnchantmentEffect removeBinomial) {
            return removeBinomial.chance().getValue(level);
        }
        return 0.0F;
    }

    public static float igniteSeconds(Enchantment enchantment, int level) {
        for (EnchantmentEntityEffect effect : EnchantmentValues.targetedEntityEffects(enchantment, POST_ATTACK)) {
            if (effect instanceof IgniteEnchantmentEffect ignite) {
                return ignite.duration().getValue(level);
            }
        }
        return 0.0F;
    }

    public static float frostRadius(Enchantment enchantment, int level) {
        for (EnchantmentLocationBasedEffect effect : EnchantmentValues.locationEffects(enchantment, LOCATION_CHANGED)) {
            if (effect instanceof ReplaceDiskEnchantmentEffect replaceDisk) {
                return replaceDisk.radius().getValue(level);
            }
        }
        return 0.0F;
    }

    public static float windBurstKnockback(Enchantment enchantment, int level) {
        for (EnchantmentEntityEffect effect : EnchantmentValues.targetedEntityEffects(enchantment, POST_ATTACK)) {
            if (effect instanceof ExplodeEnchantmentEffect explode) {
                return explode.knockbackMultiplier().map(value -> value.getValue(level)).orElse(0.0F);
            }
        }
        return 0.0F;
    }
}

