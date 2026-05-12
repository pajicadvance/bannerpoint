package me.pajic.bannerpoint.waypoint;

import me.pajic.bannerpoint.extension.BannerBlockEntityExtension;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointTransmitter;

import java.util.UUID;

public class BannerAzimuthConnection implements WaypointTransmitter.Connection {

	private final BannerBlockEntity source;
	private final UUID uuid;
	private final Waypoint.Icon icon;
	private final ServerPlayer receiver;
	private float lastAngle;

	public BannerAzimuthConnection(final BannerBlockEntity source, final Waypoint.Icon icon, final ServerPlayer receiver) {
		this.source = source;
		this.icon = icon;
		this.receiver = receiver;
		Vec3 direction = receiver.position().subtract(source.getBlockPos().getCenter()).rotateClockwise90();
		lastAngle = (float) Mth.atan2(direction.z(), direction.x());
		uuid = ((BannerBlockEntityExtension) source).bannerpoint$getUUID();
	}

	@Override
	public boolean isBroken() {
		return BannerWaypointUtil.doesSourceIgnoreReceiver(source, receiver) ||
				WaypointTransmitter.isChunkVisible(ChunkPos.containing(source.getBlockPos()), receiver) ||
				!BannerWaypointUtil.isReallyFar(source, receiver);
	}

	@Override
	public void connect() {
		receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointAzimuth(uuid, icon, lastAngle));
	}

	@Override
	public void disconnect() {
		receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(uuid));
	}

	@Override
	public void update() {
		Vec3 direction = receiver.position().subtract(source.getBlockPos().getCenter()).rotateClockwise90();
		float currentAngle = (float) Mth.atan2(direction.z(), direction.x());
		if (Mth.abs(currentAngle - lastAngle) > 0.008726646F) {
			receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointAzimuth(uuid, icon, currentAngle));
			lastAngle = currentAngle;
		}
	}
}
