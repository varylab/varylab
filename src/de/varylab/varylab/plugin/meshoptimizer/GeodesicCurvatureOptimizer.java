package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.GeodesicCurvatureFunctional;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;
import de.varylab.varylab.plugin.image.ImageHook;

public class GeodesicCurvatureOptimizer extends VarylabOptimizerPlugin {

	private HalfedgeInterface
		hif = null;
	private GeodesicCurvatureFunctional<VVertex, VEdge, VFace>
		functional = new GeodesicCurvatureFunctional<VVertex, VEdge, VFace>();
	
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		functional.setAdapters(hif.getAdapters());
		return functional;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
	}
	
	
	@Override
	public String getName() {
		return "Geodesic Curvature";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Geodesic Curvature Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("angle.png");
		return info;
	}


}
