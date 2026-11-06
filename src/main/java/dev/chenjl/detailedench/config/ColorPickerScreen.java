package dev.chenjl.detailedench.config;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.function.IntConsumer;

/**
 * HSV colour picker: drag on the wheel for hue/saturation, use the slider for brightness, click a
 * preset swatch, or type a {@code #RRGGBB} code. The preview lines show how the colour will look in
 * a tooltip (only the number is tinted when editing the number colour).
 */
public final class ColorPickerScreen extends Screen {
    private static final Identifier WHEEL =
            Identifier.of("detailed_enchantment_descriptions", "textures/gui/color_wheel.png");
    private static final int WHEEL_SIZE = 96;
    private static final int BLOCK_HEIGHT = 236;

    private static final int[] PRESETS = {
            0xFFFFFF, 0xAAAAAA, 0x555555, 0x000000,
            0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55,
            0x55FFFF, 0x5555FF, 0xFF55FF, 0xBB55FF,
    };

    private final Screen parent;
    private final IntConsumer onPicked;
    private final int originalColor;
    /** True when the colour being edited is the number colour (the description keeps its own). */
    private final boolean previewAsNumber;

    private float hue;
    private float saturation;
    private float brightness;

    private TextFieldWidget hexField;
    private boolean updatingHexField;

    public ColorPickerScreen(Screen parent, Text title, int initialRgb, boolean previewAsNumber,
                             IntConsumer onPicked) {
        super(title);
        this.parent = parent;
        this.onPicked = onPicked;
        this.previewAsNumber = previewAsNumber;
        this.originalColor = initialRgb;
        applyRgb(initialRgb);
    }

    // ---------------------------------------------------------------- layout

    private int blockTop() {
        return Math.max(6, (this.height - BLOCK_HEIGHT) / 2);
    }

    private int wheelLeft() {
        return this.width / 2 - WHEEL_SIZE / 2;
    }

    private int wheelTop() {
        return blockTop() + 34;
    }

    private int presetsLeft() {
        return this.width / 2 - (PRESETS.length * 16 - 4) / 2;
    }

    private int presetsTop() {
        return wheelTop() + WHEEL_SIZE + 6;
    }

    private int previewTop() {
        return presetsTop() + 22;
    }

    private int sliderTop() {
        return previewTop() + 24;
    }

    // ---------------------------------------------------------------- state

    private int currentColor() {
        float section = (this.hue % 360.0F) / 60.0F;
        float chroma = this.brightness * this.saturation;
        float second = chroma * (1.0F - Math.abs(section % 2.0F - 1.0F));
        float match = this.brightness - chroma;
        float red;
        float green;
        float blue;
        if (section < 1.0F) {
            red = chroma; green = second; blue = 0.0F;
        } else if (section < 2.0F) {
            red = second; green = chroma; blue = 0.0F;
        } else if (section < 3.0F) {
            red = 0.0F; green = chroma; blue = second;
        } else if (section < 4.0F) {
            red = 0.0F; green = second; blue = chroma;
        } else if (section < 5.0F) {
            red = second; green = 0.0F; blue = chroma;
        } else {
            red = chroma; green = 0.0F; blue = second;
        }
        int r = Math.round((red + match) * 255.0F);
        int g = Math.round((green + match) * 255.0F);
        int b = Math.round((blue + match) * 255.0F);
        return (r << 16) | (g << 8) | b;
    }

    private void applyRgb(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        float max = Math.max(red, Math.max(green, blue)) / 255.0F;
        float min = Math.min(red, Math.min(green, blue)) / 255.0F;
        this.brightness = max <= 0.0F ? 1.0F : max;
        this.saturation = max <= 0.0F ? 0.0F : (max - min) / max;
        this.hue = hueOf(red, green, blue);
        syncHexField();
    }

    private static float hueOf(int red, int green, int blue) {
        float r = red / 255.0F;
        float g = green / 255.0F;
        float b = blue / 255.0F;
        float max = Math.max(r, Math.max(g, b));
        float delta = max - Math.min(r, Math.min(g, b));
        if (delta <= 0.0F) {
            return 0.0F;
        }
        float hue;
        if (max == r) {
            hue = ((g - b) / delta) % 6.0F;
        } else if (max == g) {
            hue = (b - r) / delta + 2.0F;
        } else {
            hue = (r - g) / delta + 4.0F;
        }
        return (hue * 60.0F + 360.0F) % 360.0F;
    }

    private void syncHexField() {
        if (this.hexField == null) {
            return;
        }
        this.updatingHexField = true;
        this.hexField.setText(String.format(Locale.ROOT, "#%06X", currentColor()));
        this.updatingHexField = false;
    }

    private void applyHexInput(String input) {
        if (this.updatingHexField) {
            return;
        }
        String cleaned = input.startsWith("#") ? input.substring(1) : input;
        if (cleaned.length() != 6) {
            return;
        }
        for (int i = 0; i < cleaned.length(); i++) {
            if (Character.digit(cleaned.charAt(i), 16) < 0) {
                return;
            }
        }
        applyRgb(Integer.parseInt(cleaned, 16));
    }

    // ---------------------------------------------------------------- widgets

