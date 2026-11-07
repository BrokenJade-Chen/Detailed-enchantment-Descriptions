package dev.chenjl.detailedench;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Key bindings registered by the mod. */
public final class Keybinds {
    public static final String CATEGORY = "key.categories.detailed_enchantment_descriptions";

    /** Opens the settings screen (unbound by default). */
    public static final KeyBinding OPEN_CONFIG = register("open_config", GLFW.GLFW_KEY_UNKNOWN);
    /** While held, descriptions are shown when {@code onlyWhenKeyHeld} is enabled. */
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

    /**
     * @return whether the "show descriptions" key is physically held down
     *
     * <p>{@link KeyBinding#isPressed()} only reflects the last state vanilla recorded, and vanilla
     * stops updating key binding states while a screen (e.g. the inventory) is open - which is
     * exactly when tooltips are shown. Querying the raw key state works in every context.</p>
     */
    public static boolean isShowHeld() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return false;
        }
        InputUtil.Key key = InputUtil.fromTranslationKey(SHOW_DESCRIPTIONS.getBoundKeyTranslationKey());
        long handle = client.getWindow().getHandle();
        if (key.getCategory() == InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(handle, key.getCode()) == GLFW.GLFW_PRESS;
        }
        if (key.getCategory() == InputUtil.Type.SCANCODE) {
            return false;
        }
        return InputUtil.isKeyPressed(handle, key.getCode());
    }
}
