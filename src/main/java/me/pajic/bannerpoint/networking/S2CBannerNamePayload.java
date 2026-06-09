package me.pajic.bannerpoint.networking;

import me.pajic.bannerpoint.Bannerpoint;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record S2CBannerNamePayload(UUID uuid, Component name) implements CustomPacketPayload {
	public static final Identifier ID = Bannerpoint.id("banner_name");
	public static final CustomPacketPayload.Type<S2CBannerNamePayload> TYPE = new CustomPacketPayload.Type<>(ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CBannerNamePayload> CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, S2CBannerNamePayload::uuid,
			ComponentSerialization.STREAM_CODEC, S2CBannerNamePayload::name,
			S2CBannerNamePayload::new
	);

	@Override @NotNull
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
