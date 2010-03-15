package de.varylab.varylab.plugin.ui;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.aggregators.MenuAggregator;
import de.jtem.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class VarylabMenu extends MenuAggregator {

	public VarylabMenu() {
	}

	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("VaryLab Menu", "Stefan Sechelmann");
	}
	
	@Override
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

}
