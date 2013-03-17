package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.WillmoreFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;
import de.varylab.varylab.plugin.image.ImageHook;

public class WillmoreOptimizer extends VarylabOptimizerPlugin {

	private WillmoreFunctional<VVertex, VEdge, VFace> 
		functional = new WillmoreFunctional<VVertex, VEdge, VFace>();
	
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

	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}


}
