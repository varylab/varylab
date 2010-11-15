package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

@TexturePosition
public class VTexturePositionAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	private static double[]
	    defaultTexCoordinate = {0, 0, 0, 1};
	
	public VTexturePositionAdapter() {
		super(VVertex.class, null, null, double[].class, true, true);
	}
	
	@Override
	public double getPriority() {
		return 1;
	}
	
	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		if (v.texcoord != null) {
			return v.texcoord;
		} else {
			return defaultTexCoordinate;
		}
	}
	
	@Override
	public void setVertexValue(VVertex v, double[] value, AdapterSet a) {
		switch (value.length) {
		case 2:
			v.texcoord = new double[] {value[0], value[1], 0, 1.0};
			break;
		case 3:
			v.texcoord = new double[] {value[0], value[1], value[2], 1.0};
			break;
		case 4:
		default:	
			v.texcoord = value;
			break;
		} 
	}
	
}
