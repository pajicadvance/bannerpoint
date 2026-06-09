package me.pajic.bannerpoint.platform.neoforge;

//? neoforge {

/*import me.pajic.bannerpoint.Bannerpoint;
import me.pajic.bannerpoint.networking.S2CBannerNamePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Bannerpoint.MOD_ID)
@EventBusSubscriber(modid = Bannerpoint.MOD_ID)
public class NeoforgeEntrypoint {

	@SubscribeEvent
	private static void onCommonSetup(FMLCommonSetupEvent event) {
		Bannerpoint.onInitialize();
	}

	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1");
		registrar.playToClient(S2CBannerNamePayload.TYPE, S2CBannerNamePayload.CODEC);
	}
}
*///?}
