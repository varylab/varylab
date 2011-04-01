package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.CircularFunctional;
import de.varylab.varylab.plugin.OptimizerPlugin;

public class CircularQuadOptimizer extends OptimizerPlugin {

	private CircularFunctional<VVertex, VEdge, VFace>
		functional = new CircularFunctional<VVertex, VEdge, VFace>();
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}

	@Override
	public String getName() {
		return "Circular Quad Energy";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Circular Quad Optimizer", "Thilo Roerig");
//		info.icon = ImageHook.getIcon("angle.png");
		return info;
	}


}
