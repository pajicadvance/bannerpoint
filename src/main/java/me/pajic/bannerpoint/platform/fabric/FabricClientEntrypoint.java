package me.pajic.bannerpoint.platform.fabric;

//? fabric {

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.bannerpoint.BannerpointClient;
import me.pajic.bannerpoint.client.BannerNameRenderer;
import me.pajic.bannerpoint.client.ModKeybinds;
import me.pajic.bannerpoint.networking.S2CBannerDistancePayload;
import me.pajic.bannerpoint.networking.S2CBannerNamePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BannerpointClient.onInitialize();
        ClientPlayNetworking.registerGlobalReceiver(S2CBannerNamePayload.TYPE, (payload, _) ->
                BannerNameRenderer.setName(payload)
        );
        ClientPlayNetworking.registerGlobalReceiver(S2CBannerDistancePayload.TYPE, (payload, _) ->
                BannerNameRenderer.setFarDistance(payload)
        );
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> BannerNameRenderer.clear());
        KeyMapping.Category.register(ModKeybinds.CATEGORY_ID);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.SHOW_NAMES);
        ClientTickEvents.END_CLIENT_TICK.register(_ -> BannerNameRenderer.tick());
	}
}
//?}
