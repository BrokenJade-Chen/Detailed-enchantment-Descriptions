package dev.chenjl.detailedench.neoforge;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.effects.ExplodeEffect;
import net.minecraft.world.item.enchantment.effects.Ignite;
import net.minecraft.world.item.enchantment.effects.RemoveBinomial;
import net.minecraft.world.item.enchantment.effects.ReplaceDisk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The description rules, one per vanilla enchantment. */
public final class Describer {
    private Describer() {
    }

    public static List<Component> lines(Holder<Enchantment> holder, int level, ItemStack stack) {
        Enchantment enchantment = holder.value();
        var enchantmentKey = holder.unwrapKey().map(key -> key.location()).orElse(null);
        DescriptionFile override = enchantmentKey == null ? null : DescriptionRepository.get(enchantmentKey);
        if (override != null && override.disable) {
            return List.of();
        }
        List<Component> base = builtin(holder, level, stack);
        if (override == null) {
            return base;
        }
        List<Component> custom = LineRenderer.render(override, enchantment, level);
        if (!override.isAppend()) {
            return custom;
        }
        List<Component> merged = new ArrayList<>(base);
        merged.addAll(custom);
        return merged;
    }

    private static List<Component> builtin(Holder<Enchantment> holder, int level, ItemStack stack) {
        Enchantment enchantment = holder.value();
        String path = holder.unwrapKey().map(key -> key.location().getPath()).orElse("");
        List<Component> out = new ArrayList<>();
        switch (path) {
            // ---- damage ----
            case "sharpness", "smite", "bane_of_arthropods", "impaling", "power" ->
                    add(out, path, number(EnchantValues.sum(enchantment, EnchantmentEffectComponents.DAMAGE, level)));
            case "knockback", "punch" ->
                    add(out, path, number(EnchantValues.sum(enchantment, EnchantmentEffectComponents.KNOCKBACK, level)));
            case "piercing" -> add(out, path, number(EnchantValues.sum(enchantment,
                    EnchantmentEffectComponents.PROJECTILE_PIERCING, level)));
            case "density" -> add(out, path, number(EnchantValues.sum(enchantment,
                    EnchantmentEffectComponents.SMASH_DAMAGE_PER_FALLEN_BLOCK, level)));
            case "breach" -> add(out, path, percentOf(Math.abs(EnchantValues.sum(enchantment,
                    EnchantmentEffectComponents.ARMOR_EFFECTIVENESS, level))));

            // ---- protection: every point of damage_protection is 4% ----
            case "protection", "projectile_protection", "feather_falling" ->
                    add(out, path, percentPoints(EnchantValues.sum(enchantment,
                            EnchantmentEffectComponents.DAMAGE_PROTECTION, level) * 4.0F));
            case "fire_protection" -> add(out, path,
                    percentPoints(EnchantValues.sum(enchantment,
                            EnchantmentEffectComponents.DAMAGE_PROTECTION, level) * 4.0F),
                    percentOf(Math.abs(EnchantValues.attribute(enchantment, Attributes.BURNING_TIME, level))));
            case "blast_protection" -> add(out, path,
                    percentPoints(EnchantValues.sum(enchantment,
                            EnchantmentEffectComponents.DAMAGE_PROTECTION, level) * 4.0F),
                    percentOf(EnchantValues.attribute(enchantment, Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, level)));

            // ---- attribute driven ----
            case "efficiency" -> add(out, path, number(EnchantValues.attribute(enchantment,
                    Attributes.MINING_EFFICIENCY, level)));
            case "aqua_affinity" -> add(out, path);
            case "depth_strider" -> add(out, path, percentOf(EnchantValues.attribute(enchantment,
                    Attributes.WATER_MOVEMENT_EFFICIENCY, level)));
            case "respiration" -> add(out, path, number(EnchantValues.attribute(enchantment,
                    Attributes.OXYGEN_BONUS, level) * 15.0F));
            case "swift_sneak" -> add(out, path, percentOf(0.3F + EnchantValues.attribute(enchantment,
                    Attributes.SNEAKING_SPEED, level)));
            case "sweeping_edge" -> add(out, path, percentOf(EnchantValues.attribute(enchantment,
                    Attributes.SWEEPING_DAMAGE_RATIO, level)));
            case "soul_speed" -> add(out, path,
                    percentOf(EnchantValues.locationAttribute(enchantment, Attributes.MOVEMENT_SPEED, level) * 10.0F),
                    percentOf(0.04F * level));

            // ---- probability / triggers ----
            case "unbreaking" -> add(out, path, percentOf(unbreakingChance(enchantment, level, stack)));
            case "thorns" -> add(out, path, percentOf(0.15F * level));
            case "fire_aspect" -> add(out, path, number(igniteSeconds(enchantment, level)));
            case "flame" -> add(out, path);
            case "frost_walker" -> add(out, path, number(frostRadius(enchantment, level)));
            case "wind_burst" -> add(out, path, number(windBurstKnockback(enchantment, level)));
            case "lure" -> add(out, path, number(Math.abs(EnchantValues.sum(enchantment,
                    EnchantmentEffectComponents.FISHING_TIME_REDUCTION, level))));
            case "luck_of_the_sea" -> add(out, path, number(EnchantValues.sum(enchantment,
                    EnchantmentEffectComponents.FISHING_LUCK_BONUS, level)));
            case "quick_charge" -> add(out, path, number(Math.abs(EnchantValues.singleComponent(enchantment,
                    EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME, level))));
            case "multishot" -> add(out, path, number(1.0F + EnchantValues.sum(enchantment,
                    EnchantmentEffectComponents.PROJECTILE_COUNT, level)));
            case "loyalty" -> add(out, path, number(EnchantValues.sum(enchantment,
                    EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION, level)));
            case "riptide" -> add(out, path, number(EnchantValues.singleComponent(enchantment,
                    EnchantmentEffectComponents.TRIDENT_SPIN_ATTACK_STRENGTH, level)));
            case "mending" -> add(out, path);
            case "looting" -> add(out, path, number(level), percentOf(EnchantValues.sumTargeted(enchantment,
                    EnchantmentEffectComponents.EQUIPMENT_DROPS, level)));

            // ---- description only ----
            case "binding_curse", "vanishing_curse", "channeling", "fortune", "infinity", "silk_touch" ->
                    add(out, path);
            default -> {
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ helpers

    private static float unbreakingChance(Enchantment enchantment, int level, ItemStack stack) {
        List<ConditionalEffect<EnchantmentValueEffect>> entries =
                enchantment.getEffects(EnchantmentEffectComponents.ITEM_DAMAGE);
        if (entries.isEmpty()) {
            return 0.0F;
        }
        boolean armor = stack.getItem() instanceof ArmorItem;
        int index = Math.min(armor ? 0 : 1, entries.size() - 1);
        if (entries.get(index).effect() instanceof RemoveBinomial binomial) {
            return binomial.chance().calculate(level);
        }
        return 0.0F;
    }

    private static float igniteSeconds(Enchantment enchantment, int level) {
        for (EnchantmentEntityEffect effect : EnchantValues.postAttackEffects(enchantment)) {
            if (effect instanceof Ignite ignite) {
                return ignite.duration().calculate(level);
            }
        }
        return 0.0F;
    }

    private static float frostRadius(Enchantment enchantment, int level) {
        for (EnchantmentLocationBasedEffect effect : EnchantValues.locationEffects(enchantment)) {
            if (effect instanceof ReplaceDisk disk) {
                return disk.radius().calculate(level);
            }
        }
        return 0.0F;
    }

    private static float windBurstKnockback(Enchantment enchantment, int level) {
        for (EnchantmentEntityEffect effect : EnchantValues.postAttackEffects(enchantment)) {
            if (effect instanceof ExplodeEffect explode) {
                return explode.knockbackMultiplier().map(value -> value.calculate(level)).orElse(0.0F);
            }
        }
        return 0.0F;
    }

    // ------------------------------------------------------------------ output

    private static void add(List<Component> out, String key, Component... args) {
        NeoConfig config = NeoConfig.get();
        if (!config.showDescriptions) {
            return;
        }
        MutableComponent line = (config.showNumbers && args.length > 0
                ? Component.translatable("ded.desc." + key, (Object[]) args)
                : Component.translatable(config.showNumbers ? "ded.desc." + key : "ded.desc." + key + ".plain"))
                .withStyle(Style.EMPTY.withColor(config.descriptionColorRgb));
        out.add(line);
    }

    private static Component number(float value) {
        NeoConfig config = NeoConfig.get();
        if (!config.showNumbers) {
            return Component.empty();
        }
        return Component.literal(format(value)).withStyle(Style.EMPTY.withColor(config.numberColorRgb));
    }

    /** Ratio (0.16) shown as a percentage. */
    private static Component percentOf(float ratio) {
        NeoConfig config = NeoConfig.get();
        if (!config.showNumbers) {
            return Component.empty();
        }
        return Component.literal(format(ratio * 100.0F) + "%")
                .withStyle(Style.EMPTY.withColor(config.numberColorRgb));
    }

    /** Value already expressed in percent points (16 -> "16%"). */
    private static Component percentPoints(float percent) {
        NeoConfig config = NeoConfig.get();
        if (!config.showNumbers) {
            return Component.empty();
        }
        return Component.literal(format(percent) + "%")
                .withStyle(Style.EMPTY.withColor(config.numberColorRgb));
    }

    static String format(float value) {
        if (Math.abs(value - Math.round(value)) < 0.0005F) {
            return Integer.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
