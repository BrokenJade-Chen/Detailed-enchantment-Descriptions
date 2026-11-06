package dev.chenjl.detailedench.tooltip;

import dev.chenjl.detailedench.config.ModConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

import java.util.List;

/**
 * Adds one description line under every enchantment name in an item tooltip.
 *
 * <p>Lines are inserted right after the matching name line (instead of being appended at the end)
 * so the result reads the same way as the description mods this replaces.</p>
 */
public final class EnchantmentTooltipAppender implements ItemTooltipCallback {
    private static final String VANILLA_NAMESPACE = "minecraft";

    @Override
    public void getTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType type, List<Text> lines) {
        ModConfig config = ModConfig.get();
        if (!config.enabled) {
            return;
        }

        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) {
            return;
        }
        if (!config.showEnchantedBook && stack.contains(DataComponentTypes.STORED_ENCHANTMENTS)) {
            return;
        }
        if (hidesEnchantments(stack)) {
            return;
        }

        for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
            Identifier id = entry.getKey().map(RegistryKey::getValue).orElse(null);
            if (id == null) {
                continue;
            }
            if (!config.includeModded && !VANILLA_NAMESPACE.equals(id.getNamespace())) {
                continue;
            }

            int level = enchantments.getLevel(entry);
            List<Text> description = EnchantmentDescriber.describe(entry.value(), id, level, stack);
            if (description.isEmpty()) {
                continue;
            }

            int nameLine = findNameLine(lines, entry, level);
            if (nameLine >= 0) {
                lines.addAll(nameLine + 1, description);
            } else {
                lines.addAll(description);
            }
        }
    }

    /** Respects the vanilla "hide enchantments" tooltip flag. */
    private static boolean hidesEnchantments(ItemStack stack) {
        TooltipDisplayComponent display = stack.get(DataComponentTypes.TOOLTIP_DISPLAY);
        if (display == null) {
            return false;
        }
        return display.hiddenComponents().contains(DataComponentTypes.ENCHANTMENTS)
                || display.hiddenComponents().contains(DataComponentTypes.STORED_ENCHANTMENTS);
    }

    /** @return the index of the tooltip line that shows this enchantment's name, or -1 */
    private static int findNameLine(List<Text> lines, RegistryEntry<Enchantment> entry, int level) {
        String name = Enchantment.getName(entry, level).getString();
        for (int i = 0; i < lines.size(); i++) {
            if (name.equals(lines.get(i).getString())) {
                return i;
            }
        }
        return -1;
    }
}
