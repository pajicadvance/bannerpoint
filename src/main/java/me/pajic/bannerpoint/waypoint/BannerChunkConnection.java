package me.pajic.bannerpoint.waypoint;

import me.pajic.bannerpoint.extension.BannerBlockEntityExtension;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointTransmitter;

import java.util.UUID;

public class BannerChunkConnection implements WaypointTransmitter.ChunkConnection {

	private final BannerBlockEntity source;
	private final UUID uuid;
	private final Waypoint.Icon icon;
	private final ServerPlayer receiver;
	private ChunkPos lastPosition;

	public BannerChunkConnection(final BannerBlockEntity source, final Waypoint.Icon icon, final ServerPlayer receiver) {
		this.source = source;
		this.icon = icon;
		this.receiver = receiver;
		lastPosition = ChunkPos.containing(source.getBlockPos());
		uuid = ((BannerBlockEntityExtension) source).bannerpoint$getUUID();
	}

	@Override
	public int distanceChessboard() {
		return lastPosition.getChessboardDistance(ChunkPos.containing(source.getBlockPos()));
	}

	@Override
	public void connect() {
		receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointChunk(uuid, icon, lastPosition));
	}

	@Override
	public void disconnect() {
		receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(uuid));
	}

	@Override
	public void update() {
		ChunkPos currentPosition = ChunkPos.containing(source.getBlockPos());
		if (currentPosition.getChessboardDistance(lastPosition) > 0) {
			receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointChunk(uuid, icon, currentPosition));
			lastPosition = currentPosition;
		}
	}

	@Override
	public boolean isBroken() {
		return WaypointTransmitter.ChunkConnection.super.isBroken() ||
				BannerWaypointUtil.doesSourceIgnoreReceiver(source, receiver) ||
				WaypointTransmitter.isChunkVisible(lastPosition, receiver);
	}
}
