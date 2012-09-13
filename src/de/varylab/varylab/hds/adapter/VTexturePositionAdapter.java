package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

@TexturePosition
public class VTexturePositionAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	public VTexturePositionAdapter() {
		super(VVertex.class, null, null, double[].class, true, true);
	}
	
	@Override
	public double getPriority() {
		return 1;
	}
	
	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		return v.T;
	}
	
	@Override
	public void setVertexValue(VVertex v, double[] value, AdapterSet a) {
		if (value.length == 2) {
			v.T[0] = value[0];
			v.T[1] = value[1];
			v.T[2] = 0.0;
			v.T[3] = 1.0;
		} else 
		if (value.length == 3) {
			v.T[0] = value[0];
			v.T[1] = value[1];
			v.T[2] = value[2];
			v.T[3] = 1.0;
		} else 
		if (value.length == 4) {
			System.arraycopy(value, 0, v.T, 0, 4);
		} else {
			throw new IllegalArgumentException("invalid dimension in set vertex value of CoVertex");
		}
	}
	
}
