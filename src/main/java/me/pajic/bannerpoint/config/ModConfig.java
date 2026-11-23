package me.pajic.bannerpoint.config;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import me.pajic.bannerpoint.Bannerpoint;

@Version(version = 1)
public class ModConfig extends Config {

	public ModConfig() {
		super(Bannerpoint.id("config"));
	}

	@RequiresAction(action = Action.RESTART)
	public ValidatedBoolean transmitWhenNamed = new ValidatedBoolean();
	@RequiresAction(action = Action.RESTART)
	public ValidatedBoolean transmitWhenTiedToMap = new ValidatedBoolean();
	public ValidatedBoolean showDistance = new ValidatedBoolean();
	@RequiresAction(action = Action.RESTART)
	public ValidatedDouble bannerTransmitRange = new ValidatedDouble(60000000, 60000000, 1, ValidatedNumber.WidgetType.TEXTBOX);
}
