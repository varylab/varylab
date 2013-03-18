package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.OppositeEdgesCurvatureFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class OppositeEdgesCurvatureOptimizer extends VarylabOptimizerPlugin {

	private HalfedgeInterface	
		hif = null;
	private OppositeEdgesCurvatureFunctional<VVertex, VEdge, VFace>
		functional = new OppositeEdgesCurvatureFunctional<VVertex, VEdge, VFace>();
	
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
		return "Opposite Edges Curvature";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Opposite Edges Curvature Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("angle.png");
		return info;
	}


}
