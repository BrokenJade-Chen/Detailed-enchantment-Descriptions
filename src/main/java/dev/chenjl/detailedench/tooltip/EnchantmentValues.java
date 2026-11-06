package dev.chenjl.detailedench.tooltip;

import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.AllOfEnchantmentEffects;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.enchantment.effect.TargetedEnchantmentEffect;
import net.minecraft.enchantment.effect.value.AddEnchantmentEffect;
import net.minecraft.enchantment.effect.value.MultiplyEnchantmentEffect;
import net.minecraft.enchantment.effect.value.RemoveBinomialEnchantmentEffect;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the numbers straight out of an enchantment's data-driven {@code effects}.
 *
 * <p>Since 1.21 enchantment behaviour lives in JSON ({@code data/<namespace>/enchantment/*.json}),
 * so the values shown in a tooltip can be evaluated with the exact same formulas the game uses
 * instead of being hard-coded per enchantment.</p>
 */
public final class EnchantmentValues {
    private static final Random SHARED_RANDOM = Random.create(0L);

    private EnchantmentValues() {
    }

    /** Sums every {@code add} style value effect of the given component. */
    public static float sum(Enchantment enchantment,
                            ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> type,
                            int level) {
        float total = 0.0F;
        for (EnchantmentEffectEntry<EnchantmentValueEffect> entry : enchantment.getEffect(type)) {
            total += evaluate(entry.effect(), level);
        }
        return total;
    }

    /** Sums every {@code add} style value effect of a targeted component (e.g. {@code equipment_drops}). */
    public static float sumTargeted(Enchantment enchantment,
                                    ComponentType<List<TargetedEnchantmentEffect<EnchantmentValueEffect>>> type,
                                    int level) {
        float total = 0.0F;
        for (TargetedEnchantmentEffect<EnchantmentValueEffect> entry : enchantment.getEffect(type)) {
            total += evaluate(entry.effect(), level);
        }
        return total;
    }

    /** Evaluates a single value effect (used by the components that are not wrapped in a list). */
    public static float single(EnchantmentValueEffect effect, int level) {
        return evaluate(effect, level);
    }

    /**
     * Reads a component that holds a single (not list-wrapped) value effect, e.g.
     * {@code crossbow_charge_time} or {@code trident_spin_attack_strength}.
     */
    public static float singleComponent(Enchantment enchantment,
                                        ComponentType<EnchantmentValueEffect> type,
                                        int level) {
        MutableFloat value = new MutableFloat(0.0F);
        enchantment.modifyValue(type, SHARED_RANDOM, level, value);
        return value.floatValue();
    }

    /** Sums the amount of every attribute modifier matching {@code attribute}. */
    public static float attribute(Enchantment enchantment,
                                  RegistryEntry<EntityAttribute> attribute,
                                  int level) {
        float total = 0.0F;
        for (AttributeEnchantmentEffect effect : enchantment.getEffect(
                net.minecraft.component.EnchantmentEffectComponentTypes.ATTRIBUTES)) {
            if (effect.attribute().value() == attribute.value()) {
                total += effect.amount().getValue(level);
            }
        }
        return total;
    }

    /** All entity effects of a component, with {@code all_of} wrappers flattened. */
    public static List<EnchantmentEntityEffect> entityEffects(
            Enchantment enchantment,
            ComponentType<List<EnchantmentEffectEntry<EnchantmentEntityEffect>>> type) {
        List<EnchantmentEntityEffect> result = new ArrayList<>();
        for (EnchantmentEffectEntry<EnchantmentEntityEffect> entry : enchantment.getEffect(type)) {
            flatten(entry.effect(), result);
        }
        return result;
    }

    /** All targeted entity effects of a component, with {@code all_of} wrappers flattened. */
    public static List<EnchantmentEntityEffect> targetedEntityEffects(
            Enchantment enchantment,
            ComponentType<List<TargetedEnchantmentEffect<EnchantmentEntityEffect>>> type) {
        List<EnchantmentEntityEffect> result = new ArrayList<>();
        for (TargetedEnchantmentEffect<EnchantmentEntityEffect> entry : enchantment.getEffect(type)) {
            flatten(entry.effect(), result);
        }
        return result;
    }

    /** All location based effects of a component, with {@code all_of} wrappers flattened. */
    public static List<EnchantmentLocationBasedEffect> locationEffects(
            Enchantment enchantment,
            ComponentType<List<EnchantmentEffectEntry<EnchantmentLocationBasedEffect>>> type) {
        List<EnchantmentLocationBasedEffect> result = new ArrayList<>();
        for (EnchantmentEffectEntry<EnchantmentLocationBasedEffect> entry : enchantment.getEffect(type)) {
            flattenLocation(entry.effect(), result);
        }
        return result;
    }

    private static float evaluate(EnchantmentValueEffect effect, int level) {
        if (effect instanceof AddEnchantmentEffect add) {
            return add.value().getValue(level);
        }
        if (effect instanceof MultiplyEnchantmentEffect multiply) {
            return multiply.factor().getValue(level);
        }
        if (effect instanceof RemoveBinomialEnchantmentEffect removeBinomial) {
            return removeBinomial.chance().getValue(level);
        }
        if (effect instanceof AllOfEnchantmentEffects.ValueEffects allOf) {
            float total = 0.0F;
            for (EnchantmentValueEffect child : allOf.effects()) {
                total += evaluate(child, level);
            }
            return total;
        }
        // Fallback: run the effect with a neutral input so custom effects still produce something.
        return effect.apply(level, SHARED_RANDOM, 0.0F);
    }

    private static void flatten(EnchantmentEntityEffect effect, List<EnchantmentEntityEffect> out) {
        if (effect instanceof AllOfEnchantmentEffects.EntityEffects allOf) {
            for (EnchantmentEntityEffect child : allOf.effects()) {
                flatten(child, out);
            }
            return;
        }
        out.add(effect);
    }

    private static void flattenLocation(EnchantmentLocationBasedEffect effect,
                                        List<EnchantmentLocationBasedEffect> out) {
        if (effect instanceof AllOfEnchantmentEffects.LocationBasedEffects allOf) {
            for (EnchantmentLocationBasedEffect child : allOf.effects()) {
                flattenLocation(child, out);
            }
            return;
        }
        out.add(effect);
    }
}