    @Override
    protected void init() {
        int top = blockTop();
        int fieldWidth = 78;
        int fieldX = this.width / 2 - fieldWidth - 2;

        this.hexField = new TextFieldWidget(this.textRenderer, fieldX, top + 12, fieldWidth, 18,
                Text.translatable("ded.color.hex"));
        this.hexField.setMaxLength(8);
        this.hexField.setText(String.format(Locale.ROOT, "#%06X", currentColor()));
        this.hexField.setChangedListener(this::applyHexInput);
        this.addDrawableChild(this.hexField);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("ded.color.apply"), button -> {
            applyHexInput(this.hexField.getText());
            syncHexField();
        }).dimensions(fieldX + fieldWidth + 4, top + 12, 56, 18).build());

        int sliderWidth = 160;
        int sliderX = this.width / 2 - sliderWidth / 2;
        Text initialBrightness = Text.translatable("ded.color.brightness", Math.round(this.brightness * 100.0F));
        this.addDrawableChild(new SliderWidget(sliderX, sliderTop(), sliderWidth, 20,
                initialBrightness, this.brightness) {
            @Override
            protected void updateMessage() {
                setMessage(Text.translatable("ded.color.brightness", Math.round(this.value * 100.0F)));
            }

            @Override
            protected void applyValue() {
                brightness = (float) this.value;
                syncHexField();
            }
        });

        int buttonY = sliderTop() + 26;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.confirm())
                .dimensions(sliderX, buttonY, 78, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> this.cancel())
                .dimensions(sliderX + sliderWidth - 78, buttonY, 78, 20).build());
    }

    // ---------------------------------------------------------------- drawing

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, blockTop() - 4, 0xFFFFFFFF);

        int left = wheelLeft();
        int top = wheelTop();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WHEEL, left, top, 0.0F, 0.0F,
                WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);

        double angle = Math.toRadians(this.hue);
        double radius = this.saturation * (WHEEL_SIZE / 2.0F - 1);
        int markerX = (int) Math.round(left + WHEEL_SIZE / 2.0F + Math.cos(angle) * radius);
        int markerY = (int) Math.round(top + WHEEL_SIZE / 2.0F + Math.sin(angle) * radius);
        context.fill(markerX - 3, markerY - 3, markerX + 3, markerY + 3, 0xFFFFFFFF);
        context.fill(markerX - 2, markerY - 2, markerX + 2, markerY + 2, 0xFF000000);

        int current = currentColor();
        int presetsX = presetsLeft();
        int presetsY = presetsTop();
        for (int i = 0; i < PRESETS.length; i++) {
            int swatchX = presetsX + i * 16;
            context.fill(swatchX, presetsY, swatchX + 12, presetsY + 12, 0xFF000000 | PRESETS[i]);
            if (PRESETS[i] == current) {
                context.fill(swatchX - 1, presetsY - 1, swatchX + 13, presetsY, 0xFFFFFFFF);
                context.fill(swatchX - 1, presetsY + 12, swatchX + 13, presetsY + 13, 0xFFFFFFFF);
                context.fill(swatchX - 1, presetsY, swatchX, presetsY + 12, 0xFFFFFFFF);
                context.fill(swatchX + 12, presetsY, swatchX + 13, presetsY + 12, 0xFFFFFFFF);
            }
        }

        context.drawCenteredTextWithShadow(this.textRenderer,
                previewLine("ded.color.preview.description", "3"), this.width / 2, previewTop(), 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                previewLine("ded.color.preview.value", "75%"), this.width / 2, previewTop() + 11, 0xFFFFFFFF);
    }

    /**
     * Mirrors what the tooltip will look like: only the number gets the number colour, the rest of
     * the line uses the description colour.
     */
    private Text previewLine(String key, String number) {
        int picked = currentColor();
        int numberColor = this.previewAsNumber ? picked : ModConfig.get().numberColorRgb;
        int baseColor = this.previewAsNumber ? ModConfig.get().descriptionColorRgb : picked;
        Text numberText = Text.literal(number).styled(style -> style.withColor(numberColor));
        return Text.translatable(key, numberText).styled(style -> style.withColor(baseColor));
    }

    // ---------------------------------------------------------------- input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int presetsX = presetsLeft();
            int presetsY = presetsTop();
            if (mouseY >= presetsY - 2 && mouseY <= presetsY + 14) {
                for (int i = 0; i < PRESETS.length; i++) {
                    int swatchX = presetsX + i * 16;
                    if (mouseX >= swatchX - 2 && mouseX <= swatchX + 14) {
                        applyRgb(PRESETS[i]);
                        return true;
                    }
                }
            }
            if (insideWheel(mouseX, mouseY)) {
                pick(mouseX, mouseY);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && insideWheel(mouseX, mouseY)) {
            pick(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private boolean insideWheel(double mouseX, double mouseY) {
        double dx = mouseX - (wheelLeft() + WHEEL_SIZE / 2.0F);
        double dy = mouseY - (wheelTop() + WHEEL_SIZE / 2.0F);
        return dx * dx + dy * dy <= (WHEEL_SIZE / 2.0F) * (WHEEL_SIZE / 2.0F);
    }

    private void pick(double mouseX, double mouseY) {
        double dx = mouseX - (wheelLeft() + WHEEL_SIZE / 2.0F);
        double dy = mouseY - (wheelTop() + WHEEL_SIZE / 2.0F);
        this.hue = (float) ((Math.toDegrees(Math.atan2(dy, dx)) + 360.0) % 360.0);
        this.saturation = (float) Math.min(1.0, Math.hypot(dx, dy) / (WHEEL_SIZE / 2.0));
        syncHexField();
    }

    // ---------------------------------------------------------------- confirm

    private void confirm() {
        this.onPicked.accept(currentColor());
        ModConfig.save();
        this.close();
    }

    private void cancel() {
        this.onPicked.accept(this.originalColor);
        this.close();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
