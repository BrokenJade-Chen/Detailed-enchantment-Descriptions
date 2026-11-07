package dev.chenjl.detailedench.neoforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Minimal JSON config, kept in the config folder. */
public final class NeoConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("detailed_enchantment_descriptions");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static NeoConfig instance = new NeoConfig();

    public boolean enabled = true;
    public boolean showDescriptions = true;
    public boolean showNumbers = true;
    public boolean showEnchantedBook = true;
    public boolean onlyWhenKeyHeld = false;
    public int descriptionColorRgb = 0x555555;
    public int numberColorRgb = 0x00AA00;

    public static NeoConfig get() {
        return instance;
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("detailed_enchantment_descriptions.json");
    }

    public static void load() {
        Path path = file();
        if (Files.exists(path)) {
            try {
                instance = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), NeoConfig.class);
                if (instance == null) {
                    instance = new NeoConfig();
                }
            } catch (Exception e) {
                LOGGER.warn("Could not read config, using defaults", e);
                instance = new NeoConfig();
            }
        }
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(instance), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Could not write config", e);
        }
    }
}
