package dev.chenjl.detailedench.tooltip;

import dev.chenjl.detailedench.config.ModConfig;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Locale;

/** Formats the numbers that end up inside tooltip lines. */
public final class Numbers {
    private Numbers() {
    }

    /** Formats a value as "3" or "12.5" (never "3.0"). */
    public static String of(float value) {
        if (Math.abs(value - Math.round(value)) < 0.001F) {
            return Integer.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    /** Formats a ratio (0.75) as a percentage (75). */
    public static String percent(float ratio) {
        return of(ratio * 100.0F);
    }

    /** Wraps a number in a coloured, translatable-safe text for use as a tooltip argument. */
    public static MutableText coloured(String value) {
        return Text.literal(value).styled(style -> style.withColor(ModConfig.get().numberColorRgb));
    }

    public static MutableText coloured(float value) {
        return coloured(of(value));
    }
}
