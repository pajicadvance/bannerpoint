package me.pajic.bannerpoint.mixin;

import me.pajic.bannerpoint.Bannerpoint;
import me.pajic.bannerpoint.extension.BannerBlockEntityExtension;
import me.pajic.bannerpoint.waypoint.BannerAzimuthConnection;
import me.pajic.bannerpoint.waypoint.BannerBlockConnection;
import me.pajic.bannerpoint.waypoint.BannerChunkConnection;
import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(BannerBlockEntity.class)
public abstract class BannerBlockEntityMixin extends BlockEntity implements WaypointTransmitter, BannerBlockEntityExtension, Nameable {

	public BannerBlockEntityMixin(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
		super(type, worldPosition, blockState);
	}

	@Shadow public abstract DyeColor getBaseColor();

	@Unique private Waypoint.Icon bannerpoint$icon = new Waypoint.Icon();
	@Unique private UUID bannerpoint$uuid;
	@Unique private boolean bannerpoint$tiedToMap;
	@Unique private boolean bannerpoint$hasCustomName;

	@Inject(
			method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/DyeColor;)V",
			at = @At("TAIL")
	)
	private void onInit(BlockPos worldPosition, BlockState blockState, DyeColor color, CallbackInfo ci) {
		bannerpoint$icon = BannerWaypointUtil.createBannerIcon(color.getTextColor());
		bannerpoint$uuid = UUID.randomUUID();
		bannerpoint$tiedToMap = false;
	}

	@Override
	public boolean isTransmittingWaypoint() {
		if (hasCustomName()) bannerpoint$hasCustomName = true;
		if (Bannerpoint.CONFIG.transmitWhenNamed.get() && bannerpoint$hasCustomName) return true;
		return Bannerpoint.CONFIG.transmitWhenTiedToMap.get() && bannerpoint$tiedToMap;
	}

	@Override @NotNull
	public Optional<Connection> makeWaypointConnectionWith(@NotNull ServerPlayer player) {
		BannerBlockEntity self = (BannerBlockEntity) (Object) this;
		if (BannerWaypointUtil.doesSourceIgnoreReceiver(self, player)) return Optional.empty();
		if (BannerWaypointUtil.isReallyFar(self, player)) return Optional.of(new BannerAzimuthConnection(self, bannerpoint$icon, player));
		return !WaypointTransmitter.isChunkVisible(ChunkPos.containing(self.getBlockPos()), player) ?
					Optional.of(new BannerChunkConnection(self, bannerpoint$icon, player)) :
					Optional.of(new BannerBlockConnection(self, bannerpoint$icon, player));
	}

	@Override @NotNull
	public Icon waypointIcon() {
		return bannerpoint$icon;
	}

	@Override
    public UUID bannerpoint$getUUID() {
        return bannerpoint$uuid;
    }

	@Override
	public void bannerpoint$setTiedToMap(boolean tiedToMap) {
		bannerpoint$tiedToMap = tiedToMap;
	}

	@Override
	public boolean bannerpoint$hasCustomName() {
		return bannerpoint$hasCustomName;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		output.store("locator_bar_icon", Waypoint.Icon.CODEC, bannerpoint$icon);
		output.store("uuid", UUIDUtil.CODEC, bannerpoint$uuid);
		output.putBoolean("tied_to_map", bannerpoint$tiedToMap);
		output.putBoolean("has_custom_name", bannerpoint$hasCustomName);
		super.saveAdditional(output);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		bannerpoint$icon = input.read("locator_bar_icon", Icon.CODEC).orElse(BannerWaypointUtil.createBannerIcon(getBaseColor().getTextColor()));
		bannerpoint$uuid = input.read("uuid", UUIDUtil.CODEC).orElse(UUID.randomUUID());
		bannerpoint$tiedToMap = input.getBooleanOr("tied_to_map", false);
		bannerpoint$hasCustomName = input.getBooleanOr("has_custom_name", false);
		super.loadAdditional(input);
	}
}
