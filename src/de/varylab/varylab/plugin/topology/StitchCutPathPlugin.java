package de.varylab.varylab.plugin.topology;

import static de.jtem.halfedge.util.HalfEdgeUtils.isBoundaryEdge;
import static de.jtem.halfedge.util.HalfEdgeUtils.isBoundaryVertex;
import static de.varylab.varylab.plugin.topology.StitchingUtility.stitch;

import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class StitchCutPathPlugin extends AlgorithmPlugin{

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Topology;
	}

	@Override
	public String getAlgorithmName() {
		return "Stitch Cut Path";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		HalfedgeSelection selection = hi.getSelection();
		Set<V> vSel = selection.getVertices(hds);
		Set<E> eSel = selection.getEdges(hds);
		if(vSel.size() != 1) throw new RuntimeException("Please select one boundary vertex and edge");
		
		V startV = vSel.iterator().next();
		E endE = eSel.iterator().next();

		if (!isBoundaryVertex(startV)) throw new RuntimeException("No boundary Vertex as Start");
		if (!isBoundaryEdge(endE)) throw new RuntimeException("No Boundary Edge as End");
		
		E e1 = startV.getIncomingEdge();
		while (e1.getLeftFace() != null) {
			e1 = e1.getNextEdge().getOppositeEdge();
		}
		E e2 = e1.getNextEdge();
		while (true) {
			V v1 = e1.getStartVertex();
			V v2 = e2.getTargetVertex();
			E nextE1 = e1.getPreviousEdge();
			E nextE2 = e2.getNextEdge();
			stitch(hds, a.querySet(double[].class), v1, v2);
			if (eSel.contains(e1) || eSel.contains(e2)) {
				break;
			}
			e1 = nextE1;
			e2 = nextE2;
		}
		hi.set(hds);
	}
}
