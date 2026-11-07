package dev.chenjl.detailedench.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/** Key bindings registered on the mod event bus. */
public final class Keybinds {
    public static final String CATEGORY = "key.categories.detailed_enchantment_descriptions";

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.detailed_enchantment_descriptions.open_config",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);

    public static final KeyMapping SHOW_DESCRIPTIONS = new KeyMapping(
            "key.detailed_enchantment_descriptions.show_descriptions",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);

    private Keybinds() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
        event.register(SHOW_DESCRIPTIONS);
    }

    /**
     * Raw key state: {@link KeyMapping#isDown()} returns a cached flag that vanilla only refreshes
     * while no screen is open, which is exactly when tooltips are shown.
     */
    public static boolean isShowHeld() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) {
            return false;
        }
        InputConstants.Key key = SHOW_DESCRIPTIONS.getKey();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(client.getWindow().getWindow(), key.getValue()) == GLFW.GLFW_PRESS;
        }
        if (key.getType() == InputConstants.Type.SCANCODE) {
            return false;
        }
        return InputConstants.isKeyDown(client.getWindow().getWindow(), key.getValue());
    }
}
