package dev.chenjl.detailedench.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.chenjl.detailedench.DetailedEnchantmentDescriptions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * User facing settings, stored as JSON in {@code config/detailed_enchantment_descriptions.json}.
 *
 * <p>Everything defaults to "on": the mod is meant to replace description mods, so an untouched
 * install should already show descriptions and numbers.</p>
 */
public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(DetailedEnchantmentDescriptions.MOD_ID + ".json");

    private static ModConfig instance;

    /** Master switch. */
    public boolean enabled = true;
    /** Show the description line under each enchantment. */
    public boolean showDescriptions = true;
    /** Show numeric values inside the description (e.g. "+12.5 damage"). */
    public boolean showNumbers = true;
    /** Also describe enchantments that do not come from {@code minecraft:}. */
    public boolean includeModded = true;
    /** Describe enchantments on enchanted books. */
    public boolean showEnchantedBook = true;
    /** When true, descriptions are only shown while the "show descriptions" key is held. */
    public boolean onlyWhenKeyHeld = false;
    /** 0xRRGGBB colour used for the numbers. */
    public int numberColorRgb = 0x00AA00;
    /** 0xRRGGBB colour used for the description text itself. */
    public int descriptionColorRgb = 0x555555;

    /** Legacy (0.1.4 and older) colour names. */
    public String numberColor = null;
    public String descriptionColor = null;
    /** Legacy (0.1.5) ARGB values, converted to RGB on load. */
    public Integer numberColorArgb = null;
    public Integer descriptionColorArgb = null;
    /** Log unknown effect components once, for debugging. */
    public boolean verboseUnknown = false;

    public static ModConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                instance = loaded == null ? new ModConfig() : loaded;
                instance.migrateLegacyColors();
            } catch (IOException | RuntimeException e) {
                DetailedEnchantmentDescriptions.LOGGER.warn("Could not read config, using defaults", e);
                instance = new ModConfig();
            }
        } else {
            instance = new ModConfig();
            save();
        }
    }

    private void migrateLegacyColors() {
        boolean changed = false;
        if (this.numberColorArgb != null) {
            this.numberColorRgb = this.numberColorArgb & 0xFFFFFF;
            this.numberColorArgb = null;
            changed = true;
        }
        if (this.descriptionColorArgb != null) {
            this.descriptionColorRgb = this.descriptionColorArgb & 0xFFFFFF;
            this.descriptionColorArgb = null;
            changed = true;
        }
        if (this.numberColor != null) {
            this.numberColorRgb = legacyColor(this.numberColor, this.numberColorRgb);
            this.numberColor = null;
            changed = true;
        }
        if (this.descriptionColor != null) {
            this.descriptionColorRgb = legacyColor(this.descriptionColor, this.descriptionColorRgb);
            this.descriptionColor = null;
            changed = true;
        }
        if (changed) {
            save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            DetailedEnchantmentDescriptions.LOGGER.warn("Could not save config", e);
        }
    }

    /** @return the RGB value of a legacy colour name, or {@code fallback} */
    private static int legacyColor(String name, int fallback) {
        if (name == null) {
            return fallback;
        }
        try {
            return NumberColor.valueOf(name).formatting().getColorValue();
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    /** Preset colours for the numeric part of a description. */
    public enum NumberColor {
        DARK_GRAY(Formatting.DARK_GRAY),
        GRAY(Formatting.GRAY),
        DARK_GREEN(Formatting.DARK_GREEN),
        GREEN(Formatting.GREEN),
        AQUA(Formatting.AQUA),
        BLUE(Formatting.BLUE),
        GOLD(Formatting.GOLD),
        YELLOW(Formatting.YELLOW);

        private final Formatting formatting;

        NumberColor(Formatting formatting) {
            this.formatting = formatting;
        }

        public Formatting formatting() {
            return this.formatting;
        }

        public NumberColor next() {
            NumberColor[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }
}
