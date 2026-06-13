package me.pajic.bannerpoint.waypoint;

import me.pajic.bannerpoint.Bannerpoint;
import me.pajic.bannerpoint.extension.BannerBlockEntityExtension;
import me.pajic.bannerpoint.extension.ServerLevelExtension;
import me.pajic.bannerpoint.networking.NetworkingUtil;
import me.pajic.bannerpoint.networking.S2CBannerNamePayload;
import me.pajic.bannerpoint.saveddata.LevelSavedBanners;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
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

import java.util.Optional;

public class BannerWaypointUtil {

	private static final ResourceKey<WaypointStyleAsset> BANNER = ResourceKey.create(WaypointStyleAssets.ROOT_ID, Bannerpoint.id("banner"));
	public static boolean savedDataLoadFinished = false;

	public static void startTrackingSaved(ServerLevel level, LevelSavedBanners data) {
		data.getBanners().forEach(pos -> {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof BannerBlockEntity bbe) startTracking(level, bbe, false);
		});
		savedDataLoadFinished = true;
		Bannerpoint.debugLog("Finished loading saved banners");
	}

	public static void startTrackingOnInit(ServerLevel level, BlockEntity be) {
		if (savedDataLoadFinished && level instanceof ServerLevel serverLevel && be instanceof BannerBlockEntity bbe) startTracking(serverLevel, bbe, true);
	}

	public static void setMapTracking(boolean shouldTrack, ServerLevel level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (Bannerpoint.CONFIG.transmitWhenTiedToMap.get() && be instanceof BannerBlockEntity bbe) {
			BannerBlockEntityExtension bbee = (BannerBlockEntityExtension) bbe;
			bbee.bannerpoint$setTiedToMap(shouldTrack);
			if (!bbee.bannerpoint$hasCustomName()) {
				if (shouldTrack) startTracking(level.getLevel(), bbe, true);
				else stopTracking(level.getLevel(), bbe);
			}
			bbe.setChanged();
		}
	}

	public static void startTracking(ServerLevel level, BannerBlockEntity bbe, boolean save) {
		WaypointTransmitter wt = (WaypointTransmitter) bbe;
		Bannerpoint.debugLog("Attempting to track banner at {}", bbe.getBlockPos().toShortString());
		if (wt.isTransmittingWaypoint()) {
			level.getWaypointManager().trackWaypoint(wt);
			Bannerpoint.debugLog("Started tracking banner");
		}
		if (save) {
			LevelSavedBanners data = ((ServerLevelExtension) level).bannerpoint$getSavedBanners();
			data.saveBanner(bbe.getBlockPos());
			Bannerpoint.debugLog("Saved banner to level data");
		}
	}

	public static void stopTracking(ServerLevel level, BannerBlockEntity bbe) {
		level.getWaypointManager().untrackWaypoint((WaypointTransmitter) bbe);
		LevelSavedBanners data = ((ServerLevelExtension) level).bannerpoint$getSavedBanners();
		data.removeBanner(bbe.getBlockPos());
	}

	@SuppressWarnings("resource")
	public static Optional<WaypointTransmitter.Connection> createConnection(BannerBlockEntity bbe, ServerPlayer player, Waypoint.Icon icon) {
		if (doesSourceIgnoreReceiver(bbe, player)) return Optional.empty();
		BannerBlockEntityExtension bbee = (BannerBlockEntityExtension) bbe;
		if (bbee.bannerpoint$hasCustomName() && bbe.hasCustomName()) player.level().players().forEach(serverPlayer ->
				NetworkingUtil.s2c(serverPlayer, new S2CBannerNamePayload(
						bbee.bannerpoint$getUUID(),
						MutableComponent.create(bbee.bannerpoint$getCustomName().getContents())
								.withColor(bbe.getBaseColor().getTextColor()))
				)
		);
		if (isReallyFar(bbe, player)) return Optional.of(new BannerAzimuthConnection(bbe, icon, player));
		return !WaypointTransmitter.isChunkVisible(ChunkPos.containing(bbe.getBlockPos()), player) ?
				Optional.of(new BannerChunkConnection(bbe, icon, player)) :
				Optional.of(new BannerBlockConnection(bbe, icon, player));
	}

	public static Waypoint.Icon createBannerIcon(DyeColor color) {
		return new Waypoint.Icon(BANNER, Optional.of(color.getTextureDiffuseColor()));
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
