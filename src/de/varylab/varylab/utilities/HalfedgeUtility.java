package de.varylab.varylab.utilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;

public class HalfedgeUtility {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void  retainEdges(HDS hds, Set<E> edges) {
		for(E e : new ArrayList<E>(hds.getEdges())) {
			if(!e.isPositive()) {
				continue;
			}
			if(!edges.contains(e)) {
				TopologyAlgorithms.removeEdge(e);
			}
		}
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void  retainFaces(HDS hds, HashSet<F> hyperbolicFaces) {
		for(F f : new ArrayList<F>(hds.getFaces())) {
			if(!hyperbolicFaces.contains(f)) {
				TopologyAlgorithms.removeFace(f);
			}
	}
}
}
