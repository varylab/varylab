package de.varylab.varylab.utilities;

import java.util.HashSet;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public class SelectionUtility {

	
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<E> selectGeodesic(E e, HDS hds) {
		Set<E> geodesic = new HashSet<E>();
		E next = e;
		geodesic.add(next);
		geodesic.add(next.getOppositeEdge());
		while(!HalfEdgeUtils.isBoundaryVertex(next.getTargetVertex())) {
			next = getOpposingEdge(next);
			if(next == null) break;
			if(!geodesic.add(next)) break;
			next = next.getOppositeEdge();
			if(!geodesic.add(next)) break;
		}
		next = e.getOppositeEdge();
		while(!HalfEdgeUtils.isBoundaryVertex(next.getTargetVertex())) {
			next = getOpposingEdge(next);
			if(next == null) break;
			if(!geodesic.add(next)) break;
			next = next.getOppositeEdge();
			if(!geodesic.add(next)) break;
		}
		return geodesic;
	}
	
	// get the opposite edge at a vertex, returns null if degree of vertex is odd
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> E getOpposingEdge(E next) {
		V v = next.getTargetVertex();
		E opposite = next;
		int degree = HalfEdgeUtilsExtra.getDegree(v);
		
		if(degree%2 != 0) return null;
		
		for(int i = 0;i < degree/2; ++i) {
			opposite = opposite.getNextEdge().getOppositeEdge();
		}
		
		return opposite;
	}

}
