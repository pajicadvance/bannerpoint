package me.pajic.bannerpoint.waypoint;

import me.pajic.bannerpoint.Bannerpoint;
import me.pajic.bannerpoint.extension.BannerBlockEntityExtension;
import me.pajic.bannerpoint.extension.ServerLevelExtension;
import me.pajic.bannerpoint.mixin.WaypointIconAccessor;
import me.pajic.bannerpoint.networking.NetworkingUtil;
import me.pajic.bannerpoint.networking.S2CBannerDistancePayload;
import me.pajic.bannerpoint.networking.S2CBannerNamePayload;
import me.pajic.bannerpoint.saveddata.LevelSavedBanners;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BannerWaypointUtil {

	private static final ResourceKey<WaypointStyleAsset> BANNER = ResourceKey.create(WaypointStyleAssets.ROOT_ID, Bannerpoint.id("banner"));
	private static final int FAR_DISTANCE_STEP = 10;

	public static void startTrackingSaved(ServerLevel level, LevelSavedBanners data) {
		for (BlockPos pos : List.copyOf(data.getBanners())) {
			if (level.getBlockEntity(pos) instanceof BannerBlockEntity bbe) refreshTracking(level, bbe);
			else data.removeBanner(pos);
		}
		Bannerpoint.debugLog("Finished loading saved banners");
	}

	public static void startTrackingOnInit(ServerLevel level, BlockEntity be) {
		if (be instanceof BannerBlockEntity bbe) refreshTracking(level, bbe);
	}

	public static void setMapTracking(boolean tied, ServerLevel level, BlockPos pos) {
		LevelSavedBanners data = getData(level);
		if (data == null || !(level.getBlockEntity(pos) instanceof BannerBlockEntity bbe)) return;
		if (tied) data.addMapTie(pos);
		else data.removeMapTie(pos);
		refreshTracking(level, bbe);
	}

	public static void refreshTracking(ServerLevel level, BannerBlockEntity bbe) {
		LevelSavedBanners data = getData(level);
		if (data == null) return;
		BlockPos pos = bbe.getBlockPos();
		BannerBlockEntityExtension bbee = (BannerBlockEntityExtension) bbe;
		if (bbee.bannerpoint$consumeLegacyTiedToMap() && data.getMapTies(pos) == 0) data.addMapTie(pos);
		BannerBlockEntity previous = data.getTracked(pos);
		if (previous != null && previous != bbe) {
			level.getWaypointManager().untrackWaypoint((WaypointTransmitter) previous);
			data.removeTracked(pos);
			previous = null;
		}
		Bannerpoint.debugLog("Refreshing banner at {}", pos.toShortString());
		if (((WaypointTransmitter) bbe).isTransmittingWaypoint()) {
			if (previous == null) {
				level.getWaypointManager().trackWaypoint((WaypointTransmitter) bbe);
				data.setTracked(pos, bbe);
				Bannerpoint.debugLog("Started tracking banner");
			} else {
				level.getWaypointManager().remakeConnections((WaypointTransmitter) bbe);
			}
			data.saveBanner(pos);
		} else {
			if (previous != null) {
				level.getWaypointManager().untrackWaypoint((WaypointTransmitter) bbe);
				data.removeTracked(pos);
				Bannerpoint.debugLog("Stopped tracking banner");
			}
			data.removeBanner(pos);
		}
	}

	public static void onBannerRemoved(ServerLevel level, BlockPos pos) {
		LevelSavedBanners data = getData(level);
		if (data == null) return;
		BannerBlockEntity tracked = data.removeTracked(pos);
		if (tracked != null) {
			level.getWaypointManager().untrackWaypoint((WaypointTransmitter) tracked);
			Bannerpoint.debugLog("Stopped tracking removed banner at {}", pos.toShortString());
		}
		data.removeBanner(pos);
		data.clearMapTies(pos);
	}

	public static boolean isTiedToMap(@Nullable ServerLevel level, BlockPos pos) {
		LevelSavedBanners data = level == null ? null : getData(level);
		return data != null && data.getMapTies(pos) > 0;
	}

	private static @Nullable LevelSavedBanners getData(ServerLevel level) {
		return ((ServerLevelExtension) level).bannerpoint$getSavedBanners();
	}

	public static Optional<WaypointTransmitter.Connection> createConnection(BannerBlockEntity bbe, ServerPlayer player, Waypoint.Icon icon) {
		if (doesSourceIgnoreReceiver(bbe, player)) return Optional.empty();
		if (isReallyFar(bbe, player)) return Optional.of(new BannerAzimuthConnection(bbe, icon, player));
		return !WaypointTransmitter.isChunkVisible(ChunkPos.containing(bbe.getBlockPos()), player) ?
				Optional.of(new BannerChunkConnection(bbe, icon, player)) :
				Optional.of(new BannerBlockConnection(bbe, icon, player));
	}

	public static void sendName(BannerBlockEntity bbe, ServerPlayer player) {
		Component customName = bbe.getCustomName();
		if (customName == null) return;
		NetworkingUtil.s2cIfSupported(player, new S2CBannerNamePayload(
				((BannerBlockEntityExtension) bbe).bannerpoint$getUUID(),
				customName,
				bbe.getBaseColor().getTextColor()
		));
	}

	public static void sendFarDistance(UUID uuid, ServerPlayer player, int distance) {
		if (!Bannerpoint.CONFIG.showDistance.get()) return;
		NetworkingUtil.s2cIfSupported(player, new S2CBannerDistancePayload(uuid, distance));
	}

	public static int roundedDistance(BannerBlockEntity bbe, ServerPlayer player) {
		return (int) Math.round(distance(bbe.getBlockPos(), player) / FAR_DISTANCE_STEP) * FAR_DISTANCE_STEP;
	}

	public static Waypoint.Icon createBannerIcon(DyeColor color) {
        return WaypointIconAccessor.bannerpoint$callInit(BANNER, Optional.of(color.getTextureDiffuseColor()));
	}

	public static boolean doesSourceIgnoreReceiver(final BannerBlockEntity source, final ServerPlayer receiver) {
		if (receiver.isSpectator()) return false;
		else return distance(source.getBlockPos(), receiver) >= Math.min(Bannerpoint.CONFIG.bannerTransmitRange.get(), receiver.getAttributeValue(Attributes.WAYPOINT_RECEIVE_RANGE));
	}

	public static boolean isReallyFar(final BannerBlockEntity source, final ServerPlayer receiver) {
		return distance(source.getBlockPos(), receiver) > 332;
	}

	private static double distance(final BlockPos source, final ServerPlayer receiver) {
		return Math.sqrt(receiver.distanceToSqr(source.getX(), source.getY(), source.getZ()));
	}
}
