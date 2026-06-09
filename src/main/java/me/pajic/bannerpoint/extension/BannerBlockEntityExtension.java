package me.pajic.bannerpoint.extension;

import net.minecraft.network.chat.Component;

import java.util.UUID;

public interface BannerBlockEntityExtension {
	UUID bannerpoint$getUUID();
	void bannerpoint$setTiedToMap(boolean tiedToMap);
	boolean bannerpoint$hasCustomName();
	Component bannerpoint$getCustomName();
}
