package me.pajic.bannerpoint.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.bannerpoint.Bannerpoint;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ModKeybinds {

	public static final Identifier CATEGORY_ID = Bannerpoint.id("keys");
	public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(CATEGORY_ID);
	public static final KeyMapping SHOW_NAMES = new KeyMapping("key.bannerpoint.show_names", InputConstants.UNKNOWN.getValue(), CATEGORY);
}
