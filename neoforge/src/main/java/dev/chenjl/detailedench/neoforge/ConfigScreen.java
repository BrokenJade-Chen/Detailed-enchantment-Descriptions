package dev.chenjl.detailedench.neoforge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Small settings screen: the toggles that matter, no colour picker yet. */
public final class ConfigScreen extends Screen {
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("ded.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 150;
        int y = 44;
        y = addToggle(x, y, "ded.config.enabled", () -> NeoConfig.get().enabled, v -> NeoConfig.get().enabled = v);
        y = addToggle(x, y, "ded.config.descriptions", () -> NeoConfig.get().showDescriptions,
                v -> NeoConfig.get().showDescriptions = v);
        y = addToggle(x, y, "ded.config.numbers", () -> NeoConfig.get().showNumbers,
                v -> NeoConfig.get().showNumbers = v);
        y = addToggle(x, y, "ded.config.books", () -> NeoConfig.get().showEnchantedBook,
                v -> NeoConfig.get().showEnchantedBook = v);
        y = addToggle(x, y, "ded.config.hold_key", () -> NeoConfig.get().onlyWhenKeyHeld,
                v -> NeoConfig.get().onlyWhenKeyHeld = v);

        y += 4;
        this.colorButtons.put(false, this.addRenderableWidget(Button.builder(
                        colorLabel("ded.config.description_color", NeoConfig.get().descriptionColorRgb),
                        b -> this.openPicker(false))
                .bounds(x, y, 300, 20).build()));
        y += 24;
        this.colorButtons.put(true, this.addRenderableWidget(Button.builder(
                        colorLabel("ded.config.number_color", NeoConfig.get().numberColorRgb),
                        b -> this.openPicker(true))
                .bounds(x, y, 300, 20).build()));

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
                .bounds(this.width / 2 - 150, this.height - 32, 300, 20).build());
    }

    private final java.util.Map<Boolean, Button> colorButtons = new java.util.HashMap<>();

    private void openPicker(boolean numberColor) {
        int current = numberColor ? NeoConfig.get().numberColorRgb : NeoConfig.get().descriptionColorRgb;
        this.minecraft.setScreen(new ColorPickerScreen(this,
                Component.translatable(numberColor ? "ded.config.number_color" : "ded.config.description_color"),
                current, numberColor, value -> {
            if (numberColor) {
                NeoConfig.get().numberColorRgb = value;
            } else {
                NeoConfig.get().descriptionColorRgb = value;
            }
            NeoConfig.save();
            Button button = this.colorButtons.get(numberColor);
            if (button != null) {
                button.setMessage(colorLabel(numberColor ? "ded.config.number_color" : "ded.config.description_color", value));
            }
        }));
    }

    private static Component colorLabel(String key, int rgb) {
        return Component.translatable(key).append(": ").append(
                Component.literal(String.format("#%06X", rgb)).withStyle(style -> style.withColor(rgb)));
    }

    private int addToggle(int x, int y, String key, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        this.addRenderableWidget(Button.builder(label(key, getter.get()), button -> {
            setter.accept(!getter.get());
            button.setMessage(label(key, getter.get()));
            NeoConfig.save();
        }).bounds(x, y, 300, 20).build());
        return y + 24;
    }

    private static Component label(String key, boolean value) {
        return Component.translatable(key).append(": ")
                .append(Component.translatable(value ? "ded.value.on" : "ded.value.off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
