package me.pajic.bannerpoint.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.pajic.bannerpoint.Bannerpoint;
import net.minecraft.core.BlockPos;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LevelSavedBanners extends SavedData {

	private final Set<BlockPos> banners = new HashSet<>();

	public static final Codec<LevelSavedBanners> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					BlockPos.CODEC.listOf().fieldOf("banners").forGetter(o -> o.banners.stream().toList())
			).apply(instance, LevelSavedBanners::new)
	);

	public static final SavedDataType<LevelSavedBanners> TYPE = new SavedDataType<>(
			Bannerpoint.id("banners"),
			LevelSavedBanners::new,
			CODEC,
			DataFixTypes.LEVEL
	);

	public static SavedDataType<LevelSavedBanners> getType() {
		return TYPE;
	}

	public LevelSavedBanners() {
		setDirty();
	}

	private LevelSavedBanners(List<BlockPos> banners) {
		this.banners.addAll(banners);
		setDirty();
	}

	public void saveBanner(BlockPos pos) {
		if (banners.add(pos)) setDirty();
	}

	public void removeBanner(BlockPos pos) {
		if (banners.remove(pos)) setDirty();
	}

	public Set<BlockPos> getBanners() {
		return banners;
	}
}
