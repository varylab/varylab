package de.varylab.varylab.plugin.meshoptimizer;


import de.jtem.halfedgetools.functional.Functional;
import de.varylab.varylab.functional.InflateFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class InflateOptimizer extends VarylabOptimizerPlugin {

	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return new InflateFunctional();
	}

	@Override
	public String getName() {
		return "Inflate to CMC?";
	}

}
