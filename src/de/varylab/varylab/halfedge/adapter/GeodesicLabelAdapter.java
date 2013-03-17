package de.varylab.varylab.halfedge.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.type.GeodesicLabel;

@GeodesicLabel
public class GeodesicLabelAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, Integer> {

	public GeodesicLabelAdapter() {
		super(null, VEdge.class, null, Integer.class, true, true);
	}

	@Override
	public Integer getEdgeValue(VEdge e, AdapterSet a) {
		return e.getGeodesicLabel();
	}
	
	@Override
	public void setEdgeValue(VEdge e, Integer value, AdapterSet a) {
		e.setGeodesicLabel(value);
	}

}
