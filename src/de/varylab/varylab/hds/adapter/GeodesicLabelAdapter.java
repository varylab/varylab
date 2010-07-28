package de.varylab.varylab.hds.adapter;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

@GeodesicLabel
public class GeodesicLabelAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, Integer> {

	public GeodesicLabelAdapter() {
		super(null, VEdge.class, null, Integer.class, true, true);
	}

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> Integer get(N n, AdapterSet a) {
		if (n instanceof VEdge) {
			return ((VEdge) n).getGeodesicLabel();
		}
		return -1;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> void set(N n, Integer label, AdapterSet a) {
		if (n instanceof VEdge) {
			((VEdge) n).setGeodesicLabel(label);
		}
	}
	

	@Override
	public double getPriority() {
		return 0;
	}

}
