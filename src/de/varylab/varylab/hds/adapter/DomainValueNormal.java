package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

public class DomainValueNormal extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	
	public DomainValueNormal() {
		super(VVertex.class, VEdge.class, VFace.class, double[].class, true, false);
	}
	
	@Override
	public double[] getFaceValue(VFace f, AdapterSet a) {
		return super.getFaceValue(f, a);
	}
	
}
