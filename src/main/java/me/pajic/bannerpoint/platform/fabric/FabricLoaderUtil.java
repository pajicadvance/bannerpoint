package me.pajic.bannerpoint.platform.fabric;

//? fabric {

import me.pajic.bannerpoint.platform.MultiLoaderUtil;
import net.fabricmc.loader.api.FabricLoader;

public class FabricLoaderUtil implements MultiLoaderUtil {

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
//?}
