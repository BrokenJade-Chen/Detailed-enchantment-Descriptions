package dev.chenjl.detailedench;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.chenjl.detailedench.config.ConfigScreen;
import dev.chenjl.detailedench.data.DescriptionFile;
import dev.chenjl.detailedench.data.DescriptionExporter;
import dev.chenjl.detailedench.data.DescriptionRepository;
import dev.chenjl.detailedench.tooltip.EnchantmentDescriber;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import dev.chenjl.detailedench.config.ModConfig;
import dev.chenjl.detailedench.tooltip.EnchantmentTooltipAppender;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Client entry point: registers the tooltip hook and the two ways to open the settings screen
 * (an unbound key binding and the {@code /dedconfig} client command).
 */
public final class DetailedEnchantmentDescriptions implements ClientModInitializer {
    public static final String MOD_ID = "detailed_enchantment_descriptions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


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

        Keybinds.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (Keybinds.OPEN_CONFIG.wasPressed()) {
                pendingConfigScreen = true;
            }
            if (pendingConfigScreen) {
                pendingConfigScreen = false;
                openConfig(client);
            }
        });

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.of(MOD_ID, "descriptions");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        DescriptionRepository.reload(manager);
                    }
                });

        LOGGER.info("Registering client commands");
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("dedconfig").executes(context -> openConfigRequest()));
            dispatcher.register(literal("ded")
                    .executes(context -> openConfigRequest())
                    .then(literal("reload").executes(context -> {
                        DescriptionRepository.reload(MinecraftClient.getInstance().getResourceManager());
                        context.getSource().sendFeedback(
                                Text.translatable("ded.reload.done", DescriptionRepository.size()));
                        return 1;
                    }))
                    .then(literal("dump").then(argument("id", StringArgumentType.string())
                            .executes(context -> dump(context, StringArgumentType.getString(context, "id")))))
                    .then(literal("export").then(argument("id", StringArgumentType.string())
                            .executes(context -> export(context, StringArgumentType.getString(context, "id"))))));
        });
    }

    private static int export(CommandContext<FabricClientCommandSource> context, String rawId) {
        Identifier id = Identifier.tryParse(rawId);
        if (id == null) {
            context.getSource().sendError(Text.literal("Invalid enchantment id: " + rawId));
            return 0;
        }
        Enchantment enchantment = findEnchantment(id);
        if (enchantment == null) {
            context.getSource().sendError(Text.literal("Unknown enchantment: " + id));
            return 0;
        }
        try {
            context.getSource().sendFeedback(Text.translatable("ded.export.done",
                    DescriptionExporter.export(enchantment, id).toString()));
            return 1;
        } catch (Exception e) {
            LOGGER.warn("Could not export {}", id, e);
            context.getSource().sendError(Text.literal("Export failed: " + e.getMessage()));
            return 0;
        }
    }

    private static Enchantment findEnchantment(Identifier id) {
        MinecraftClient client = MinecraftClient.getInstance();
        net.minecraft.registry.DynamicRegistryManager manager = client.world != null
                ? client.world.getRegistryManager()
                : client.getNetworkHandler() != null ? client.getNetworkHandler().getRegistryManager() : null;
        return manager == null ? null
                : manager.getOptional(RegistryKeys.ENCHANTMENT).map(registry -> registry.get(id)).orElse(null);
    }

    private static int dump(CommandContext<FabricClientCommandSource> context, String rawId) {
        Identifier id = Identifier.tryParse(rawId);
        if (id == null) {
            context.getSource().sendError(Text.literal("Invalid enchantment id: " + rawId));
            return 0;
        }
        DescriptionFile override = DescriptionRepository.get(id);
        String mode = override == null ? "none" : (override.disable ? "disabled" : (override.isAppend() ? "append" : "replace"));
        context.getSource().sendFeedback(Text.literal(
                id + " | builtin=" + EnchantmentDescriber.hasBuiltin(id) + " | override=" + mode));
        return 1;
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

