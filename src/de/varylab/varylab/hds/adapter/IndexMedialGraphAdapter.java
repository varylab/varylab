package de.varylab.varylab.hds.adapter;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;

public class IndexMedialGraphAdapter extends AbstractAdapter<Double> {
	
//	private double digits = 1E4;
	
	public IndexMedialGraphAdapter() {
		super(Double.class, true, false);
	}

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getV(V v, AdapterSet a) {
		if(HalfEdgeUtils.isBoundaryVertex(v)) {
			return null;
		}
		throw new RuntimeException("Please check in the method IsothermicUtility.alphaRotation()");
//		return Math.round(digits*IsothermicUtility.alphaRotation(v, a) / (2.0 * Math.PI))/digits;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getF(F f, AdapterSet a) {
		throw new RuntimeException("Please check in the method IsothermicUtility.alphaRotation()");
//		return Math.round(digits*IsothermicUtility.alphaRotation(f, a) / (2.0 * Math.PI))/digits;
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Vertex.class.isAssignableFrom(nodeClass) || Face.class.isAssignableFrom(nodeClass);
	}
	
	@Override
	public String toString() {
		return "Index Medial Graph";
	}
	
}