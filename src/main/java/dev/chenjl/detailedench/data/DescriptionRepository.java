package dev.chenjl.detailedench.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.chenjl.detailedench.DetailedEnchantmentDescriptions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Holds the JSON descriptions loaded from resource packs and from the config directory.
 *
 * <p>Load order matters: resource packs are read first, then the config directory, so a player's
 * local file overrides whatever a modpack ships.</p>
 */
public final class DescriptionRepository {
    /** Resource path prefix: {@code assets/<namespace>/enchantment_descriptions/...}. */
    public static final String RESOURCE_DIR = "enchantment_descriptions";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Identifier, DescriptionFile> ENTRIES = new LinkedHashMap<>();

    private DescriptionRepository() {
    }

    public static DescriptionFile get(Identifier enchantment) {
        return ENTRIES.get(enchantment);
    }

    public static Map<Identifier, DescriptionFile> all() {
        return Map.copyOf(ENTRIES);
    }

    public static int size() {
        return ENTRIES.size();
    }

    /** Reloads both carriers. Called on resource reload and by {@code /ded reload}. */
    public static void reload(ResourceManager manager) {
        Map<Identifier, DescriptionFile> loaded = new LinkedHashMap<>();

        Map<Identifier, Resource> resources =
                manager.findResources(RESOURCE_DIR, path -> path.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try (Reader reader = new InputStreamReader(entry.getValue().getInputStream(), StandardCharsets.UTF_8)) {
                DescriptionFile file = GSON.fromJson(reader, DescriptionFile.class);
                register(loaded, file, "resource pack " + entry.getKey());
            } catch (Exception e) {
                DetailedEnchantmentDescriptions.LOGGER.warn("Bad description file {}", entry.getKey(), e);
            }
        }

        Path configDir = FabricLoader.getInstance().getConfigDir()
                .resolve(DetailedEnchantmentDescriptions.MOD_ID)
                .resolve("enchantments");
        if (Files.isDirectory(configDir)) {
            try (Stream<Path> files = Files.list(configDir)) {
                List<Path> jsonFiles = files.filter(path -> path.getFileName().toString().endsWith(".json")).toList();
                for (Path path : jsonFiles) {
                    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                        DescriptionFile file = GSON.fromJson(reader, DescriptionFile.class);
                        register(loaded, file, "config " + path.getFileName());
                    } catch (Exception e) {
                        DetailedEnchantmentDescriptions.LOGGER.warn("Bad description file {}", path, e);
                    }
                }
            } catch (Exception e) {
                DetailedEnchantmentDescriptions.LOGGER.warn("Could not read description config directory", e);
            }
        }

        ENTRIES.clear();
        ENTRIES.putAll(loaded);
        DetailedEnchantmentDescriptions.LOGGER.info("Loaded {} enchantment description override(s)", ENTRIES.size());
    }

    /** @return the config file that stores the override for this enchantment */
    public static Path configFile(Identifier enchantment) {
        return FabricLoader.getInstance().getConfigDir()
                .resolve(DetailedEnchantmentDescriptions.MOD_ID)
                .resolve("enchantments")
                .resolve(enchantment.getNamespace() + "_" + enchantment.getPath() + ".json");
    }

    private static void register(Map<Identifier, DescriptionFile> target, DescriptionFile file, String source) {
        if (file == null || file.enchantment == null || file.enchantment.isBlank()) {
            DetailedEnchantmentDescriptions.LOGGER.warn("Skipping {}: missing 'enchantment'", source);
            return;
        }
        Identifier id = Identifier.tryParse(file.enchantment);
        if (id == null) {
            DetailedEnchantmentDescriptions.LOGGER.warn("Skipping {}: invalid id '{}'", source, file.enchantment);
            return;
        }
        target.put(id, file);
    }
}
