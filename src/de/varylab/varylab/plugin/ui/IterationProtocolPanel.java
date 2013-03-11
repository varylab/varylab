package de.varylab.varylab.plugin.ui;

import java.util.LinkedList;
import java.util.List;

import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.optimization.IterationProtocol;
import de.varylab.varylab.plugin.VarylabShrinkPlugin;

public class IterationProtocolPanel extends VarylabShrinkPlugin {

	private List<IterationProtocol>
		protocolList = new LinkedList<IterationProtocol>();
	
	
	
	public IterationProtocolPanel() {
		setInitialPosition(SHRINKER_TOP);
		shrinkPanel.setTitle("Optimization Protocol");
	}
	
	public void appendIterationProtocol(IterationProtocol p) {
		protocolList.add(p);
	}
	public void resetProtokoll() {
		protocolList.clear();
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
