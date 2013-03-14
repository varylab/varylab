package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.type.GeodesicLabel;

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
