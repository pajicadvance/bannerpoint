package me.pajic.bannerpoint.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.bannerpoint.Bannerpoint;
import me.pajic.bannerpoint.client.BannerNameRenderer;
import me.pajic.bannerpoint.client.ModKeybinds;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class BannerpointClientGameTest implements FabricClientGameTest {

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			//? if >=26.2 {
			singleplayer.getConnection().waitForChunksRender();
			//?} else {
			/*singleplayer.getClientLevel().waitForChunksRender();
			*///?}
			TestServerContext server = singleplayer.getServer();
			BlockPos origin = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().blockPosition());
			server.runCommand("gamemode survival @a");
			server.runCommand("time set noon");
			server.runCommand("tp @a " + xyz(origin) + " 0 0");
			context.waitTicks(5);

			BlockPos near = origin.offset(0, 0, 40);
			server.runCommand("setblock " + xyz(near.below()) + " stone");
			server.runCommand("setblock " + xyz(near) + " white_banner{CustomName:\"Near Base\"}");
			context.waitTicks(20);
			assertClientHas(context, near, true);
			assertName(context, near, "Near Base");
			assertLabelVisible(context, near, false);
			context.getInput().holdKey(options -> options.keyShift);
			context.waitTicks(10);
			assertLabelVisible(context, near, true);
			screenshot(context, "bannerpoint_looked_at");
			context.getInput().releaseKey(options -> options.keyShift);
			context.waitTicks(10);
			assertLabelVisible(context, near, false);

			context.getInput().holdKey(options -> options.keyPlayerList);
			context.waitTicks(10);
			int centerBefore = context.computeOnClient(c -> BannerNameRenderer.labelCenterX(uuid(near)));
			context.getInput().releaseKey(options -> options.keyPlayerList);
			context.runOnClient(c -> c.player.setYRot(c.player.getYRot() + 10.0F));
			context.waitTick();
			float fadingAlpha = context.computeOnClient(c -> BannerNameRenderer.labelAlpha(uuid(near)));
			int centerAfter = context.computeOnClient(c -> BannerNameRenderer.labelCenterX(uuid(near)));
			check(fadingAlpha > 0.0F && fadingAlpha < 1.0F, "Label was not fading out, alpha " + fadingAlpha);
			check(Math.abs(centerAfter - centerBefore) >= 8, "Fading label did not follow its icon: " + centerBefore + " -> " + centerAfter);
			context.runOnClient(c -> c.player.setYRot(c.player.getYRot() - 10.0F));
			context.waitTicks(10);
			assertLabelVisible(context, near, false);

			server.runCommand("data merge block " + xyz(near) + " {CustomName:\"Renamed Base\"}");
			context.waitTicks(10);
			assertName(context, near, "Renamed Base");

			server.runCommand("tp @a " + xyz(origin) + " 20 0");
			context.waitTicks(10);
			context.getInput().holdKey(options -> options.keyShift);
			context.waitTicks(10);
			assertLabelVisible(context, near, false);
			context.getInput().releaseKey(options -> options.keyShift);
			context.getInput().holdKey(options -> options.keyPlayerList);
			context.waitTicks(10);
			assertLabelVisible(context, near, true);
			context.getInput().releaseKey(options -> options.keyPlayerList);
			context.waitTicks(10);
			assertLabelVisible(context, near, false);
			context.runOnClient(client -> {
				//? if >=26.3 {
				ModKeybinds.SHOW_NAMES.setKey(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_LALT));
				//?} else {
				/*ModKeybinds.SHOW_NAMES.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_LALT));
				*///?}
				KeyMapping.resetMapping();
			});
			context.getInput().holdKey(ModKeybinds.SHOW_NAMES);
			context.waitTicks(10);
			assertLabelVisible(context, near, true);
			context.getInput().releaseKey(ModKeybinds.SHOW_NAMES);
			context.runOnClient(client -> {
				ModKeybinds.SHOW_NAMES.setKey(InputConstants.UNKNOWN);
				KeyMapping.resetMapping();
			});
			context.waitTicks(10);
			assertLabelVisible(context, near, false);
			server.runCommand("tp @a " + xyz(origin) + " 0 0");

			BlockPos left = near.offset(2, 0, 0);
			BlockPos right = near.offset(-2, 0, 0);
			server.runCommand("setblock " + xyz(left.below()) + " stone");
			server.runCommand("setblock " + xyz(left) + " black_banner{CustomName:\"A very long banner name that should get truncated\"}");
			server.runCommand("setblock " + xyz(right.below()) + " stone");
			server.runCommand("setblock " + xyz(right) + " red_banner{CustomName:{text:\"Custom Colored\",color:\"aqua\"}}");
			context.waitTicks(20);
			context.getInput().holdKey(options -> options.keyPlayerList);
			context.waitTicks(10);
			assertLabelVisible(context, near, true);
			assertLabelVisible(context, left, true);
			assertLabelVisible(context, right, true);
			screenshot(context, "bannerpoint_stacked");
			context.getInput().releaseKey(options -> options.keyPlayerList);

			server.runCommand("setblock " + xyz(near.below()) + " air");
			context.waitTicks(10);
			assertServerCount(server, near, 0);
			assertClientHas(context, near, false);
			assertName(context, near, "");
			server.runCommand("setblock " + xyz(left) + " air");
			server.runCommand("setblock " + xyz(right) + " air");
			context.waitTicks(10);
			screenshot(context, "bannerpoint_near_removed");

			BlockPos far = origin.offset(0, 0, 1200);
			server.runCommand("forceload add " + far.getX() + " " + far.getZ());
			waitForChunk(context, server, far, true);
			server.runCommand("setblock " + xyz(far.below()) + " stone");
			server.runCommand("setblock " + xyz(far) + " white_banner{CustomName:\"Far Outpost\"}");
			context.waitTicks(20);
			assertServerCount(server, far, 1);
			assertClientHas(context, far, true);
			int farDistance = context.computeOnClient(c -> BannerNameRenderer.getFarDistance(uuid(far)));
			check(Math.abs(farDistance - 1200) <= 20, "Client far distance was " + farDistance);

			server.runCommand("forceload remove " + far.getX() + " " + far.getZ());
			waitForChunk(context, server, far, false);
			context.waitTicks(20);
			assertClientHas(context, far, true);
			context.getInput().holdKey(options -> options.keyPlayerList);
			context.waitTicks(5);
			screenshot(context, "bannerpoint_far_unloaded");
			context.getInput().releaseKey(options -> options.keyPlayerList);

			server.runCommand("forceload add " + far.getX() + " " + far.getZ());
			waitForChunk(context, server, far, true);
			context.waitTicks(5);
			assertServerCount(server, far, 1);
			assertClientHas(context, far, true);

			server.runCommand("setblock " + xyz(far) + " air");
			context.waitTicks(10);
			assertServerCount(server, far, 0);
			assertClientHas(context, far, false);
			server.runCommand("forceload remove " + far.getX() + " " + far.getZ());
			screenshot(context, "bannerpoint_far_removed");
		}
	}

	private static void screenshot(ClientGameTestContext context, String name) {
		//? if >=26.2 {
		context.takeScreenshot(name);
		//?}
	}

	private static UUID uuid(BlockPos pos) {
		return UUID.nameUUIDFromBytes((Bannerpoint.MOD_ID + ":minecraft:overworld:" + pos.asLong()).getBytes(StandardCharsets.UTF_8));
	}

	private static String xyz(BlockPos pos) {
		return pos.getX() + " " + pos.getY() + " " + pos.getZ();
	}

	private static void check(boolean condition, String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static void assertName(ClientGameTestContext context, BlockPos pos, String expected) {
		String name = context.computeOnClient(c -> BannerNameRenderer.getName(uuid(pos)).getString());
		check(name.equals(expected), "Client name for banner at " + xyz(pos) + " was '" + name + "', expected '" + expected + "'");
	}

	private static void assertLabelVisible(ClientGameTestContext context, BlockPos pos, boolean expected) {
		boolean visible = context.computeOnClient(c -> BannerNameRenderer.visibleLabels().contains(uuid(pos)));
		check(visible == expected, "Label for banner at " + xyz(pos) + " visible=" + visible + ", expected " + expected);
	}

	private static void assertClientHas(ClientGameTestContext context, BlockPos pos, boolean expected) {
		Set<UUID> ids = context.computeOnClient(client -> {
			Set<UUID> result = new HashSet<>();
			client.player.connection.getWaypointManager().forEachWaypoint(client.player, waypoint -> waypoint.id().left().ifPresent(result::add));
			return result;
		});
		check(ids.contains(uuid(pos)) == expected, "Client waypoint for banner at " + xyz(pos) + " present=" + !expected + ", expected " + expected);
	}

	private static void assertServerCount(TestServerContext server, BlockPos pos, long expected) {
		long count = server.computeOnServer(s -> s.overworld().getWaypointManager().transmitters().stream()
				.filter(transmitter -> transmitter instanceof BannerBlockEntity banner && banner.getBlockPos().equals(pos))
				.count());
		check(count == expected, "Server tracks " + count + " banners at " + xyz(pos) + ", expected " + expected);
	}

	private static void waitForChunk(ClientGameTestContext context, TestServerContext server, BlockPos pos, boolean loaded) {
		for (int i = 0; i < 400; i++) {
			boolean isLoaded = server.computeOnServer(s -> s.overworld().getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4));
			if (isLoaded == loaded) return;
			context.waitTick();
		}
		throw new AssertionError("Chunk at " + xyz(pos) + " never became loaded=" + loaded);
	}
}
