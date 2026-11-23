package me.pajic.bannerpoint.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

//? fabric {
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//?} neoforge {
/*import net.neoforged.neoforge.network.PacketDistributor;
*///?}

public class NetworkingUtil {

	public static boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
		//? fabric {
		return ServerPlayNetworking.canSend(player, type);
		//?} neoforge {
		/*return player.connection.hasChannel(type);
		*///?}
	}

	public static void s2cIfSupported(ServerPlayer player, CustomPacketPayload payload) {
		if (canSend(player, payload.type())) s2c(player, payload);
	}

	public static void s2c(ServerPlayer player, CustomPacketPayload payload) {
		//? fabric {
		ServerPlayNetworking.send(player, payload);
		//?} neoforge {
		/*PacketDistributor.sendToPlayer(player, payload);
		*///?}
	}
}
