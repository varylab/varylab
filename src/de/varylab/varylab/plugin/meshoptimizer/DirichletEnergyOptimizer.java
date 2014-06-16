package de.varylab.varylab.plugin.meshoptimizer;


import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.dirichlet.DirichletEnergyFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class DirichletEnergyOptimizer extends VarylabOptimizerPlugin {

	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return new DirichletEnergyFunctional<>();
	}

	@Override
	public String getName() {
		return "Cotangent Dirichlet Energy";
	}

}
