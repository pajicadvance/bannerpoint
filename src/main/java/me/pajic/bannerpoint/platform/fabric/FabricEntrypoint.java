package me.pajic.bannerpoint.platform.fabric;

//? fabric {

import me.pajic.bannerpoint.Bannerpoint;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		Bannerpoint.onInitialize();
	}
}
//?}
