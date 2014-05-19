package de.varylab.varylab.plugin.rf;

import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.halfedge.VEdge;

public class RodConnectivityAdapter extends AbstractAdapter<VEdge> {
	
	Map<VEdge, VEdge>
		nextRodMap = null;
	
	public RodConnectivityAdapter(Map<VEdge, VEdge> nextRodMap) {
		super(VEdge.class, true, false);
		this.nextRodMap = nextRodMap; 
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> VEdge getE(E e, AdapterSet a) {
		return nextRodMap.get(e);
	}
}