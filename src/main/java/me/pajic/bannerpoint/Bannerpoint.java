package me.pajic.bannerpoint;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.pajic.bannerpoint.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public class Bannerpoint implements ModInitializer {

    public static final String MOD_ID = /*$ mod_id*/ "bannerpoint";
    public static ModConfig CONFIG = ConfigApiJava.registerAndLoadConfig(ModConfig::new);

    @Override
    public void onInitialize() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
