package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.ForceConeFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class ForceConeOptimizer extends VarylabOptimizerPlugin { 
	
	private ForceConeFunctional<VVertex, VEdge, VFace>
	functional = new ForceConeFunctional<VVertex, VEdge, VFace>();

	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}
	
	@Override
	public String getName() {
		return "Force cones";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Force Cone Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("forceCone.png",16,16);
		return info;
	}

}
