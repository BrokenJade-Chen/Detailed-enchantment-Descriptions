package dev.chenjl.detailedench.tooltip;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Produces the tooltip lines for one enchantment at a given level.
 *
 * <p>Built-in rules and user supplied JSON descriptions both go through this interface, so a JSON
 * file can override or extend any enchantment without code changes.</p>
 */
@FunctionalInterface
public interface LineProvider {
    List<Text> lines(Enchantment enchantment, int level, ItemStack stack);
}
