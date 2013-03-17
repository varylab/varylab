package de.varylab.varylab.halfedge.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;

public class DomainValueNormal extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	
	public DomainValueNormal() {
		super(VVertex.class, VEdge.class, VFace.class, double[].class, true, false);
	}
	
	@Override
	public double[] getFaceValue(VFace f, AdapterSet a) {
		return super.getFaceValue(f, a);
	}
	
}
