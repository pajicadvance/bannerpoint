package me.pajic.bannerpoint.config;

import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.pajic.bannerpoint.Bannerpoint;
import org.jetbrains.annotations.NotNull;

@Version(version = 2)
public class ModClientConfig extends Config {

	public ModClientConfig() {
		super(Bannerpoint.id("client_config"));
	}

	public ValidatedEnum<NameDisplay> nameDisplay = new ValidatedEnum<>(NameDisplay.HOLD_KEY);
	public ValidatedBoolean useTabAndShift = new ValidatedBoolean();
	public ValidatedBoolean showLookedAtName = new ValidatedBoolean(false);
	public ValidatedInt lookAtThreshold = new ValidatedInt(6, 40, 0);
	public ValidatedInt maxNameWidth = new ValidatedInt(100, 300, 20);
	public ValidatedBoolean coloredText = new ValidatedBoolean();
	public ValidatedBoolean textOutline = new ValidatedBoolean();
	public ValidatedBoolean textShadow = new ValidatedBoolean();
	public ValidatedBoolean textBackground = new ValidatedBoolean();
	public ValidatedInt textBackgroundOpacity = new ValidatedInt(50, 100, 0);

	public enum NameDisplay implements EnumTranslatable {
		ALWAYS,
		HOLD_KEY,
		TOGGLE_KEY,
		NEVER;

		@Override @NotNull
		public String prefix() {
			return "bannerpoint.client_config.nameDisplay";
		}
	}
}
