package me.pajic.bannerpoint.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.pajic.bannerpoint.Bannerpoint;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LevelSavedBanners extends SavedData {

	private final Set<BlockPos> banners = new HashSet<>();
	private final Map<BlockPos, Integer> mapTies = new HashMap<>();
	private final Map<BlockPos, BannerBlockEntity> tracked = new HashMap<>();

	private record MapTie(BlockPos pos, int count) {
		private static final Codec<MapTie> CODEC = RecordCodecBuilder.create(
				instance -> instance.group(
						BlockPos.CODEC.fieldOf("pos").forGetter(MapTie::pos),
						ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(MapTie::count)
				).apply(instance, MapTie::new)
		);
	}

	public static final Codec<LevelSavedBanners> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					BlockPos.CODEC.listOf().fieldOf("banners").forGetter(o -> List.copyOf(o.banners)),
					MapTie.CODEC.listOf().optionalFieldOf("map_ties", List.of()).forGetter(o -> o.mapTies.entrySet().stream()
							.map(e -> new MapTie(e.getKey(), e.getValue()))
							.toList())
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

	public LevelSavedBanners() {}

	private LevelSavedBanners(List<BlockPos> banners, List<MapTie> mapTies) {
		this.banners.addAll(banners);
		mapTies.forEach(tie -> this.mapTies.put(tie.pos(), tie.count()));
	}

	public void saveBanner(BlockPos pos) {
		if (banners.add(pos.immutable())) setDirty();
	}

	public void removeBanner(BlockPos pos) {
		if (banners.remove(pos)) setDirty();
	}

	public Set<BlockPos> getBanners() {
		return banners;
	}

	public int getMapTies(BlockPos pos) {
		return mapTies.getOrDefault(pos, 0);
	}

	public void addMapTie(BlockPos pos) {
		mapTies.merge(pos.immutable(), 1, Integer::sum);
		setDirty();
	}

	public void removeMapTie(BlockPos pos) {
		Integer count = mapTies.get(pos);
		if (count == null) return;
		if (count <= 1) mapTies.remove(pos);
		else mapTies.put(pos, count - 1);
		setDirty();
	}

	public void clearMapTies(BlockPos pos) {
		if (mapTies.remove(pos) != null) setDirty();
	}

	public @Nullable BannerBlockEntity getTracked(BlockPos pos) {
		return tracked.get(pos);
	}

	public void setTracked(BlockPos pos, BannerBlockEntity bbe) {
		tracked.put(pos.immutable(), bbe);
	}

	public @Nullable BannerBlockEntity removeTracked(BlockPos pos) {
		return tracked.remove(pos);
	}
}
