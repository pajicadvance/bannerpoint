package me.pajic.bannerpoint.config;

import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.pajic.bannerpoint.Bannerpoint;

@Version(version = 1)
public class ModClientConfig extends Config {

	public ModClientConfig() {
		super(Bannerpoint.id("client_config"));
	}

	public ValidatedBoolean coloredText = new ValidatedBoolean();
	public ValidatedBoolean textShadow = new ValidatedBoolean();
	public ValidatedBoolean textBackground = new ValidatedBoolean();
	public ValidatedInt textBackgroundOpacity = new ValidatedInt(50, 100, 0);
}
