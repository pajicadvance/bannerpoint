package me.pajic.bannerpoint.platform.fabric;

//? fabric {

import me.pajic.bannerpoint.Bannerpoint;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.bannerpoint.networking.S2CBannerNamePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		Bannerpoint.onInitialize();
		PayloadTypeRegistry.clientboundPlay().register(S2CBannerNamePayload.TYPE, S2CBannerNamePayload.CODEC);
	}
}
//?}
