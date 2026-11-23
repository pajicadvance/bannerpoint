package me.pajic.bannerpoint.mixin;

import me.pajic.bannerpoint.Bannerpoint;
import me.pajic.bannerpoint.extension.BannerBlockEntityExtension;
import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Mixin(BannerBlockEntity.class)
public abstract class BannerBlockEntityMixin extends BlockEntity implements WaypointTransmitter, BannerBlockEntityExtension, Nameable {

	public BannerBlockEntityMixin(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
		super(type, worldPosition, blockState);
	}

	@Shadow public abstract DyeColor getBaseColor();

	@Unique private Icon bannerpoint$icon = new Icon();
	@Unique private @Nullable UUID bannerpoint$uuid;
	@Unique private boolean bannerpoint$legacyTiedToMap;

	@Inject(
			method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/DyeColor;)V",
			at = @At("TAIL")
	)
	private void onInit(BlockPos worldPosition, BlockState blockState, DyeColor color, CallbackInfo ci) {
		bannerpoint$icon = BannerWaypointUtil.createBannerIcon(color);
	}

	@Inject(
			method = "saveAdditional",
			at = @At("TAIL")
	)
	private void saveModData(ValueOutput output, CallbackInfo ci) {
		output.store("locator_bar_icon", Icon.CODEC, bannerpoint$icon);
	}

	@Inject(
			method = "loadAdditional",
			at = @At("TAIL")
	)
	private void loadModData(ValueInput input, CallbackInfo ci) {
		bannerpoint$icon = input.read("locator_bar_icon", Icon.CODEC).orElse(BannerWaypointUtil.createBannerIcon(getBaseColor()));
		bannerpoint$legacyTiedToMap = input.getBooleanOr("tied_to_map", false);
	}

	@Override
	public boolean isTransmittingWaypoint() {
		if (Bannerpoint.CONFIG.transmitWhenNamed.get() && hasCustomName()) return true;
		return Bannerpoint.CONFIG.transmitWhenTiedToMap.get() &&
				level instanceof ServerLevel serverLevel &&
				BannerWaypointUtil.isTiedToMap(serverLevel, worldPosition);
	}

	@Override @NotNull
	public Optional<Connection> makeWaypointConnectionWith(@NotNull ServerPlayer player) {
		return BannerWaypointUtil.createConnection((BannerBlockEntity) (Object) this, player, bannerpoint$icon);
	}

	@Override @NotNull
	public Icon waypointIcon() {
		return bannerpoint$icon;
	}

	@Override
	public UUID bannerpoint$getUUID() {
		if (bannerpoint$uuid != null) return bannerpoint$uuid;
		String dimension = level != null ? level.dimension().identifier().toString() : "";
		UUID uuid = UUID.nameUUIDFromBytes((Bannerpoint.MOD_ID + ":" + dimension + ":" + worldPosition.asLong()).getBytes(StandardCharsets.UTF_8));
		if (level != null) bannerpoint$uuid = uuid;
		return uuid;
	}

	@Override
	public boolean bannerpoint$consumeLegacyTiedToMap() {
		boolean legacy = bannerpoint$legacyTiedToMap;
		bannerpoint$legacyTiedToMap = false;
		return legacy;
	}
}
