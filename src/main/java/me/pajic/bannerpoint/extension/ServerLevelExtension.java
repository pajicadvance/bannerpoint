package me.pajic.bannerpoint.extension;

import me.pajic.bannerpoint.saveddata.LevelSavedBanners;
import org.jetbrains.annotations.Nullable;

public interface ServerLevelExtension {
	@Nullable LevelSavedBanners bannerpoint$getSavedBanners();
}
