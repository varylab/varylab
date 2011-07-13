package de.varylab.varylab.plugin.topology;

import static de.jtem.halfedge.util.HalfEdgeUtils.isBoundaryVertex;

import java.util.Iterator;
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
		
		if (vSel.size() != 2 || eSel.size() == 0) {
			throw new RuntimeException("Please select two boundary vertices to identify and a boundary edge on the stitch path.");
		}
		Iterator<V> vIt = vSel.iterator();
		V vs1 = vIt.next();
		V vs2 = vIt.next();
		if (!isBoundaryVertex(vs1) || !isBoundaryVertex(vs2)) {
			throw new RuntimeException("Please select two boundary vertices");
		}
		E be = eSel.iterator().next();
		if (be.getLeftFace() != null) {
			be = be.getOppositeEdge();
		}
		if (be.getLeftFace() != null) {
			throw new RuntimeException("Please select a boundary edge.");
		}
		// find edges at end-vertices
		E walker = be;
		E es1 = null;
		E es2 = null;
		int counter = 0;
		boolean count = false;
		do {
			if ((walker.getTargetVertex() == vs1 || walker.getTargetVertex() == vs2) && es2 == null) {
				es2 = walker;
				if (walker.getTargetVertex() == vs1) { // wrong labelling 
					V tmpV = vs2;
					vs2 = vs1;
					vs1 = tmpV;
				}
			}
			if (walker.getStartVertex() == vs1 && es2 != null) {
				es1 = walker;
				count = true;
				counter = 1;
			}
			if (count) {
				counter++;
			}
			walker = walker.getNextEdge();
		} while (walker != es2); 
		System.out.println("start edges: " + es1 + ", " + es2);
		System.out.println("Stitch Set size: " + counter);
		if (es1 == null || es2 == null) {
			throw new RuntimeException("Cannot find end edges");
		}
		if (counter % 2 != 0) {
			throw new RuntimeException("Odd number of edges on stitch path");
		}
		walker = es1;
		for (int i = 0; i < counter / 2; i++) {
			walker = walker.getNextEdge();
		}
		V v = walker.getStartVertex();
		StitchingUtility.stitch(hds, v, counter / 2, a);
		hi.set(hds);
	}
}
