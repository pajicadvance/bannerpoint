package me.pajic.bannerpoint.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

	@Shadow protected ServerLevel level;

	@Inject(
			method = "destroyBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;destroy(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
			)
	)
	private void stopTrackingOnBreak(CallbackInfoReturnable<Boolean> cir, @Local BlockEntity blockEntity) {
		if (blockEntity instanceof BannerBlockEntity bbe) BannerWaypointUtil.stopTracking(level, bbe);
	}
}
