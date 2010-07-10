package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TexCoordinate;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

@TexCoordinate
public class VTexCoordAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	public VTexCoordAdapter() {
		super(VVertex.class, null, null, double[].class, true, true);
	}
	
	@Override
	public double getPriority() {
		return 1;
	}
	
	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		return v.texcoord;
	}
	
	@Override
	public void setVertexValue(VVertex v, double[] value, AdapterSet a) {
		v.texcoord = value;
	}
	
}
