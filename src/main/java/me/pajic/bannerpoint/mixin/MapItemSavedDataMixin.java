package me.pajic.bannerpoint.mixin;

import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin {

	@Inject(
			method = "toggleBanner",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;removeDecoration(Ljava/lang/String;)V"
			)
	)
	private void stopTrackingOnRemovedFromMap(LevelAccessor level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BannerWaypointUtil.setMapTracking(false, level, pos);
	}

	@Inject(
			method = "toggleBanner",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;addDecoration(Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/LevelAccessor;Ljava/lang/String;DDDLnet/minecraft/network/chat/Component;)V"
			)
	)
	private void startTrackingOnAddedToMap(LevelAccessor level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BannerWaypointUtil.setMapTracking(true, level, pos);
	}
}
