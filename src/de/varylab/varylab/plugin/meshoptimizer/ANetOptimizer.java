package de.varylab.varylab.plugin.meshoptimizer;

import de.jreality.math.Pn;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.PlanarStarFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class ANetOptimizer extends VarylabOptimizerPlugin {

	private PlanarStarFunctional<VVertex, VEdge, VFace>
		functional = new PlanarStarFunctional<VVertex, VEdge, VFace>(1.0);
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}
	
	public static double getShortestEdgeLength(VHDS hds) {
		double r = Double.MAX_VALUE;
		for (VEdge e : hds.getEdges()) {
			double[] sp = e.getStartVertex().getP();
			double[] tp = e.getTargetVertex().getP();
			double tmp = Pn.distanceBetween(sp, tp, Pn.EUCLIDEAN);
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
		PluginInfo info = new PluginInfo(getName(), "Thilo Rörig");
		info.icon = ImageHook.getIcon("combinatorics.png");
		return info;
	}

}
