package dev.chenjl.detailedench.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Click-through settings screen built with vanilla widgets only, so the mod has no hard dependency
 * on a config library. Opened from the {@code /dedconfig} command or the (unbound) key binding.
 */
public final class ConfigScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int COLUMN_WIDTH = 210;

    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("ded.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig config = ModConfig.get();
        int x = (this.width - COLUMN_WIDTH) / 2;
        int y = 42;

        this.addDrawableChild(toggle(x, y, "ded.config.enabled",
                () -> config.enabled, value -> config.enabled = value));
        y += ROW_HEIGHT;
        this.addDrawableChild(toggle(x, y, "ded.config.descriptions",
                () -> config.showDescriptions, value -> config.showDescriptions = value));
        y += ROW_HEIGHT;
        this.addDrawableChild(toggle(x, y, "ded.config.numbers",
                () -> config.showNumbers, value -> config.showNumbers = value));
        y += ROW_HEIGHT;
        this.addDrawableChild(toggle(x, y, "ded.config.modded",
                () -> config.includeModded, value -> config.includeModded = value));
        y += ROW_HEIGHT;
        this.addDrawableChild(toggle(x, y, "ded.config.books",
                () -> config.showEnchantedBook, value -> config.showEnchantedBook = value));
        y += ROW_HEIGHT;

        this.addDrawableChild(ButtonWidget.builder(descriptionColorLabel(config), button ->
                this.openPicker(Text.translatable("ded.config.description_color"),
                        config.descriptionColorRgb, false, value -> config.descriptionColorRgb = value))
                .dimensions(x, y, COLUMN_WIDTH, 20).build());
        y += ROW_HEIGHT;

        this.addDrawableChild(ButtonWidget.builder(colorLabel(config), button ->
                this.openPicker(Text.translatable("ded.config.number_color"),
                        config.numberColorRgb, true, value -> config.numberColorRgb = value))
                .dimensions(x, y, COLUMN_WIDTH, 20).build());
        y += ROW_HEIGHT;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
                .dimensions(x, y + 8, COLUMN_WIDTH, 20).build());
    }

    private static ButtonWidget toggle(int x, int y, String labelKey,
                                       BooleanSupplier getter, Consumer<Boolean> setter) {
        return ButtonWidget.builder(toggleLabel(labelKey, getter.getAsBoolean()), button -> {
            setter.accept(!getter.getAsBoolean());
            button.setMessage(toggleLabel(labelKey, getter.getAsBoolean()));
            ModConfig.save();
        }).dimensions(x, y, COLUMN_WIDTH, 20).build();
    }

    private static Text toggleLabel(String labelKey, boolean value) {
        return Text.translatable(labelKey).append(": ")
                .append(Text.translatable(value ? "ded.value.on" : "ded.value.off"));
    }

    private void openPicker(Text title, int currentRgb, boolean numberColor,
                            java.util.function.IntConsumer setter) {
        if (this.client != null) {
            this.client.setScreen(new ColorPickerScreen(this, title, currentRgb, numberColor, value -> {
                setter.accept(value);
                ModConfig.save();
            }));
        }
    }

    private static Text descriptionColorLabel(ModConfig config) {
        return Text.translatable("ded.config.description_color").append(": ")
                .append(swatch(config.descriptionColorRgb));
    }

    private static Text colorLabel(ModConfig config) {
        return Text.translatable("ded.config.number_color").append(": ")
                .append(swatch(config.numberColorRgb));
    }

    private static Text swatch(int rgb) {
        return Text.literal(String.format("#%06X", rgb)).styled(style -> style.withColor(rgb));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
