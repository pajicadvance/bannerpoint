package me.pajic.bannerpoint;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.pajic.bannerpoint.config.ModConfig;
import net.minecraft.resources.Identifier;

public class Bannerpoint {

	public static final String MOD_ID = /*$ mod_id*/ "bannerpoint";
	public static ModConfig CONFIG = ConfigApiJava.registerAndLoadConfig(ModConfig::new);

	public static void onInitialize() {}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
