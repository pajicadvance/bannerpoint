package me.pajic.bannerpoint.platform.fabric;

//? fabric {

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.bannerpoint.BannerpointClient;
import me.pajic.bannerpoint.client.BannerNameRenderer;
import me.pajic.bannerpoint.networking.S2CBannerNamePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BannerpointClient.onInitialize();
		ClientPlayNetworking.registerGlobalReceiver(S2CBannerNamePayload.TYPE, (payload, _) ->
				BannerNameRenderer.setName(payload)
		);
	}
}
//?}
