package de.varylab.varylab.plugin.topology;

import java.util.Iterator;
import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class StitchingPlugin extends AlgorithmPlugin{

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Topology;
	}

	@Override
	public String getAlgorithmName() {
		return "Stitch";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		HalfedgeSelection selection = hi.getSelection();
		List<V> vertices = selection.getVertices(hds);
		if(vertices.size() != 2) {
			return;
		}
		Iterator<V> si = vertices.iterator();
		V v1 = si.next(), v2 = si.next();
		StitchingUtility.stitch(hds, v1, v2, a);
		hi.set(hds);
	}
}
