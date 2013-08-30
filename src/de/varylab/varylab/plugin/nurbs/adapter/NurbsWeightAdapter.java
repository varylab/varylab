package de.varylab.varylab.plugin.nurbs.adapter;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.plugin.nurbs.type.NurbsWeight;

@NurbsWeight
public class NurbsWeightAdapter extends AbstractAdapter<Double> {

	Map<Vertex<?,?,?>, Double>
		weightMap = new HashMap<Vertex<?,?,?>, Double>();
	
	public NurbsWeightAdapter() {
		super(Double.class,true,true);
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Vertex.class.isAssignableFrom(nodeClass);
	}
	
	@Override
	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> Double getV(V v, AdapterSet a) {
		Double val = weightMap.get(v);
		if(val == null) return 1.0;
		return val;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> void setV(V v, Double value, AdapterSet a) {
		weightMap.put(v, value);
	}

	@Override
	public void update() {
		for(Vertex<?,?,?> v : weightMap.keySet()) {
			if(!v.isValid()) {
				weightMap.remove(v);
			}
		}
	}
}
