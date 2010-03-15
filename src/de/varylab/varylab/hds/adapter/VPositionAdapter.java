package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

@Position
public class VPositionAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	public VPositionAdapter() {
		super(VVertex.class, null, null, double[].class, true, true);
	}
	
	@Override
	public double getPriority() {
		return 1;
	}
	
	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		return v.position;
	}
	
	@Override
	public void setVertexValue(VVertex v, double[] value, AdapterSet a) {
		v.position = value;
	}
	
}
