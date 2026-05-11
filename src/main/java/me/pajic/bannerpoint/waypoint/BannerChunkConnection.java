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
		return this.lastPosition.getChessboardDistance(ChunkPos.containing(source.getBlockPos()));
	}

	@Override
	public void connect() {
		this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointChunk(uuid, this.icon, this.lastPosition));
	}

	@Override
	public void disconnect() {
		this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(uuid));
	}

	@Override
	public void update() {
		ChunkPos currentPosition = ChunkPos.containing(source.getBlockPos());
		if (currentPosition.getChessboardDistance(this.lastPosition) > 0) {
			this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointChunk(uuid, this.icon, currentPosition));
			this.lastPosition = currentPosition;
		}
	}

	@Override
	public boolean isBroken() {
		return WaypointTransmitter.ChunkConnection.super.isBroken() ||
				BannerWaypointUtil.doesSourceIgnoreReceiver(this.source, this.receiver) ||
				WaypointTransmitter.isChunkVisible(this.lastPosition, this.receiver);
	}
}
