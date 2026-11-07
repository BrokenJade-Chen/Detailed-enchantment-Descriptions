package dev.chenjl.detailedench.compat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

/**
 * Version override for 1.21.1 – 1.21.4: no render pipelines yet, and the tooltip display component
 * does not exist (hidden tooltips are handled by NBT flags on those versions).
 */
public final class Compat {
    private Compat() {
    }

    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, int size) {
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, 0.0F, 0.0F, size, size, size, size);
    }

    public static boolean hidesEnchantments(ItemStack stack) {
        // 1.21.1 - 1.21.4 store the "hide enchantments" flag as a component
        return stack.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
    }

    public static boolean isArmor(ItemStack stack) {
        return stack.isIn(ItemTags.HEAD_ARMOR) || stack.isIn(ItemTags.CHEST_ARMOR)
                || stack.isIn(ItemTags.LEG_ARMOR) || stack.isIn(ItemTags.FOOT_ARMOR);
    }
}
