package me.pajic.bannerpoint.client;

import me.pajic.bannerpoint.BannerpointClient;
import me.pajic.bannerpoint.networking.S2CBannerNamePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.world.waypoints.TrackedWaypoint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BannerNameRenderer {

	private static final Map<UUID, Component> uuidToName = new HashMap<>();

	public static void setName(S2CBannerNamePayload payload) {
		uuidToName.put(payload.uuid(), payload.name());
	}

	public static Component getName(UUID uuid) {
		return uuidToName.getOrDefault(uuid, Component.empty());
	}

	public static void renderBannerName(Minecraft minecraft, GuiGraphicsExtractor graphics, TrackedWaypoint waypoint, int screenMiddle, int dotPosition, int top, double angle) {
		if (minecraft.options.keyPlayerList.isDown() || (minecraft.options.keyShift.isDown() && angle <= 0.5 && angle >= -0.5)) {
			waypoint.id().left().ifPresent(uuid -> {
				Component name = BannerpointClient.CONFIG.coloredText.get() ?
						BannerNameRenderer.getName(uuid) :
						MutableComponent.create(BannerNameRenderer.getName(uuid).getContents()).withColor(0xffffffff);
				Font font = minecraft.font;
				int x = screenMiddle + dotPosition + 5 - font.width(name) / 2;
				int y = top - 12;
				if (BannerpointClient.CONFIG.textBackground.get()) {
					graphics.fill(
							x - 2, y - 2, x + font.width(name) + 2, y + 10,
							ARGB.color(ARGB.as8BitChannel((float) BannerpointClient.CONFIG.textBackgroundOpacity.get() / 100), 0, 0, 0)
					);
				}
				graphics.text(font, name, x, y, 0xffffffff, BannerpointClient.CONFIG.textShadow.get());
			});
		}
	}
}
