package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.geodesic.GeodesicAngleFunctional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.ui.image.ImageHook;

public class GeodesicAngleOptimizer extends OptimizerPlugin {

	@Override
	public Functional<VVertex, VEdge, VFace> createFunctional(VHDS hds) {
		return new GeodesicAngleFunctional<VVertex, VEdge, VFace>();
	}

	@Override
	public String getName() {
		return "Geodesic Angle Energy";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Geodesic Angle Energy Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("angle.png");
		return info;
	}


}
