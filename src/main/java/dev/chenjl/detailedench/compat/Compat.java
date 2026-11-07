package dev.chenjl.detailedench.compat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

/**
 * Small version-specific helpers. The source in {@code src/main} targets 1.21.5+; older versions
 * get an override in {@code src/versions/<mcver>/java}.
 */
public final class Compat {
    private Compat() {
    }

    /** Draws a GUI texture; the 1.21.6+ pipeline API replaced the older RenderLayer one. */
    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, int size) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, size, size, size, size);
    }

    /** @return whether the stack hides its enchantments in the tooltip */
    public static boolean hidesEnchantments(ItemStack stack) {
        TooltipDisplayComponent display = stack.get(DataComponentTypes.TOOLTIP_DISPLAY);
        if (display == null) {
            return false;
        }
        return display.hiddenComponents().contains(DataComponentTypes.ENCHANTMENTS)
                || display.hiddenComponents().contains(DataComponentTypes.STORED_ENCHANTMENTS);
    }

    /** @return whether the stack is armour (item tags exist on every supported version) */
    public static boolean isArmor(ItemStack stack) {
        return stack.isIn(ItemTags.HEAD_ARMOR) || stack.isIn(ItemTags.CHEST_ARMOR)
                || stack.isIn(ItemTags.LEG_ARMOR) || stack.isIn(ItemTags.FOOT_ARMOR);
    }
}
