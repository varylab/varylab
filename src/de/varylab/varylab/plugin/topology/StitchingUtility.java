package de.varylab.varylab.plugin.topology;

import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;

public class StitchingUtility {

	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> boolean stitch(HDS hds, TypedAdapterSet<double[]> a, V v1, V v2) {
	
		List<E> inEdges = findEdgesOfCommonHole(v1,v2);
		E 	ie1 = null,
			ie2 = null;
		if(inEdges.size() == 2) {
			ie1 = inEdges.get(0);
			ie2 = inEdges.get(1);
		} else {
			for(E e: HalfEdgeUtils.incomingEdges(v1)) {
				if(e.getLeftFace() == null) {
					ie1 = e;
					break;
				}
			}
			for(E e: HalfEdgeUtils.incomingEdges(v2)) {
				if(e.getLeftFace() == null) {
					ie2 = e;
					break;
				}
			}
		}
		if(ie1 == null || ie2 == null) {
			return false;
		}
		
		E splitE = insertEdge(v1,ie1,v2,ie2);
		double[] p1 = a.get(Position3d.class, v1);
		double[] p2 = a.get(Position3d.class, v2);
		double[] newCoords = Rn.linearCombination(null, .5, p1, .5, p2);
		V newV = TopologyAlgorithms.collapseEdge(splitE);
		a.set(Position.class, newV, newCoords);
		removeDigons(newV);
		return true;		
	}
	
	private static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void removeDigons(V v) {
		for(E e : HalfEdgeUtils.incomingEdges(v)) {
			if(e.getNextEdge() == e.getPreviousEdge()) {
				E eo = e.getOppositeEdge();
				eo.linkOppositeEdge(e.getNextEdge().getOppositeEdge());
				eo.getHalfEdgeDataStructure().removeEdge(e.getNextEdge());
				eo.getHalfEdgeDataStructure().removeEdge(e);
			}
		}
	}
	
	// Returns a list of incoming edges of v1 resp. v2, such that the left faces
	// of the edge are equal (may be null / hole) and this face contains v1 and v2
	private static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> List<E> findEdgesOfCommonHole(V v1, V v2) {
		List<E> inEdges = new LinkedList<E>();
		for(E e : HalfEdgeUtils.incomingEdges(v1)) {
			if(e.getLeftFace() != null) {
				continue;
			}
			E be = e.getNextEdge();
			while(be.getTargetVertex() != v1) {
				if(be.getTargetVertex() == v2) {
					inEdges.add(e);
					inEdges.add(be);
					return inEdges;
				}
				be = be.getNextEdge();
			}
		}
		return inEdges;
	}
	
	private static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	>  E insertEdge(V v1, E e1, V v2, E e2) {
		HalfEdgeDataStructure<V, E, F> hds = v1.getHalfEdgeDataStructure();
		E	ne = hds.addNewEdge(),
			neo = hds.addNewEdge();
		ne.linkOppositeEdge(neo);
		ne.setTargetVertex(v2);
		neo.setTargetVertex(v1);
		ne.linkNextEdge(e2.getNextEdge());
		neo.linkNextEdge(e1.getNextEdge());
		ne.linkPreviousEdge(e1);
		neo.linkPreviousEdge(e2);
		return ne;
	}
}
