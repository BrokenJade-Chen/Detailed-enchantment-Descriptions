package dev.chenjl.detailedench.neoforge;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.ArrayList;
import java.util.List;

/** Reads the numbers straight out of the enchantment's data components. */
public final class EnchantValues {
    private static final RandomSource RANDOM = RandomSource.create(42L);

    private EnchantValues() {
    }

    /** Sums every value effect of a list component at the given level. */
    public static float sum(Enchantment enchantment,
                            DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type,
                            int level) {
        float value = 0.0F;
        for (ConditionalEffect<EnchantmentValueEffect> conditional : enchantment.getEffects(type)) {
            value = conditional.effect().process(level, RANDOM, value);
        }
        return value;
    }

    /** Sums a component that stores targeted effects (e.g. equipment drops). */
    public static float sumTargeted(Enchantment enchantment,
                                    DataComponentType<List<TargetedConditionalEffect<EnchantmentValueEffect>>> type,
                                    int level) {
        float value = 0.0F;
        for (TargetedConditionalEffect<EnchantmentValueEffect> targeted : enchantment.getEffects(type)) {
            value = targeted.effect().process(level, RANDOM, value);
        }
        return value;
    }

    /** A single effect component (crossbow charge time, riptide strength). */
    public static float singleComponent(Enchantment enchantment,
                                        DataComponentType<EnchantmentValueEffect> type,
                                        int level) {
        MutableFloat value = new MutableFloat(0.0F);
        enchantment.modifyUnfilteredValue(type, RANDOM, level, value);
        return value.floatValue();
    }

    /** Attribute bonus declared in the {@code attributes} component. */
    public static float attribute(Enchantment enchantment, Holder<Attribute> attribute, int level) {
        for (EnchantmentAttributeEffect effect : enchantment.getEffects(EnchantmentEffectComponents.ATTRIBUTES)) {
            if (effect.attribute().value() == attribute.value()) {
                return effect.amount().calculate(level);
            }
        }
        return 0.0F;
    }

    /** Attribute bonus applied by {@code location_changed} effects (Soul Speed and friends). */
    public static float locationAttribute(Enchantment enchantment, Holder<Attribute> attribute, int level) {
        for (ConditionalEffect<EnchantmentLocationBasedEffect> conditional
                : enchantment.getEffects(EnchantmentEffectComponents.LOCATION_CHANGED)) {
            float found = findLocationAttribute(conditional.effect(), attribute, level);
            if (found != 0.0F) {
                return found;
            }
        }
        return 0.0F;
    }

    /** Flattens {@code all_of} composites while looking for an attribute effect. */
    private static float findLocationAttribute(EnchantmentLocationBasedEffect effect,
                                               Holder<Attribute> attribute, int level) {
        if (effect instanceof EnchantmentAttributeEffect attributeEffect
                && attributeEffect.attribute().value() == attribute.value()) {
            return attributeEffect.amount().calculate(level);
        }
        if (effect instanceof AllOf.LocationBasedEffects allOf) {
            for (EnchantmentLocationBasedEffect nested : allOf.effects()) {
                float found = findLocationAttribute(nested, attribute, level);
                if (found != 0.0F) {
                    return found;
                }
            }
        }
        return 0.0F;
    }

    /** Flattens {@code location_changed} into plain effects. */
    public static List<EnchantmentLocationBasedEffect> locationEffects(Enchantment enchantment) {
        List<EnchantmentLocationBasedEffect> out = new ArrayList<>();
        for (ConditionalEffect<EnchantmentLocationBasedEffect> conditional
                : enchantment.getEffects(EnchantmentEffectComponents.LOCATION_CHANGED)) {
            collectLocation(conditional.effect(), out);
        }
        return out;
    }

    private static void collectLocation(EnchantmentLocationBasedEffect effect,
                                        List<EnchantmentLocationBasedEffect> out) {
        if (effect instanceof AllOf.LocationBasedEffects allOf) {
            for (EnchantmentLocationBasedEffect nested : allOf.effects()) {
                collectLocation(nested, out);
            }
        } else {
            out.add(effect);
        }
    }

    /** Flattens {@code post_attack} into plain entity effects. */
    public static List<EnchantmentEntityEffect> postAttackEffects(Enchantment enchantment) {
        List<EnchantmentEntityEffect> out = new ArrayList<>();
        for (TargetedConditionalEffect<EnchantmentEntityEffect> targeted
                : enchantment.getEffects(EnchantmentEffectComponents.POST_ATTACK)) {
            collectEntity(targeted.effect(), out);
        }
        return out;
    }

    private static void collectEntity(EnchantmentEntityEffect effect, List<EnchantmentEntityEffect> out) {
        if (effect instanceof AllOf.EntityEffects allOf) {
            for (EnchantmentEntityEffect nested : allOf.effects()) {
                collectEntity(nested, out);
            }
        } else {
            out.add(effect);
        }
    }
}
