package de.varylab.varylab.plugin.meshoptimizer;


import de.jtem.halfedgetools.functional.Functional;
import de.varylab.varylab.functional.EdgeLengthEqualizerFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

/**
 * An optimizer that optimizes a given quad-triangle mesh
 * towards incircles. Together with planar-quads
 * these circles can be realized. 
 * @author sechel
 *
 */
public class EdgeLengthEqualizerOptimizer extends VarylabOptimizerPlugin {

	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return new EdgeLengthEqualizerFunctional<VVertex, VEdge, VFace>();
	}

	@Override
	public String getName() {
		return "Edge Length Equalizer";
	}

}
