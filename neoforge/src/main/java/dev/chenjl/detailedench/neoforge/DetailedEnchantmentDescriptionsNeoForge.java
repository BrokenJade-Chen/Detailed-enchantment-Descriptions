package dev.chenjl.detailedench.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/** NeoForge entry point: tooltip lines, config screen, key bindings, commands and JSON overrides. */
@Mod(DetailedEnchantmentDescriptionsNeoForge.MOD_ID)
public final class DetailedEnchantmentDescriptionsNeoForge {
    public static final String MOD_ID = "detailed_enchantment_descriptions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean pendingConfigScreen;

    public DetailedEnchantmentDescriptionsNeoForge(IEventBus modBus) {
        NeoConfig.load();

        modBus.addListener(Keybinds::register);
        modBus.addListener(this::onRegisterReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onTooltip);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    // ------------------------------------------------------------------ tooltip

    private void onTooltip(ItemTooltipEvent event) {
        NeoConfig config = NeoConfig.get();
        if (!config.enabled) {
            return;
        }
        if (config.onlyWhenKeyHeld && !Keybinds.isShowHeld()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.has(net.minecraft.core.component.DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
            return;
        }
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (enchantments.isEmpty()) {
            enchantments = stack.getEnchantments();
        }
        if (enchantments.isEmpty()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            int level = entry.getIntValue();
            List<Component> lines = Describer.lines(holder, level, stack);
            if (lines.isEmpty()) {
                continue;
            }
            String name = Enchantment.getFullname(holder, level).getString();
            int index = indexOf(tooltip, name);
            if (index >= 0) {
                tooltip.addAll(index + 1, lines);
            }
        }
    }

    private static int indexOf(List<Component> tooltip, String name) {
        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).getString().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------ tick / commands

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (Keybinds.OPEN_CONFIG.consumeClick()) {
            pendingConfigScreen = true;
        }
        if (pendingConfigScreen) {
            pendingConfigScreen = false;
            client.setScreen(new ConfigScreen(client.screen));
        }
    }

    private void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("dedconfig").executes(context -> openConfig()));
        dispatcher.register(Commands.literal("ded")
                .executes(context -> openConfig())
                .then(Commands.literal("reload").executes(context -> reloadDescriptions(context.getSource())))
                .then(Commands.literal("dump").then(Commands.argument("id", com.mojang.brigadier.arguments.StringArgumentType.string())
                        .executes(context -> dump(context.getSource(),
                                com.mojang.brigadier.arguments.StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("export").then(Commands.argument("id", com.mojang.brigadier.arguments.StringArgumentType.string())
                        .executes(context -> export(context.getSource(),
                                com.mojang.brigadier.arguments.StringArgumentType.getString(context, "id"))))));
    }

    private static int openConfig() {
        pendingConfigScreen = true;
        return 1;
    }

    private static int reloadDescriptions(CommandSourceStack source) {
        DescriptionRepository.loadConfig();
        source.sendSuccess(() -> Component.translatable("ded.reload.done", DescriptionRepository.size()), false);
        return 1;
    }

    private static int dump(CommandSourceStack source, String rawId) {
        ResourceLocation id = ResourceLocation.tryParse(rawId);
        if (id == null) {
            source.sendFailure(Component.literal("Invalid enchantment id: " + rawId));
            return 0;
        }
        DescriptionFile override = DescriptionRepository.get(id);
        String mode = override == null ? "none"
                : override.disable ? "disabled" : override.isAppend() ? "append" : "replace";
        source.sendSuccess(() -> Component.literal(id + " | override=" + mode), false);
        return 1;
    }

    private static int export(CommandSourceStack source, String rawId) {
        ResourceLocation id = ResourceLocation.tryParse(rawId);
        var level = Minecraft.getInstance().level;
        if (id == null || level == null) {
            return 0;
        }
        var enchantment = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).get(id);
        if (enchantment == null) {
            source.sendFailure(Component.literal("Unknown enchantment: " + id));
            return 0;
        }
        try {
            Path out = DescriptionExporter.export(enchantment, id);
            source.sendSuccess(() -> Component.translatable("ded.export.done", out.toString()), false);
            return 1;
        } catch (Exception e) {
            LOGGER.warn("Export failed for {}", id, e);
            source.sendFailure(Component.literal("Export failed: " + e.getMessage()));
            return 0;
        }
    }

    // ------------------------------------------------------------------ resources

    private void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
                DescriptionRepository.reload(manager);
                return null;
            }

            @Override
            protected void apply(Void unused, ResourceManager manager, ProfilerFiller profiler) {
                // loaded in prepare()
            }
        });
    }
}
