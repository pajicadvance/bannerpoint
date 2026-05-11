package me.pajic.bannerpoint.mixin;

import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {

	@Shadow @Nullable protected Level level;

	@SuppressWarnings("ConstantValue")
	@Inject(
			method = "applyComponents",
			at = @At("TAIL")
	)
	private void trackBannerWaypointOnApplyComponents(CallbackInfo ci) {
		if (level instanceof ServerLevel serverLevel && (BlockEntity) (Object) this instanceof BannerBlockEntity bbe) {
			BannerWaypointUtil.startTracking(serverLevel, bbe);
		}
	}

	@SuppressWarnings("ConstantValue")
	@Inject(
			method = "setLevel",
			at = @At("TAIL")
	)
	private void trackBannerWaypointOnSetLevel(CallbackInfo ci) {
		if (level instanceof ServerLevel serverLevel && (BlockEntity) (Object) this instanceof BannerBlockEntity bbe) {
			BannerWaypointUtil.startTracking(serverLevel, bbe);
		}
	}
}
