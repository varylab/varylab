package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.discreteconformal.functional.ElectrostaticSphereFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class ElectrostaticSphereOptimizer extends VarylabOptimizerPlugin {

	private ElectrostaticSphereFunctional<VVertex, VEdge, VFace>
		functional = new ElectrostaticSphereFunctional<VVertex, VEdge, VFace>();
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}

	@Override
	public String getName() {
		return "Electrostatic Sphere Energy";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Electrostatic Sphere Optimizer", "Stefan Sechelmann");
		info.icon = ImageHook.getIcon("electro.png");
		return info;
	}


}
