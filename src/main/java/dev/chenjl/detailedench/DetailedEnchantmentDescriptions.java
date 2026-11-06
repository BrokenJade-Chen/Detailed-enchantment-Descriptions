package dev.chenjl.detailedench;

import dev.chenjl.detailedench.config.ConfigScreen;
import dev.chenjl.detailedench.config.ModConfig;
import dev.chenjl.detailedench.tooltip.EnchantmentTooltipAppender;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Client entry point: registers the tooltip hook and the two ways to open the settings screen
 * (an unbound key binding and the {@code /dedconfig} client command).
 */
public final class DetailedEnchantmentDescriptions implements ClientModInitializer {
    public static final String MOD_ID = "detailed_enchantment_descriptions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final String KEY_CATEGORY = "key.categories." + MOD_ID;
    private static final String KEY_OPEN_CONFIG = "key." + MOD_ID + ".open_config";

    private static KeyBinding openConfigKey;

    /**
     * Opening a screen straight from a chat command does not stick: the chat screen tears itself down
     * right after the command runs and would close whatever we opened. Commands therefore only set a
     * flag that the client tick handles on the next frame.
     */
    private static boolean pendingConfigScreen;

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        ItemTooltipCallback.EVENT.register(new EnchantmentTooltipAppender());

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_OPEN_CONFIG,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, // unbound by default so it cannot clash with other mods
                KEY_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.wasPressed()) {
                pendingConfigScreen = true;
            }
            if (pendingConfigScreen) {
                pendingConfigScreen = false;
                openConfig(client);
            }
        });

        LOGGER.info("Registering client commands");
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("dedconfig").executes(context -> openConfigRequest()));
            dispatcher.register(literal("ded").executes(context -> openConfigRequest()));
        });
    }

    private static int openConfigRequest() {
        LOGGER.info("Settings requested from command");
        pendingConfigScreen = true;
        return 1;
    }

    private static void openConfig(MinecraftClient client) {
        client.setScreen(createConfigScreen(client.currentScreen));
    }

    /** The settings screen, built with vanilla widgets so no config library is required. */
    public static Screen createConfigScreen(Screen parent) {
        return new ConfigScreen(parent);
    }
}
