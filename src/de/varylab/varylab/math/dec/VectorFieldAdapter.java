package de.varylab.varylab.math.dec;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;

@VectorField
public class VectorFieldAdapter extends AbstractAdapter<double[]> {
	
	Map<Face<?,?,?>,double[]>
		faceVectorMap = new HashMap<Face<?,?,?>, double[]>();
	
	public VectorFieldAdapter() {
		super(double[].class,true,true);
	}
	
	public VectorFieldAdapter(Map<Face<?,?,?>,double[]> vf) {
		super(double[].class,true,true);
		faceVectorMap = new HashMap<Face<?,?,?>, double[]>(vf);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Face.class.isAssignableFrom(nodeClass);
	}

	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> double[] getF(F f, AdapterSet a) {
		if(faceVectorMap.containsKey(f)) {
			return faceVectorMap.get(f);
		} else {
			return new double[]{0.0,0.0,0.0};
		}
	}

	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> void setF(F f, double[] vec, AdapterSet a) {
		faceVectorMap.put(f,vec);
	}
	
	@Override
	public double getPriority() {
		return 0;
	}
}