package dev.chenjl.detailedench.data;

import java.util.ArrayList;
import java.util.List;

/**
 * One JSON file describing how an enchantment should be shown. Files live either in a resource pack
 * ({@code assets/<modid>/enchantment_descriptions/<name>.json}) or in
 * {@code config/detailed_enchantment_descriptions/enchantments/<name>.json}; the config directory
 * wins when both define the same enchantment.
 *
 * <p>See {@code docs/EXTENDING.md} for the full schema.</p>
 */
public final class DescriptionFile {
    /** Schema version, allows future format changes without breaking old files. */
    public int schema = 1;

    /** Enchantment id, e.g. {@code minecraft:sharpness} or {@code somemod:fire_touch}. */
    public String enchantment;

    /** {@code replace} (default) or {@code append}. */
    public String mode = "replace";

    /** When true the enchantment gets no description at all. */
    public boolean disable = false;

    /** Lines in display order. */
    public List<Line> lines = new ArrayList<>();

    public boolean isAppend() {
        return "append".equalsIgnoreCase(this.mode);
    }

    /** A single tooltip line. */
    public static final class Line {
        /** {@code text}, {@code value} (a value effect component) or {@code attribute}. */
        public String type = "text";

        /** Translation key for {@code text} lines, or the template key for value lines. */
        public String translate;

        /** Literal fallback for {@code text} lines when no translation key is given. */
        public String text;

        /** Named value source for {@code source} lines (see docs/EXTENDING.md). */
        public String source;

        /** Component id for {@code value} lines, e.g. {@code minecraft:damage}. */
        public String component;

        /** Attribute id for {@code attribute} lines, e.g. {@code minecraft:mining_efficiency}. */
        public String attribute;

        /** Format template key for value lines; defaults to {@code ded.line.value}. */
        public String format;

        /** Value multiplier applied before formatting (e.g. 100 to turn 0.15 into 15). */
        public float scale = 1.0F;

        /** For {@code type: "template"}: one spec per {@code %s} placeholder, in order. */
        public List<ValueSpec> values = new ArrayList<>();
    }

    /** One number used by a {@code template} line. */
    public static final class ValueSpec {
        /** Component id whose value effects are summed, e.g. {@code minecraft:damage}. */
        public String component;

        /** Attribute id, e.g. {@code minecraft:mining_efficiency}. */
        public String attribute;

        /** Named value source for numbers that are not plain components/attributes. */
        public String source;

        /** Multiplier applied before formatting. */
        public float scale = 1.0F;
    }
}
