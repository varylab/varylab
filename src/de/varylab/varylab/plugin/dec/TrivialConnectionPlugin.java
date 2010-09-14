package de.varylab.varylab.plugin.dec;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.varylab.varylab.math.dec.DiscreteDifferentialOperators;
import de.varylab.varylab.math.dec.TrivialConnection;

public class TrivialConnectionPlugin extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.VectorField;
	}

	@Override
	public String getAlgorithmName() {
		return "Trivial connection";
	}

	@Override
	public <V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>, HDS extends HalfEdgeDataStructure<V, E, F>> void execute(
			HDS hds, CalculatorSet c, HalfedgeInterface hcp)
			throws CalculatorException {
		DiscreteDifferentialOperators dop = new DiscreteDifferentialOperators(hds, hcp.getAdapters());
		hcp.addAdapter(dop.getTrivialConnectionVectorField());
	}

}
