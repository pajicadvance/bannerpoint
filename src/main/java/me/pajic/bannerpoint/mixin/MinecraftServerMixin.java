package me.pajic.bannerpoint.mixin;

import me.pajic.bannerpoint.Bannerpoint;import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

	@Inject(
			method = "stopServer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;close()V"
			)
	)
	private void onLevelUnload(CallbackInfo ci) {
		BannerWaypointUtil.savedDataLoadFinished = false;
		Bannerpoint.debugLog("Flagged saved data for reload");
	}
}
