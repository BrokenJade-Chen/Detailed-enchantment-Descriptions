package dev.chenjl.detailedench.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.chenjl.detailedench.DetailedEnchantmentDescriptions;

/** Adds the settings button to Mod Menu; only loaded when Mod Menu is installed. */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DetailedEnchantmentDescriptions::createConfigScreen;
    }
}
