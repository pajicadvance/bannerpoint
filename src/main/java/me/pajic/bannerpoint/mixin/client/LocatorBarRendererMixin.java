package me.pajic.bannerpoint.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.bannerpoint.client.BannerNameRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//~ if >=26.2 'LocatorBarRenderer' -> 'LocatorBar' {
import net.minecraft.client.gui.contextualbar.LocatorBar;

@Mixin(LocatorBar.class)
//~}
public class LocatorBarRendererMixin {

	@Shadow @Final private Minecraft minecraft;

	@Inject(
			method = "lambda$extractRenderState$1",
			at = @At(
					value = "INVOKE",
                    //~ if >26.2 'blaze3d' -> 'renderpearl/api'
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"
			)
	)
	private void renderBannerNames(
			Entity cameraEntity,
			Level level,
			PartialTickSupplier partialTickSupplier,
			GuiGraphicsExtractor graphics,
			int top,
			TrackedWaypoint waypoint,
			CallbackInfo ci,
			@Local(name = "angle") double angle,
			@Local(name = "screenMiddle") int screenMiddle,
			@Local(name = "dotPosition") int dotPosition
	) {
		BannerNameRenderer.renderBannerName(minecraft, graphics, waypoint, screenMiddle, dotPosition, top, angle);
	}
}
