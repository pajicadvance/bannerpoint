package me.pajic.bannerpoint.config;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import me.pajic.bannerpoint.Bannerpoint;
import net.minecraft.world.waypoints.Waypoint;

@Version(version = 1)
@RequiresAction(action = Action.RELOG)
public class ModConfig extends Config {

	public ModConfig() {
		super(Bannerpoint.id("config"));
	}

	public ValidatedBoolean transmitWhenNamed = new ValidatedBoolean();
	public ValidatedBoolean transmitWhenTiedToMap = new ValidatedBoolean();
	public ValidatedDouble bannerTransmitRange = new ValidatedDouble(Waypoint.MAX_RANGE, Waypoint.MAX_RANGE, 1, ValidatedNumber.WidgetType.TEXTBOX);
}
