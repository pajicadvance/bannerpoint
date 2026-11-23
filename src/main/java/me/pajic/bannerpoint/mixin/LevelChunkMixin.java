package me.pajic.bannerpoint.mixin;

import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {

	@Shadow @Final private Level level;

	@Shadow
	private boolean isInLevel() {
		throw new AssertionError();
	}

	@Inject(
			method = "removeBlockEntity",
			at = @At("HEAD")
	)
	private void stopTrackingOnRemove(BlockPos pos, CallbackInfo ci) {
		if (isInLevel() && level instanceof ServerLevel serverLevel) {
			BannerWaypointUtil.onBannerRemoved(serverLevel, pos);
		}
	}
}
