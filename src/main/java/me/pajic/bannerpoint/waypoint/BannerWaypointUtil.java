package me.pajic.bannerpoint.waypoint;

import me.pajic.bannerpoint.Bannerpoint;
import me.pajic.bannerpoint.extension.BannerBlockEntityExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;
import net.minecraft.world.waypoints.WaypointTransmitter;

import java.util.Optional;

public class BannerWaypointUtil {

	private static final ResourceKey<WaypointStyleAsset> BANNER = ResourceKey.create(WaypointStyleAssets.ROOT_ID, Bannerpoint.id("banner"));

	public static void startTracking(ServerLevel level, BannerBlockEntity bbe) {
		WaypointTransmitter wt = (WaypointTransmitter) bbe;
		if (wt.isTransmittingWaypoint()) level.getWaypointManager().trackWaypoint(wt);
	}

	public static void stopTracking(ServerLevel level, BannerBlockEntity bbe) {
		level.getWaypointManager().untrackWaypoint((WaypointTransmitter) bbe);
	}

	public static void setMapTracking(boolean shouldTrack, LevelAccessor level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (Bannerpoint.CONFIG.transmitWhenTiedToMap.get() && be instanceof BannerBlockEntity bbe && level instanceof ServerLevelAccessor sla) {
			BannerBlockEntityExtension bbee = (BannerBlockEntityExtension) bbe;
			bbee.bannerpoint$setTiedToMap(shouldTrack);
			if (!bbee.bannerpoint$hasCustomName()) {
				if (shouldTrack) BannerWaypointUtil.startTracking(sla.getLevel(), bbe);
				else BannerWaypointUtil.stopTracking(sla.getLevel(), bbe);
			}
			bbe.setChanged();
		}
	}

	public static Waypoint.Icon createBannerIcon(int color) {
		return new Waypoint.Icon(BANNER, Optional.of(color));
	}

	public static boolean doesSourceIgnoreReceiver(final BannerBlockEntity source, final ServerPlayer receiver) {
		if (receiver.isSpectator()) return false;
		else return distance(source.getBlockPos(), receiver) >= Math.min(Bannerpoint.CONFIG.bannerTransmitRange.get(), receiver.getAttributeValue(Attributes.WAYPOINT_RECEIVE_RANGE));
	}

	public static boolean isReallyFar(final BannerBlockEntity source, final ServerPlayer receiver) {
		return distance(source.getBlockPos(), receiver) > WaypointTransmitter.REALLY_FAR_DISTANCE;
	}

	private static double distance(final BlockPos source, final ServerPlayer receiver) {
		return Math.sqrt(receiver.distanceToSqr(source.getCenter()));
	}
}
