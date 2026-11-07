package dev.chenjl.detailedench;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** 1.21.11 override: key binding categories became objects, windows are passed directly. */
public final class Keybinds {
    public static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of(DetailedEnchantmentDescriptions.MOD_ID, "main"));

    public static final KeyBinding OPEN_CONFIG = register("open_config", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyBinding SHOW_DESCRIPTIONS = register("show_descriptions", GLFW.GLFW_KEY_LEFT_ALT);

    private Keybinds() {
    }

    private static KeyBinding register(String id, int defaultKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + DetailedEnchantmentDescriptions.MOD_ID + "." + id,
                InputUtil.Type.KEYSYM,
                defaultKey,
                CATEGORY));
    }

    public static void init() {
        // Forces class initialization so both bindings are registered.
    }

    public static boolean isShowHeld() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return false;
        }
        InputUtil.Key key = InputUtil.fromTranslationKey(SHOW_DESCRIPTIONS.getBoundKeyTranslationKey());
        if (key.getCategory() == InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(client.getWindow().getHandle(), key.getCode()) == GLFW.GLFW_PRESS;
        }
        if (key.getCategory() == InputUtil.Type.SCANCODE) {
            return false;
        }
        return InputUtil.isKeyPressed(client.getWindow(), key.getCode());
    }
}
