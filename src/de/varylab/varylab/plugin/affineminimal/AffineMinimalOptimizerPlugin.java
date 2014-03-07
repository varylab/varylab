package de.varylab.varylab.plugin.affineminimal;


import de.jtem.halfedgetools.functional.Functional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class AffineMinimalOptimizerPlugin extends VarylabOptimizerPlugin {

	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return new AffineMinimalFunctional();
	}

	@Override
	public String getName() {
		return "Affine Minimal Functional Energy";
	}

}
