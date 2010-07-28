package de.varylab.varylab.utilities;

import java.util.HashSet;
import java.util.LinkedList;
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

	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> 
	> E getOppositeEdgeInFace(E e) 
	{
		F f = e.getLeftFace();
		E oe = e;
		if((HalfEdgeUtils.boundaryEdges(f).size() % 2) == 0) {
			for(int i = 0; i < HalfEdgeUtils.boundaryEdges(f).size()/2; ++i) {
				oe = oe.getNextEdge();
			}
		} else {
			if(!HalfEdgeUtils.isInteriorFace(f)) {
				for(E be : HalfEdgeUtils.boundaryEdges(f)) {
					if(be.getRightFace() == null) {
						return be;
					}
				}
			} else {
				return null;
			}
		}
		return oe;
	}
	
	public static  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> void generateStrip1D(F f, E fe,
			LinkedList<F> stripFaces,
			LinkedList<E> stripEdges)
	{
		stripFaces.addFirst(f);
		E e = fe;
		stripEdges.addLast(e);
		stripEdges.add(e.getOppositeEdge());
		
		F rf = e.getRightFace();
		while(e.getRightFace() != null) {
			if(HalfEdgeUtils.isInteriorFace(rf) && (HalfEdgeUtils.boundaryEdges(rf).size() % 2) != 0) {
				break;
			}
			stripFaces.addLast(rf);
			e = getOppositeEdgeInFace(e.getOppositeEdge());
			if(e == null) {
				break;
			}
			rf = e.getRightFace();
			stripEdges.addLast(e);
			stripEdges.add(e.getOppositeEdge());
		}
		e = getOppositeEdgeInFace(fe);
		if(e == null) {
			return;
		}
		stripEdges.addFirst(e);
		stripEdges.addFirst(e.getOppositeEdge());
		rf = e.getRightFace();
		while(rf != null) {
			if(HalfEdgeUtils.isInteriorFace(rf) && (HalfEdgeUtils.boundaryEdges(rf).size() % 2) != 0) {
				break;
			}
			stripFaces.addFirst(rf);
			e = getOppositeEdgeInFace(e.getOppositeEdge());
			if(e == null) {
				break;
			}
			rf = e.getRightFace();
			stripEdges.addFirst(e);
			stripEdges.addFirst(e.getOppositeEdge());
		}
	}
}
