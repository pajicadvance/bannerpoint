package me.pajic.bannerpoint.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

//? fabric {
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//?} neoforge {
/*import net.neoforged.neoforge.network.PacketDistributor;
*///?}

public class NetworkingUtil {

	public static void s2c(ServerPlayer player, CustomPacketPayload payload) {
		//? fabric {
		ServerPlayNetworking.send(player, payload);
		//?} neoforge {
		/*PacketDistributor.sendToPlayer(player, payload);
		*///?}
	}
}
