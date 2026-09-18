package me.pajic.bannerpoint;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.pajic.bannerpoint.config.ModClientConfig;

public class BannerpointClient {

	public static ModClientConfig CONFIG = ConfigApiJava.registerAndLoadConfig(ModClientConfig::new, RegisterType.CLIENT);

	public static void onInitialize() {}
}
