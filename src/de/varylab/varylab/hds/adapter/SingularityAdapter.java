package de.varylab.varylab.hds.adapter;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.hds.adapter.type.Singularity;

@Singularity
public class SingularityAdapter extends AbstractAdapter<Double> {
	
	Map<Vertex<?,?,?>,Double> 
		singularities = new HashMap<Vertex<?,?,?>, Double>();
	
	public SingularityAdapter() {
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
		if(singularities.containsKey(v)) {
			return singularities.get(v);
		} else {
			return 0.0;
		}
	}
	

	@Override
	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> void setV(V v, Double d, AdapterSet a) {
		singularities.put(v,d);
	}
	
	
	@Override
	public double getPriority() {
		return 0;
	}
}