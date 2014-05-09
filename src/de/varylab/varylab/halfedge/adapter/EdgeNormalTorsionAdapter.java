package de.varylab.varylab.halfedge.adapter;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;

public class EdgeNormalTorsionAdapter extends AbstractAdapter<Double> {

	public EdgeNormalTorsionAdapter() {
		super(Double.class, true, false);
	}

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getE(E e, AdapterSet a) {
		V 	v1 = e.getStartVertex(),
			v2 = e.getTargetVertex();
		double[] 	n1 = a.getD(Normal.class, v1),
					n2 = a.getD(Normal.class,v2);
		return 180*Rn.euclideanAngle(n1, n2)/Math.PI;
	}
	
	@Override
	public double getPriority() {
		return 0;
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}
	
	@Override
	public String toString() {
		return "Edge normal torsion";
	}

}
