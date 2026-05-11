package me.pajic.bannerpoint.extension;

import java.util.UUID;

public interface BannerBlockEntityExtension {
	UUID bannerpoint$getUUID();
	void bannerpoint$setTiedToMap(boolean tiedToMap);
	boolean bannerpoint$hasCustomName();
}
