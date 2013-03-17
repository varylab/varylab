package de.varylab.varylab.halfedge.adapter;

import no.uib.cipr.matrix.Vector;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.EdgeIndex;
import de.varylab.varylab.halfedge.adapter.type.Connection;

@Connection
public class ConnectionAdapter extends AbstractAdapter<Double> {

	private Vector 
		edgeValue = null;
	
	public ConnectionAdapter(Vector val) {
		super(Double.class, true, false);
		edgeValue = val;
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}

	@Override
	public double getPriority() {
		return 0;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getE(E e, AdapterSet a) {
		int ei = a.get(EdgeIndex.class, e, Integer.class);
		return (e.isPositive()?1.0:-1.0)*edgeValue.get(ei);
	}
	
}