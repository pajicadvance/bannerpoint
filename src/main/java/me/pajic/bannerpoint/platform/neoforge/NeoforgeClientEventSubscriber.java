package me.pajic.bannerpoint.platform.neoforge;

//? neoforge {

/*import me.pajic.bannerpoint.Bannerpoint;
import me.pajic.bannerpoint.BannerpointClient;
import me.pajic.bannerpoint.client.BannerNameRenderer;
import me.pajic.bannerpoint.networking.S2CBannerNamePayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(modid = Bannerpoint.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEventSubscriber {

	@SubscribeEvent
	public static void onClientSetup(final FMLCommonSetupEvent event) {
		BannerpointClient.onInitialize();
	}

	@SubscribeEvent
	public static void register(RegisterClientPayloadHandlersEvent event) {
		event.register(S2CBannerNamePayload.TYPE, (payload, _) ->
				BannerNameRenderer.setName(payload)
		);
	}
}
*///?}
