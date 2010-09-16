package de.varylab.varylab.math.dec;

import java.util.HashMap;
import java.util.Map;

import no.uib.cipr.matrix.Vector;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;

@Connection
public class ConnectionAdapter extends AbstractAdapter<Double> {

	private Map<Edge<?,?,?>,Double> 
		edgeAngleMap = new HashMap<Edge<?,?,?>, Double>();
	
	public ConnectionAdapter(Vector val, Map<Edge<?,?,?>,Integer> map) {
		super(Double.class, true, false);
		for(Edge<?,?,?> e: map.keySet()) {
			edgeAngleMap.put(e, (e.isPositive()?1.0:-1.0)*val.get(map.get(e)));
		}
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}

	@Override
	public double getPriority() {
		return 0;
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getE(E e, AdapterSet a) {
		return edgeAngleMap.get(e);
	}
	
}