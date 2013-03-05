package de.varylab.varylab.plugin.ui;

import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.VarylabShrinkPlugin;

public class OptimizationProtocolPanel extends VarylabShrinkPlugin {

	
	
	
	public OptimizationProtocolPanel() {
		setInitialPosition(SHRINKER_TOP);
		shrinkPanel.setTitle("Optimization Protocol");
	}

	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.vendorName = "Stefan Sechelmann";
		info.email = "sechel@math.tu-berlin.de";
		info.name = "Optimization Protocol Panel";
		return info;
	}
	
}
