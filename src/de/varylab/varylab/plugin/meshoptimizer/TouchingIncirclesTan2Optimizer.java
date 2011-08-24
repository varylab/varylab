package de.varylab.varylab.plugin.meshoptimizer;


import de.jtem.halfedgetools.functional.Functional;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.TouchingIncirclesFunctionalTan2;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

/**
 * An optimizer that optimizes a given quad-triangle mesh
 * towards touching incircles. Together with planar-quads
 * these circles can be realized. 
 * @author sechel
 *
 */
public class TouchingIncirclesTan2Optimizer extends VarylabOptimizerPlugin {

	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return new TouchingIncirclesFunctionalTan2();
	}

	@Override
	public String getName() {
		return "Touching Tan2 Incircles";
	}

}
