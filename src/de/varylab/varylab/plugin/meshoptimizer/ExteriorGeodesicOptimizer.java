package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.ExteriorGeodesicFunctional;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class ExteriorGeodesicOptimizer extends OptimizerPlugin {

	private ExteriorGeodesicFunctional<VVertex, VEdge, VFace>
		functional = new ExteriorGeodesicFunctional<VVertex, VEdge, VFace>();
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}

	@Override
	public String getName() {
		return "Exterior Geodesic Angle";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Exterior Geodesic Angle Energy Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("angle.png");
		return info;
	}


}
