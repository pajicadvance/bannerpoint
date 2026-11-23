package me.pajic.bannerpoint.networking;

import me.pajic.bannerpoint.Bannerpoint;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record S2CBannerDistancePayload(UUID uuid, int distance) implements CustomPacketPayload {
	public static final Identifier ID = Bannerpoint.id("banner_distance");
	public static final Type<S2CBannerDistancePayload> TYPE = new Type<>(ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CBannerDistancePayload> CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, S2CBannerDistancePayload::uuid,
			ByteBufCodecs.VAR_INT, S2CBannerDistancePayload::distance,
			S2CBannerDistancePayload::new
	);

	@Override @NotNull
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
