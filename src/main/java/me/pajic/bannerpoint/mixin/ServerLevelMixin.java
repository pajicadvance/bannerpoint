package me.pajic.bannerpoint.mixin;

import me.pajic.bannerpoint.extension.ServerLevelExtension;
import me.pajic.bannerpoint.saveddata.LevelSavedBanners;
import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelExtension {

    @Shadow public abstract SavedDataStorage getDataStorage();

    @Unique private LevelSavedBanners bannerpoint$savedBanners;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void initSavedBanners(CallbackInfo ci) {
		bannerpoint$savedBanners = getDataStorage().computeIfAbsent(LevelSavedBanners.getType());
		BannerWaypointUtil.startTrackingSaved((ServerLevel) (Object) this, bannerpoint$savedBanners);
    }

	@Override
	public LevelSavedBanners bannerpoint$getSavedBanners() {
		return bannerpoint$savedBanners;
	}
}
