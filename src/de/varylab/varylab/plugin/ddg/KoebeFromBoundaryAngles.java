package de.varylab.varylab.plugin.ddg;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class KoebeFromBoundaryAngles extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.DDG;
	}

	@Override
	public String getAlgorithmName() {
		return "Koebe From Boundary";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		for (F f : hds.getFaces()) {
			if (HalfEdgeUtils.isInteriorFace(f)) continue;
			
		}
	}

}
