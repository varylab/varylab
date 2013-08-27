package de.varylab.varylab.plugin.nurbs.algorithm;

import java.util.Collection;
import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;

public class GridRectangleUtility {

	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> boolean isGridRectangle(HDS hds) {
		for(F f : hds.getFaces()) {
			if(HalfEdgeUtils.boundaryEdges(f).size() != 4) {
				return false;
			}
		}
		int boundaryVertsDegTwo = 0;
		for(V v : hds.getVertices()) {
			int vertexDegree = HalfEdgeUtils.incomingEdges(v).size();
			if(HalfEdgeUtils.isBoundaryVertex(v)) {
				if(vertexDegree == 2) {
					++boundaryVertsDegTwo;
				} else if(vertexDegree != 3) {
					return false;
				}
			} else {
				if(vertexDegree != 4) {
					return false;
				}
			}
		}
		if(boundaryVertsDegTwo != 4) {
			return false;
		}
		return true;
	}
	
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int[] getGridRectangleSize(HDS hds) {
		int[] size = new int[2];
		Collection<F> bfs = HalfEdgeUtils.boundaryFaces(hds);
		if(bfs.size() != 1) {
			return null;
		}
		F bf = bfs.iterator().next();
		E be = bf.getBoundaryEdge();
		be = goToNextCorner(be);
		size[0] = nStepsToNextCorner(be);
		be = goToNextCorner(be);
		size[1] = nStepsToNextCorner(be);
		return size;
	}
	
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> V getCorner(HDS hds) {
		return getCornerEdge(hds).getTargetVertex();
	}
	
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> E getCornerEdge(HDS hds) {
		List<List<E>> bfs = HalfEdgeUtils.boundaryComponents(hds);
		if(bfs.size() != 1) {
			return null;
		}
		E be = bfs.iterator().next().iterator().next();
		be = goToNextCorner(be);
		return be;
	}

	public static <
		V extends Vertex<V, E, ?>, 
		E extends Edge<V, E, ?>
	> int nStepsToNextCorner(E e) {
		int nSteps = 0;
		V v = e.getTargetVertex();
		while(HalfEdgeUtils.incomingEdges(v).size() != 2) {
			e = e.getNextEdge();
			v = e.getTargetVertex();
			++nSteps;
		}
		return nSteps;
	}

	public static <
		V extends Vertex<V, E, ?>, 
		E extends Edge<V, E, ?>
	> E goToNextCorner(E e) {
		V v = e.getTargetVertex();
		while(HalfEdgeUtils.incomingEdges(v).size() != 2) {
			e = e.getNextEdge();
			v = e.getTargetVertex();
		}
		return e;
	}

}
