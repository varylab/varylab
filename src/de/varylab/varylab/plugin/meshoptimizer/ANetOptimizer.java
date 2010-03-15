package de.varylab.varylab.plugin.meshoptimizer;

import de.jreality.math.Pn;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.PlanarStarFunctional;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class ANetOptimizer extends OptimizerPlugin {

	private PlanarStarFunctional<VVertex, VEdge, VFace>
		functional = new PlanarStarFunctional<VVertex, VEdge, VFace>(1.0);
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
//		double scale = getShortestEdgeLength(hds);
//		functional.setScale(scale);
		return functional;
	}
	
	public static double getShortestEdgeLength(VHDS hds) {
		double r = Double.MAX_VALUE;
		for (VEdge e : hds.getEdges()) {
			double tmp = Pn.distanceBetween(e.getStartVertex().position, e.getTargetVertex().position, Pn.EUCLIDEAN);
			if (tmp < r) {
				r = tmp;
			}
		}
		return r;
	}
	

	@Override
	public String getName() {
		return "Planar Vertex Stars";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("A Net Optimizer", "Thilo Rörig");
		info.icon = ImageHook.getIcon("combinatorics.png");
		return info;
	}

}
