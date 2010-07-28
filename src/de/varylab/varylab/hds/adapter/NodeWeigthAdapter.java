package de.varylab.varylab.hds.adapter;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Weight;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

@Weight
public class NodeWeigthAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, Double> {

	public NodeWeigthAdapter() {
		super(VVertex.class, VEdge.class, VFace.class, Double.class, true, true);
	}

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> Double get(N n, AdapterSet a) {
		if (n instanceof VVertex) {
			return ((VVertex) n).getWeight();
		}
		if (n instanceof VEdge) {
			return ((VEdge) n).getWeight();
		}
		if (n instanceof VFace) {
			return ((VFace) n).getWeight();
		}
		return 0.0;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> void set(N n, Double w, AdapterSet a) {
		if (n instanceof VVertex) {
			((VVertex) n).setWeight(w);
		}
		if (n instanceof VEdge) {
			((VEdge) n).setWeight(w);
		}
		if (n instanceof VFace) {
			((VFace) n).setWeight(w);
		}
	}
	

	@Override
	public double getPriority() {
		return 0;
	}

}
