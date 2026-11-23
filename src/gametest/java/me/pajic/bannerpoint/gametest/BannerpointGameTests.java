package me.pajic.bannerpoint.gametest;

import me.pajic.bannerpoint.Bannerpoint;
import me.pajic.bannerpoint.extension.BannerBlockEntityExtension;
import me.pajic.bannerpoint.extension.ServerLevelExtension;
import me.pajic.bannerpoint.saveddata.LevelSavedBanners;
import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BannerpointGameTests {

	private static final Block WHITE_BANNER_BLOCK = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("white_banner"));
	private static final Item WHITE_BANNER_ITEM = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("white_banner"));
	private static final BlockPos BANNER = new BlockPos(1, 1, 1);
	private static final BlockPos OTHER_BANNER = new BlockPos(5, 1, 5);

	@GameTest
	public void namedBannerIsTracked(GameTestHelper helper) {
		withConfig(true, true, () -> {
			BannerBlockEntity banner = placeBanner(helper, BANNER, "Base");
			assertTracked(helper, banner);
			helper.assertTrue(data(helper).getBanners().contains(helper.absolutePos(BANNER)), "Named banner was not saved");
		});
		helper.succeed();
	}

	@GameTest
	public void unnamedBannerIsNotTrackedOrSaved(GameTestHelper helper) {
		withConfig(true, true, () -> {
			BannerBlockEntity banner = placeBanner(helper, BANNER, null);
			assertNotTracked(helper, banner);
			helper.assertFalse(data(helper).getBanners().contains(helper.absolutePos(BANNER)), "Unnamed banner was saved");
		});
		helper.succeed();
	}

	@GameTest
	public void namedBannerIsNotTrackedWhenNamedTransmitDisabled(GameTestHelper helper) {
		withConfig(false, true, () -> assertNotTracked(helper, placeBanner(helper, BANNER, "Base")));
		helper.succeed();
	}

	@GameTest
	public void removingNameStopsTracking(GameTestHelper helper) {
		withConfig(true, true, () -> {
			BannerBlockEntity banner = placeBanner(helper, BANNER, "Base");
			assertTracked(helper, banner);
			banner.applyComponentsFromItemStack(new ItemStack(WHITE_BANNER_ITEM));
			assertNotTracked(helper, banner);
			helper.assertFalse(data(helper).getBanners().contains(helper.absolutePos(BANNER)), "Banner stayed saved after losing its name");
		});
		helper.succeed();
	}

	@GameTest
	public void breakingSupportBlockStopsTracking(GameTestHelper helper) {
		withConfig(true, true, () -> {
			BannerBlockEntity banner = placeBanner(helper, BANNER, "Base");
			assertTracked(helper, banner);
			helper.setBlock(BANNER.below(), Blocks.AIR);
			helper.assertBlockNotPresent(WHITE_BANNER_BLOCK, BANNER);
			assertNothingTrackedAt(helper, BANNER);
			helper.assertFalse(data(helper).getBanners().contains(helper.absolutePos(BANNER)), "Banner stayed saved after its support broke");
		});
		helper.succeed();
	}

	@GameTest
	public void removalWithoutSideEffectsStopsTracking(GameTestHelper helper) {
		withConfig(true, true, () -> {
			BannerBlockEntity banner = placeBanner(helper, BANNER, "Base");
			assertTracked(helper, banner);
			helper.getLevel().setBlock(
					helper.absolutePos(BANNER),
					Blocks.AIR.defaultBlockState(),
					Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS
			);
			assertNothingTrackedAt(helper, BANNER);
		});
		helper.succeed();
	}

	@GameTest
	public void chunkReloadReplacesTrackedInstance(GameTestHelper helper) {
		withConfig(true, true, () -> {
			ServerLevel level = helper.getLevel();
			BannerBlockEntity original = placeBanner(helper, BANNER, "Base");
			UUID uuid = ((BannerBlockEntityExtension) original).bannerpoint$getUUID();
			BlockPos pos = helper.absolutePos(BANNER);
			CompoundTag tag = original.saveWithFullMetadata(level.registryAccess());
			BlockEntity reloaded = BlockEntity.loadStatic(pos, original.getBlockState(), tag, level.registryAccess());
			helper.assertTrue(reloaded instanceof BannerBlockEntity, "Reloaded block entity is not a banner");
			level.getChunkAt(pos).setBlockEntity(reloaded);
			assertNotTracked(helper, original);
			assertTracked(helper, (BannerBlockEntity) reloaded);
			helper.assertValueEqual(countTrackedAt(helper, BANNER), 1L, "tracked banners at position");
			helper.assertValueEqual(((BannerBlockEntityExtension) reloaded).bannerpoint$getUUID(), uuid, "UUID after reload");
			helper.setBlock(BANNER, Blocks.AIR);
			assertNothingTrackedAt(helper, BANNER);
		});
		helper.succeed();
	}

	@GameTest
	public void copiedBannerDataGetsOwnUuidAndNoTie(GameTestHelper helper) {
		withConfig(true, true, () -> {
			ServerLevel level = helper.getLevel();
			BannerBlockEntity original = placeBanner(helper, BANNER, null);
			BannerWaypointUtil.setMapTracking(true, level, helper.absolutePos(BANNER));
			assertTracked(helper, original);
			CompoundTag tag = original.saveCustomOnly(level.registryAccess());
			BannerBlockEntity copy = placeBanner(helper, OTHER_BANNER, null);
			copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
			assertNotTracked(helper, copy);
			helper.assertValueEqual(data(helper).getMapTies(helper.absolutePos(OTHER_BANNER)), 0, "map ties on copy");
			helper.assertFalse(
					((BannerBlockEntityExtension) copy).bannerpoint$getUUID().equals(((BannerBlockEntityExtension) original).bannerpoint$getUUID()),
					"Copied banner shares UUID with original"
			);
		});
		helper.succeed();
	}

	@GameTest
	public void copiedNamedBannerIsTrackedSeparately(GameTestHelper helper) {
		withConfig(true, true, () -> {
			ServerLevel level = helper.getLevel();
			BannerBlockEntity original = placeBanner(helper, BANNER, "Base");
			CompoundTag tag = original.saveCustomOnly(level.registryAccess());
			BannerBlockEntity copy = placeBanner(helper, OTHER_BANNER, null);
			copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
			assertTracked(helper, original);
			assertTracked(helper, copy);
			helper.assertFalse(
					((BannerBlockEntityExtension) copy).bannerpoint$getUUID().equals(((BannerBlockEntityExtension) original).bannerpoint$getUUID()),
					"Copied banner shares UUID with original"
			);
		});
		helper.succeed();
	}

	@GameTest
	public void mapTiesAreCountedAcrossMaps(GameTestHelper helper) {
		withConfig(true, true, () -> {
			ServerLevel level = helper.getLevel();
			BlockPos relative = mapSafePos(helper);
			BannerBlockEntity banner = placeBanner(helper, relative, null);
			BlockPos pos = helper.absolutePos(relative);
			MapItemSavedData mapA = MapItemSavedData.createFresh(pos.getX(), pos.getZ(), (byte) 0, false, false, level.dimension());
			MapItemSavedData mapB = MapItemSavedData.createFresh(pos.getX(), pos.getZ(), (byte) 0, false, false, level.dimension());
			helper.assertTrue(mapA.toggleBanner(level, pos), "Map A did not accept banner");
			helper.assertTrue(mapB.toggleBanner(level, pos), "Map B did not accept banner");
			helper.assertValueEqual(data(helper).getMapTies(pos), 2, "map ties after two maps");
			assertTracked(helper, banner);
			mapA.toggleBanner(level, pos);
			helper.assertValueEqual(data(helper).getMapTies(pos), 1, "map ties after removing from one map");
			assertTracked(helper, banner);
			mapB.toggleBanner(level, pos);
			helper.assertValueEqual(data(helper).getMapTies(pos), 0, "map ties after removing from both maps");
			assertNotTracked(helper, banner);
		});
		helper.succeed();
	}

	@GameTest
	public void mapTieTracksNamedBannerWhenNamedTransmitDisabled(GameTestHelper helper) {
		withConfig(false, true, () -> {
			ServerLevel level = helper.getLevel();
			BannerBlockEntity banner = placeBanner(helper, BANNER, "Base");
			BlockPos pos = helper.absolutePos(BANNER);
			assertNotTracked(helper, banner);
			BannerWaypointUtil.setMapTracking(true, level, pos);
			assertTracked(helper, banner);
			BannerWaypointUtil.setMapTracking(false, level, pos);
			assertNotTracked(helper, banner);
		});
		helper.succeed();
	}

	@GameTest
	public void mapTiesAreRecordedWhileMapTransmitDisabled(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(BANNER);
		BannerBlockEntity[] banner = new BannerBlockEntity[1];
		withConfig(true, false, () -> {
			banner[0] = placeBanner(helper, BANNER, null);
			BannerWaypointUtil.setMapTracking(true, level, pos);
			assertNotTracked(helper, banner[0]);
			helper.assertValueEqual(data(helper).getMapTies(pos), 1, "map ties while disabled");
		});
		withConfig(true, true, () -> {
			BannerWaypointUtil.refreshTracking(level, banner[0]);
			assertTracked(helper, banner[0]);
		});
		helper.succeed();
	}

	@GameTest
	public void removingBannerClearsMapTies(GameTestHelper helper) {
		withConfig(true, true, () -> {
			BlockPos pos = helper.absolutePos(BANNER);
			placeBanner(helper, BANNER, null);
			BannerWaypointUtil.setMapTracking(true, helper.getLevel(), pos);
			helper.setBlock(BANNER, Blocks.AIR);
			helper.assertValueEqual(data(helper).getMapTies(pos), 0, "map ties after removal");
			BannerBlockEntity replacement = placeBanner(helper, BANNER, null);
			assertNotTracked(helper, replacement);
		});
		helper.succeed();
	}

	@GameTest
	public void legacyTiedToMapIsMigrated(GameTestHelper helper) {
		withConfig(true, true, () -> {
			ServerLevel level = helper.getLevel();
			BannerBlockEntity banner = placeBanner(helper, BANNER, null);
			CompoundTag legacy = new CompoundTag();
			legacy.putBoolean("tied_to_map", true);
			banner.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), legacy));
			helper.assertValueEqual(data(helper).getMapTies(helper.absolutePos(BANNER)), 1, "map ties after migration");
			assertTracked(helper, banner);
			CompoundTag saved = banner.saveWithoutMetadata(level.registryAccess());
			helper.assertFalse(saved.contains("tied_to_map"), "Banner still saves tied_to_map");
			helper.assertFalse(saved.contains("uuid"), "Banner still saves uuid");
			helper.assertFalse(saved.contains("has_custom_name"), "Banner still saves has_custom_name");
		});
		helper.succeed();
	}

	@GameTest
	public void uuidIsStableForPosition(GameTestHelper helper) {
		BannerBlockEntity banner = placeBanner(helper, BANNER, null);
		UUID first = ((BannerBlockEntityExtension) banner).bannerpoint$getUUID();
		helper.setBlock(BANNER, Blocks.AIR);
		BannerBlockEntity replacement = placeBanner(helper, BANNER, null);
		helper.assertValueEqual(((BannerBlockEntityExtension) replacement).bannerpoint$getUUID(), first, "UUID at same position");
		BannerBlockEntity other = placeBanner(helper, OTHER_BANNER, null);
		helper.assertFalse(((BannerBlockEntityExtension) other).bannerpoint$getUUID().equals(first), "Different positions share a UUID");
		helper.succeed();
	}

	@GameTest
	public void staleSavedPositionsAreDropped(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(BANNER);
		LevelSavedBanners data = data(helper);
		data.saveBanner(pos);
		BannerWaypointUtil.startTrackingSaved(helper.getLevel(), data);
		helper.assertFalse(data.getBanners().contains(pos), "Stale saved banner position was kept");
		helper.succeed();
	}

	@GameTest
	public void savedDataRoundTrips(GameTestHelper helper) {
		BlockPos pos = new BlockPos(10, 64, -20);
		LevelSavedBanners data = new LevelSavedBanners();
		data.saveBanner(pos);
		data.addMapTie(pos);
		data.addMapTie(pos);
		Tag tag = LevelSavedBanners.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
		LevelSavedBanners decoded = LevelSavedBanners.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
		helper.assertTrue(decoded.getBanners().contains(pos), "Decoded data lost banner position");
		helper.assertValueEqual(decoded.getMapTies(pos), 2, "decoded map ties");
		helper.succeed();
	}

	@GameTest
	public void connectionToPlayerWithoutChannelWorks(GameTestHelper helper) {
		withConfig(true, true, () -> {
			BannerBlockEntity banner = placeBanner(helper, BANNER, "Base");
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			try {
				helper.assertTrue(((WaypointTransmitter) banner).makeWaypointConnectionWith(player).isPresent(), "No connection was made");
			} finally {
				helper.getLevel().getServer().getPlayerList().remove(player);
			}
		});
		helper.succeed();
	}

	private static BannerBlockEntity placeBanner(GameTestHelper helper, BlockPos pos, @Nullable String name) {
		helper.setBlock(pos.below(), Blocks.STONE);
		helper.setBlock(pos, WHITE_BANNER_BLOCK);
		BannerBlockEntity banner = helper.getBlockEntity(pos, BannerBlockEntity.class);
		if (name != null) {
			ItemStack stack = new ItemStack(WHITE_BANNER_ITEM);
			stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
			banner.applyComponentsFromItemStack(stack);
		}
		return banner;
	}

	private static BlockPos mapSafePos(GameTestHelper helper) {
		for (int x = 1; x < 7; x++) {
			for (int z = 1; z < 7; z++) {
				BlockPos candidate = new BlockPos(x, 1, z);
				BlockPos absolute = helper.absolutePos(candidate);
				if (isInsideMapBounds(absolute.getX()) && isInsideMapBounds(absolute.getZ())) return candidate;
			}
		}
		throw helper.assertionException("No position inside map bounds");
	}

	private static boolean isInsideMapBounds(int coordinate) {
		int mod = Math.floorMod(coordinate, 128);
		return mod != 63 && mod != 64;
	}

	private static LevelSavedBanners data(GameTestHelper helper) {
		LevelSavedBanners data = ((ServerLevelExtension) helper.getLevel()).bannerpoint$getSavedBanners();
		helper.assertTrue(data != null, "Level has no saved banner data");
		return data;
	}

	private static boolean isTracked(GameTestHelper helper, BannerBlockEntity banner) {
		return helper.getLevel().getWaypointManager().transmitters().contains((WaypointTransmitter) banner);
	}

	private static void assertTracked(GameTestHelper helper, BannerBlockEntity banner) {
		helper.assertTrue(isTracked(helper, banner), "Banner at " + banner.getBlockPos().toShortString() + " is not tracked");
	}

	private static void assertNotTracked(GameTestHelper helper, @Nullable BannerBlockEntity banner) {
		if (banner == null) return;
		helper.assertFalse(isTracked(helper, banner), "Banner at " + banner.getBlockPos().toShortString() + " is tracked");
	}

	private static long countTrackedAt(GameTestHelper helper, BlockPos relative) {
		BlockPos pos = helper.absolutePos(relative);
		return helper.getLevel().getWaypointManager().transmitters().stream()
				.filter(transmitter -> transmitter instanceof BannerBlockEntity banner && banner.getBlockPos().equals(pos))
				.count();
	}

	private static void assertNothingTrackedAt(GameTestHelper helper, BlockPos relative) {
		helper.assertValueEqual(countTrackedAt(helper, relative), 0L, "tracked banners at position");
	}

	private static void withConfig(boolean transmitWhenNamed, boolean transmitWhenTiedToMap, Runnable action) {
		boolean named = Bannerpoint.CONFIG.transmitWhenNamed.get();
		boolean tied = Bannerpoint.CONFIG.transmitWhenTiedToMap.get();
		Bannerpoint.CONFIG.transmitWhenNamed.accept(transmitWhenNamed);
		Bannerpoint.CONFIG.transmitWhenTiedToMap.accept(transmitWhenTiedToMap);
		try {
			action.run();
		} finally {
			Bannerpoint.CONFIG.transmitWhenNamed.accept(named);
			Bannerpoint.CONFIG.transmitWhenTiedToMap.accept(tied);
		}
	}
}
