package me.pajic.bannerpoint.mixin.client;

import me.pajic.bannerpoint.client.BannerNameRenderer;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWaypointManager.class)
public class ClientWaypointManagerMixin {

	@Inject(
			method = "untrackWaypoint(Lnet/minecraft/world/waypoints/TrackedWaypoint;)V",
			at = @At("HEAD")
	)
	private void removeBannerName(TrackedWaypoint waypoint, CallbackInfo ci) {
		waypoint.id().left().ifPresent(BannerNameRenderer::remove);
	}
}
