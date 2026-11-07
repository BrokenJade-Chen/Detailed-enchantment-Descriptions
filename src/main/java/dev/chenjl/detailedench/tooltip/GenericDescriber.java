package dev.chenjl.detailedench.tooltip;

import dev.chenjl.detailedench.config.ModConfig;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.component.EnchantmentEffectComponentTypes.*;

/**
 * Fallback descriptions for enchantments we have no hand written rule for (typically modded ones).
 *
 * <p>It only understands the standard value components, which covers most 1.21+ enchantments; a
 * modpack can supply exact wording through a JSON file instead (see {@code docs/EXTENDING.md}).</p>
 */
public final class GenericDescriber {
    private record Target(ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> type,
                          String formatKey,
                          float scale) {
    }

    private static final List<Target> TARGETS = List.of(
            new Target(DAMAGE, "ded.generic.damage", 1.0F),
            new Target(DAMAGE_PROTECTION, "ded.generic.protection", 4.0F),
            new Target(KNOCKBACK, "ded.generic.knockback", 1.0F),
            new Target(PROJECTILE_PIERCING, "ded.generic.piercing", 1.0F),
            new Target(PROJECTILE_COUNT, "ded.generic.projectiles", 1.0F),
            new Target(ARMOR_EFFECTIVENESS, "ded.generic.armor_effectiveness", 100.0F));

    private GenericDescriber() {
    }

    public static List<Text> lines(Enchantment enchantment, int level) {
        List<Text> out = new ArrayList<>();
        for (Target target : TARGETS) {
            float value = EnchantmentValues.sum(enchantment, target.type(), level) * target.scale();
            if (Math.abs(value) < 0.0001F) {
                continue;
            }
            out.add(Text.translatable(target.formatKey(), Numbers.coloured(value))
                    .styled(style -> style.withColor(ModConfig.get().descriptionColorRgb)));
        }
        return out;
    }
}
