package dev.chenjl.detailedench.compat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

/** 1.21.4 override: tooltip hide flags are NBT based and textures use RenderLayer. */
public final class Compat {
    private Compat() {
    }

    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, int size) {
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, 0.0F, 0.0F, size, size, size, size);
    }

    public static boolean hidesEnchantments(ItemStack stack) {
        return false;
    }

    public static boolean isArmor(ItemStack stack) {
        return stack.isIn(ItemTags.HEAD_ARMOR) || stack.isIn(ItemTags.CHEST_ARMOR)
                || stack.isIn(ItemTags.LEG_ARMOR) || stack.isIn(ItemTags.FOOT_ARMOR);
    }
}
