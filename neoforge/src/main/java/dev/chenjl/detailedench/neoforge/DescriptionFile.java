package dev.chenjl.detailedench.neoforge;

import java.util.ArrayList;
import java.util.List;

/** One JSON file describing how an enchantment should be shown. */
public final class DescriptionFile {
    public int schema = 1;
    public String enchantment;
    public String mode = "replace";
    public boolean disable = false;
    public List<Line> lines = new ArrayList<>();

    public boolean isAppend() {
        return "append".equalsIgnoreCase(this.mode);
    }

    public static final class Line {
        /** {@code text}, {@code value}, {@code attribute}, {@code source} or {@code template}. */
        public String type = "text";
        public String translate;
        public String text;
        public String source;
        public String component;
        public String attribute;
        public String format;
        public float scale = 1.0F;
        public List<ValueSpec> values = new ArrayList<>();
    }

    public static final class ValueSpec {
        public String component;
        public String attribute;
        public String source;
        public float scale = 1.0F;
    }
}
