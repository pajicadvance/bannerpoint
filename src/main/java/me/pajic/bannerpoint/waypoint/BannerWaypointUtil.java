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
import net.minecraft.world.level.Level;
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
		LevelSavedBanners data = ((ServerLevelExtension) level).bannerpoint$getSavedBanners();
		data.saveBanner(bbe.getBlockPos());
	}

	public static void stopTracking(ServerLevel level, BannerBlockEntity bbe) {
		level.getWaypointManager().untrackWaypoint((WaypointTransmitter) bbe);
		LevelSavedBanners data = ((ServerLevelExtension) level).bannerpoint$getSavedBanners();
		data.removeBanner(bbe.getBlockPos());
	}

	public static void startTrackingOnInit(Level level, BlockEntity be) {
		if (level instanceof ServerLevel serverLevel && be instanceof BannerBlockEntity bbe) startTracking(serverLevel, bbe);
	}

	public static void setMapTracking(boolean shouldTrack, LevelAccessor level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (Bannerpoint.CONFIG.transmitWhenTiedToMap.get() && be instanceof BannerBlockEntity bbe && level instanceof ServerLevelAccessor sla) {
			BannerBlockEntityExtension bbee = (BannerBlockEntityExtension) bbe;
			bbee.bannerpoint$setTiedToMap(shouldTrack);
			if (!bbee.bannerpoint$hasCustomName()) {
				if (shouldTrack) startTracking(sla.getLevel(), bbe);
				else stopTracking(sla.getLevel(), bbe);
			}
			bbe.setChanged();
		}
	}

	public static void startTrackingSaved(LevelAccessor level, LevelSavedBanners data) {
		if (level instanceof ServerLevelAccessor sla) {
			data.getBanners().forEach(pos -> {
				BlockEntity be = sla.getBlockEntity(pos);
				if (be instanceof BannerBlockEntity bbe) startTracking(sla.getLevel(), bbe);
			});
		}
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
		return distance(source.getBlockPos(), receiver) > WaypointTransmitter.REALLY_FAR_DISTANCE;
	}

	private static double distance(final BlockPos source, final ServerPlayer receiver) {
		return Math.sqrt(receiver.distanceToSqr(source.getCenter()));
	}
}
