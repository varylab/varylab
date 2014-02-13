package de.varylab.varylab.plugin.topology;

import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class ExplodePlugin extends AlgorithmPlugin{

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Topology;
	}

	@Override
	public String getAlgorithmName() {
		return "Explode";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		HDS newHDS = hi.createEmpty(hds);
		HalfedgeLayer layer = new HalfedgeLayer(hi);
		layer.setName(hi.getActiveLayer().getName() + "--explode");
		for(F face : hds.getFaces()) {
			F newFace = addNGon(newHDS, HalfEdgeUtils.boundaryEdges(face).size());
			copyCoords(face, a, newFace, layer.getEffectiveAdapters());
		}
		
		layer.set(newHDS);
		hi.addLayer(layer);
		hi.activateLayer(layer);
		
	}
	
	private static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void copyCoords(F oldFace, AdapterSet oldAs,F newFace, AdapterSet newAs) {
		E ne = newFace.getBoundaryEdge();
		for(E e : HalfEdgeUtils.boundaryEdges(oldFace)) {
			newAs.set(Position.class, ne.getTargetVertex(), 
					oldAs.getD(Position4d.class, e.getTargetVertex()));
			ne = ne.getNextEdge();
		}
	}
	
	private static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> F addNGon(HDS hds, int n ) {
		F newFace = hds.addNewFace();
		List<V> newVertices = hds.addNewVertices(n);
		List<E> newEdges = hds.addNewEdges(2*n);
		for (int i = 0; i < n; i++) {
			newEdges.get(i).setLeftFace(newFace);
			
			newEdges.get(i).linkOppositeEdge(newEdges.get(i+n));
			
			newEdges.get(i).linkNextEdge(newEdges.get((i+1)%n));
			newEdges.get(i).linkPreviousEdge(newEdges.get((i-1+n)%n));
			
			newEdges.get(i+n).linkOppositeEdge(newEdges.get(i));
			
			newEdges.get(i+n).linkNextEdge(newEdges.get((i-1+n)%n + n));
			newEdges.get(i+n).linkPreviousEdge(newEdges.get((i+1)%n + n));
			
			newEdges.get(i).setTargetVertex(newVertices.get((i+1)%n));
			newEdges.get(n+i).setTargetVertex(newVertices.get(i));
		}
		return newFace;
	}
}
