package me.pajic.bannerpoint.mixin;

import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.minecraft.world.level.Level;
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

	@Inject(
			method = {"applyComponents", "setLevel"},
			at = @At("TAIL"),
			require = 2
	)
	private void startTrackingOnInit(CallbackInfo ci) {
		BannerWaypointUtil.startTrackingOnInit(level, (BlockEntity) (Object) this);
	}
}
