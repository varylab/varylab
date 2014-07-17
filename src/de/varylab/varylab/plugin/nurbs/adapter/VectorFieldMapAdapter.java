package de.varylab.varylab.plugin.nurbs.adapter;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.plugin.nurbs.type.CurvatureLineField;

@CurvatureLineField
public class VectorFieldMapAdapter extends AbstractAdapter<double[]>{
	
	private Map<Vertex<?,?,?>,double[]>
		vertexVectorMap = new HashMap<Vertex<?,?,?>, double[]>();
	private String
		name = "Vector Field";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public VectorFieldMapAdapter() {
		super(double[].class,true,true);
	}

	public VectorFieldMapAdapter(Map<Vertex<?,?,?>,double[]> vf, String name) {
		super(double[].class,true,true);
		vertexVectorMap = new HashMap<Vertex<?,?,?>, double[]>(vf);
		this.name = name;
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
	> double[] getV(V v, AdapterSet a) {
		if(vertexVectorMap.containsKey(v)) {
			return vertexVectorMap.get(v);
		} else {
			return new double[]{0.0,0.0};
		}
	}
	
	@Override
	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> void setV(V v, double[] vec, AdapterSet a) {
		vertexVectorMap.put(v,vec);
	}
	
	@Override
	public double getPriority() {
		return 0;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
