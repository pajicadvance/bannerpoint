package me.pajic.bannerpoint.waypoint;

import me.pajic.bannerpoint.extension.BannerBlockEntityExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointTransmitter;

import java.util.UUID;

public class BannerBlockConnection implements WaypointTransmitter.BlockConnection {

	private final BannerBlockEntity source;
	private final UUID uuid;
	private final Waypoint.Icon icon;
	private final ServerPlayer receiver;
	private BlockPos lastPosition;

	public BannerBlockConnection(final BannerBlockEntity source, final Waypoint.Icon icon, final ServerPlayer receiver) {
		this.source = source;
		this.receiver = receiver;
		this.icon = icon;
		lastPosition = source.getBlockPos();
		uuid = ((BannerBlockEntityExtension) source).bannerpoint$getUUID();
	}

	@Override
	public void connect() {
		receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(uuid, icon, lastPosition));
	}

	@Override
	public void disconnect() {
		receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(uuid));
	}

	@Override
	public void update() {
		BlockPos currentPosition = source.getBlockPos();
		if (currentPosition.distManhattan(lastPosition) > 0) {
			receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointPosition(uuid, icon, currentPosition));
			lastPosition = currentPosition;
		}
	}

	@Override
	public int distanceManhattan() {
		return lastPosition.distManhattan(source.getBlockPos());
	}

	@Override
	public boolean isBroken() {
		return WaypointTransmitter.BlockConnection.super.isBroken() || BannerWaypointUtil.doesSourceIgnoreReceiver(source, receiver);
	}
}
