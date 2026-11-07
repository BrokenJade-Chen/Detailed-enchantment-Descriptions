package dev.chenjl.detailedench.compat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

/** 1.21.1 override: no render layer parameter on drawTexture, no tooltip display component. */
public final class Compat {
    private Compat() {
    }

    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, int size) {
        context.drawTexture(texture, x, y, 0.0F, 0.0F, size, size, size, size);
    }

    public static boolean hidesEnchantments(ItemStack stack) {
        return false;
    }

    public static boolean isArmor(ItemStack stack) {
        return stack.isIn(ItemTags.HEAD_ARMOR) || stack.isIn(ItemTags.CHEST_ARMOR)
                || stack.isIn(ItemTags.LEG_ARMOR) || stack.isIn(ItemTags.FOOT_ARMOR);
    }
}
