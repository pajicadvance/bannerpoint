package me.pajic.bannerpoint;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.pajic.bannerpoint.config.ModConfig;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? fabric
import net.fabricmc.loader.api.FabricLoader;
//? neoforge
//import net.neoforged.fml.loading.FMLLoader;

public class Bannerpoint {

	public static final String MOD_ID = /*$ mod_id*/ "bannerpoint";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModConfig CONFIG = ConfigApiJava.registerAndLoadConfig(ModConfig::new);

	public static void onInitialize() {}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static void debugLog(String message, Object ... args) {
		if (isDebug()) LOGGER.info(message, args);
	}

	private static boolean isDebug() {
		//? fabric
		return FabricLoader.getInstance().isDevelopmentEnvironment();
		//? neoforge
		//return !FMLLoader.getCurrent().isProduction();
	}
}
