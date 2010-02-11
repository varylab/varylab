package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.willmore.WillmoreFunctional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.ui.image.ImageHook;

public class WillmoreOptimizer extends OptimizerPlugin {

	@Override
	public Functional<VVertex, VEdge, VFace> createFunctional(VHDS hds) {
		return new WillmoreFunctional<VVertex, VEdge, VFace>();
	}

	@Override
	public String getName() {
		return "Willmore Energy";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Willmore Energy Optimizer", "Stefan Sechelmann und Thilo Roerig");
		info.icon = ImageHook.getIcon("willmore.png");
		return info;
	}


}
