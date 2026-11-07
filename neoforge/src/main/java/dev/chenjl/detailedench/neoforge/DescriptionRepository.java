package dev.chenjl.detailedench.neoforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.loading.FMLPaths;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Loads JSON descriptions from resource packs and from the config directory. */
public final class DescriptionRepository {
    public static final String RESOURCE_DIR = "enchantment_descriptions";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, DescriptionFile> ENTRIES = new LinkedHashMap<>();

    private DescriptionRepository() {
    }

    public static DescriptionFile get(ResourceLocation id) {
        return ENTRIES.get(id);
    }

    public static int size() {
        return ENTRIES.size();
    }

    public static void reload(ResourceManager manager) {
        Map<ResourceLocation, DescriptionFile> loaded = new LinkedHashMap<>();
        try {
            Map<ResourceLocation, Resource> resources =
                    manager.listResources(RESOURCE_DIR, path -> path.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                try (Reader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                    register(loaded, GSON.fromJson(reader, DescriptionFile.class), entry.getKey().toString());
                } catch (Exception e) {
                    DetailedEnchantmentDescriptionsNeoForge.LOGGER.warn("Bad description file {}", entry.getKey(), e);
                }
            }
        } catch (Exception e) {
            DetailedEnchantmentDescriptionsNeoForge.LOGGER.warn("Could not scan resource packs", e);
        }
        loadConfig(loaded);
        ENTRIES.clear();
        ENTRIES.putAll(loaded);
        DetailedEnchantmentDescriptionsNeoForge.LOGGER.info("Loaded {} enchantment description override(s)", ENTRIES.size());
    }

    /** Reloads only the config directory (used by {@code /ded reload}). */
    public static void loadConfig() {
        Map<ResourceLocation, DescriptionFile> loaded = new LinkedHashMap<>();
        loadConfig(loaded);
        ENTRIES.putAll(loaded);
    }

    private static void loadConfig(Map<ResourceLocation, DescriptionFile> target) {
        Path dir = FMLPaths.CONFIGDIR.get().resolve(DetailedEnchantmentDescriptionsNeoForge.MOD_ID)
                .resolve("enchantments");
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> files = Files.list(dir)) {
            List<Path> json = files.filter(p -> p.getFileName().toString().endsWith(".json")).toList();
            for (Path path : json) {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    register(target, GSON.fromJson(reader, DescriptionFile.class), "config " + path.getFileName());
                } catch (Exception e) {
                    DetailedEnchantmentDescriptionsNeoForge.LOGGER.warn("Bad description file {}", path, e);
                }
            }
        } catch (Exception e) {
            DetailedEnchantmentDescriptionsNeoForge.LOGGER.warn("Could not read description config directory", e);
        }
    }

    private static void register(Map<ResourceLocation, DescriptionFile> target, DescriptionFile file, String source) {
        if (file == null || file.enchantment == null || file.enchantment.isBlank()) {
            DetailedEnchantmentDescriptionsNeoForge.LOGGER.warn("Skipping {}: missing 'enchantment'", source);
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(file.enchantment);
        if (id == null) {
            DetailedEnchantmentDescriptionsNeoForge.LOGGER.warn("Skipping {}: invalid id '{}'", source, file.enchantment);
            return;
        }
        target.put(id, file);
    }
}
