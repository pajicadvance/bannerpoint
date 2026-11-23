package me.pajic.bannerpoint.client;

import me.pajic.bannerpoint.Bannerpoint;
import me.pajic.bannerpoint.BannerpointClient;
import me.pajic.bannerpoint.config.ModClientConfig;
import me.pajic.bannerpoint.networking.S2CBannerDistancePayload;
import me.pajic.bannerpoint.networking.S2CBannerNamePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BannerNameRenderer {

	private static final int ROW_HEIGHT = 12;
	private static final int MAX_ROWS = 3;
	private static final int LABEL_GAP = 4;
	private static final int SCREEN_MARGIN = 2;
	private static final float FADE_MILLIS = 150.0F;
	private static final float MIN_EDGE_ALPHA = 0.35F;
	private static final float MAX_DOT_POSITION = 173.0F / 2.0F;
	private static final int DISTANCE_COLOR = 0xAAAAAA;
	private static final int LIGHT_OUTLINE_COLOR = 0xE0E0E0;
	private static final int DARK_OUTLINE_COLOR = 0x000000;
	private static final int[][] OUTLINE_OFFSETS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

	private static final Map<UUID, BannerInfo> infos = new HashMap<>();
	private static final List<Candidate> candidates = new ArrayList<>();
	private static final Map<UUID, Label> labels = new LinkedHashMap<>();
	private static boolean toggled;
	private static long lastFrameMillis = -1;

	private record BannerInfo(@Nullable Component name, int bannerColor, int farDistance) {}

	private record Candidate(UUID uuid, int centerX, int dotPosition, int top, double distance) {}

	private static final class Label {
		private final UUID uuid;
		private int centerX;
		private int dotPosition;
		private int top;
		private double distance;
		private boolean lookedAt;
		private boolean onBar;
		private float alpha;
		private float targetAlpha;

		private Label(UUID uuid) {
			this.uuid = uuid;
		}
	}

	public static void setName(S2CBannerNamePayload payload) {
        infos.compute(payload.uuid(), (k, old) -> new BannerInfo(payload.name(), payload.bannerColor(), old == null ? -1 : old.farDistance()));
	}

	public static void setFarDistance(S2CBannerDistancePayload payload) {
        infos.compute(payload.uuid(), (k, old) -> old == null ?
                new BannerInfo(null, 0xFFFFFF, payload.distance()) :
                new BannerInfo(old.name(), old.bannerColor(), payload.distance()));
	}

	public static Component getName(UUID uuid) {
		BannerInfo info = infos.get(uuid);
		return info == null || info.name() == null ? Component.empty() : info.name();
	}

	public static int getFarDistance(UUID uuid) {
		BannerInfo info = infos.get(uuid);
		return info == null ? -1 : info.farDistance();
	}

	public static float labelAlpha(UUID uuid) {
		Label label = labels.get(uuid);
		return label == null ? 0.0F : label.alpha;
	}

	public static int labelCenterX(UUID uuid) {
		Label label = labels.get(uuid);
		return label == null ? -1 : label.centerX;
	}

	public static Set<UUID> visibleLabels() {
		Set<UUID> visible = new HashSet<>();
		labels.values().forEach(label -> {
			if (label.alpha > 0.0F) visible.add(label.uuid);
		});
		return visible;
	}

	public static void remove(UUID uuid) {
		infos.remove(uuid);
		labels.remove(uuid);
	}

	public static void clear() {
		infos.clear();
		labels.clear();
		candidates.clear();
		lastFrameMillis = -1;
	}

	public static void tick() {
		while (ModKeybinds.SHOW_NAMES.consumeClick()) toggled = !toggled;
	}

	public static void collect(TrackedWaypoint waypoint, Entity cameraEntity, int screenMiddle, int dotPosition, int top) {
		waypoint.id().left().ifPresent(uuid -> {
			BannerInfo info = infos.get(uuid);
			if (info == null || info.name() == null) return;
			double distanceSquared = waypoint.distanceSquared(cameraEntity);
			double distance = Double.isFinite(distanceSquared) ? Math.sqrt(distanceSquared) : info.farDistance();
			candidates.add(new Candidate(uuid, screenMiddle + dotPosition + 4, dotPosition, top, distance));
		});
	}

	public static void render(Minecraft minecraft, GuiGraphicsExtractor graphics) {
		ModClientConfig config = BannerpointClient.CONFIG;
		long now = Util.getMillis();
		float step = lastFrameMillis < 0 ? 1.0F : Math.min(1.0F, (now - lastFrameMillis) / FADE_MILLIS);
		lastFrameMillis = now;

		ModClientConfig.NameDisplay display = config.nameDisplay.get();
		boolean vanillaKeys = config.useTabAndShift.get();
		boolean showAll = vanillaKeys && minecraft.options.keyPlayerList.isDown() || switch (display) {
			case ALWAYS -> true;
			case HOLD_KEY -> ModKeybinds.SHOW_NAMES.isDown();
			case TOGGLE_KEY -> toggled;
			case NEVER -> false;
		};

		Candidate lookedAt = null;
		if (config.showLookedAtName.get() || vanillaKeys && minecraft.options.keyShift.isDown()) {
			int threshold = config.lookAtThreshold.get();
			for (Candidate candidate : candidates) {
				int offset = Math.abs(candidate.dotPosition());
				if (offset <= threshold && (lookedAt == null || offset < Math.abs(lookedAt.dotPosition()))) lookedAt = candidate;
			}
		}

		for (Label label : labels.values()) {
			label.targetAlpha = 0.0F;
			label.lookedAt = false;
			label.onBar = false;
		}
		for (Candidate candidate : candidates) {
			boolean isLookedAt = candidate == lookedAt;
			boolean shown = showAll || isLookedAt;
			Label label = shown ? labels.computeIfAbsent(candidate.uuid(), Label::new) : labels.get(candidate.uuid());
			if (label == null) continue;
			label.centerX = candidate.centerX();
			label.dotPosition = candidate.dotPosition();
			label.top = candidate.top();
			label.distance = candidate.distance();
			label.onBar = true;
			if (!shown) continue;
			label.lookedAt = isLookedAt;
			label.targetAlpha = display == ModClientConfig.NameDisplay.ALWAYS && !isLookedAt ?
					Mth.lerp(Math.min(1.0F, Math.abs(candidate.dotPosition()) / MAX_DOT_POSITION), 1.0F, MIN_EDGE_ALPHA) :
					1.0F;
		}
		candidates.clear();

		Iterator<Label> iterator = labels.values().iterator();
		while (iterator.hasNext()) {
			Label label = iterator.next();
			if (label.alpha < label.targetAlpha) label.alpha = Math.min(label.targetAlpha, label.alpha + step);
			else label.alpha = Math.max(label.targetAlpha, label.alpha - step);
			BannerInfo info = infos.get(label.uuid);
			if (!label.onBar || (label.alpha <= 0.0F && label.targetAlpha <= 0.0F) || info == null || info.name() == null) iterator.remove();
		}
		if (labels.isEmpty()) return;

		List<Label> ordered = new ArrayList<>(labels.values());
		ordered.sort(Comparator.<Label>comparingInt(label -> label.lookedAt ? 0 : 1).thenComparingInt(label -> Math.abs(label.dotPosition)));

		Font font = minecraft.font;
		List<List<int[]>> rows = new ArrayList<>();
		for (Label label : ordered) {
			FormattedCharSequence text = buildText(font, infos.get(label.uuid), label.distance, config);
			int width = font.width(text);
			int x = Mth.clamp(label.centerX - width / 2, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, graphics.guiWidth() - width - SCREEN_MARGIN));
			int row = claimRow(rows, x, x + width);
			if (row < 0) continue;
			int y = label.top - ROW_HEIGHT - row * ROW_HEIGHT;
			draw(graphics, font, text, x, y, width, label.alpha, config);
		}
	}

	private static FormattedCharSequence buildText(Font font, BannerInfo info, double distance, ModClientConfig config) {
		Style baseStyle = Style.EMPTY.withColor(config.coloredText.get() ? info.bannerColor() : 0xFFFFFF);
		Component name = Component.empty().withStyle(baseStyle).append(info.name());
		FormattedText nameText = name;
		int maxWidth = config.maxNameWidth.get();
		if (font.width(name) > maxWidth) {
			Component ellipsis = Component.literal("…").withStyle(baseStyle);
			nameText = FormattedText.composite(font.substrByWidth(name, Math.max(0, maxWidth - font.width(ellipsis))), ellipsis);
		}
		if (Bannerpoint.CONFIG.showDistance.get() && distance >= 0) {
			nameText = FormattedText.composite(nameText, Component.literal(" " + formatDistance(distance)).withColor(DISTANCE_COLOR));
		}
		return Language.getInstance().getVisualOrder(nameText);
	}

	private static String formatDistance(double distance) {
		if (distance < 1000) return Math.round(distance) + "m";
		return String.format(Locale.ROOT, "%.1fkm", distance / 1000);
	}

	private static int claimRow(List<List<int[]>> rows, int x0, int x1) {
		for (int row = 0; row < MAX_ROWS; row++) {
			if (rows.size() <= row) rows.add(new ArrayList<>());
			List<int[]> placed = rows.get(row);
			boolean free = placed.stream().noneMatch(range -> x0 < range[1] + LABEL_GAP && range[0] < x1 + LABEL_GAP);
			if (free) {
				placed.add(new int[]{x0, x1});
				return row;
			}
		}
		return -1;
	}

	private static void draw(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y, int width, float alpha, ModClientConfig config) {
		int alphaChannel = Math.round(alpha * 255);
		if (alphaChannel < 4) return;
		if (config.textBackground.get()) {
			int backgroundAlpha = Math.round(config.textBackgroundOpacity.get() / 100.0F * alpha * 255);
			graphics.fill(x - 2, y - 2, x + width + 2, y + 10, ARGB.color(backgroundAlpha, 0, 0, 0));
		}
		int color = ARGB.color(alphaChannel, 0xFFFFFF);
		if (config.textOutline.get()) {
			FormattedCharSequence outline = sink -> text.accept((index, style, codepoint) -> sink.accept(index, style.withColor(outlineColor(style)), codepoint));
			for (int[] offset : OUTLINE_OFFSETS) graphics.text(font, outline, x + offset[0], y + offset[1], color, false);
			graphics.text(font, text, x, y, color, false);
		} else {
			graphics.text(font, text, x, y, color, config.textShadow.get());
		}
	}

	private static int outlineColor(Style style) {
		int rgb = style.getColor() == null ? 0xFFFFFF : style.getColor().getValue();
		return luminance(rgb) < 0.4F ? LIGHT_OUTLINE_COLOR : DARK_OUTLINE_COLOR;
	}

	private static float luminance(int rgb) {
		return (ARGB.red(rgb) * 0.299F + ARGB.green(rgb) * 0.587F + ARGB.blue(rgb) * 0.114F) / 255.0F;
	}
}
