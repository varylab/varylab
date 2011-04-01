package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.ConicalFunctional;
import de.varylab.varylab.plugin.OptimizerPlugin;

public class ConicalOptimizer extends OptimizerPlugin {

	private ConicalFunctional<VVertex, VEdge, VFace>
		functional = new ConicalFunctional<VVertex, VEdge, VFace>();
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}

	@Override
	public String getName() {
		return "Conical Energy";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Conical Optimizer", "Thilo Roerig");
//		info.icon = ImageHook.getIcon("angle.png");
		return info;
	}


}
