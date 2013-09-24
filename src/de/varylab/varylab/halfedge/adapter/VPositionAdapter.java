package de.varylab.varylab.halfedge.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;

@Position
public class VPositionAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	public VPositionAdapter() {
		super(VVertex.class, null, VFace.class, double[].class, true, true);
	}
	
	@Override
	public double getPriority() {
		return 1;
	}
	
	@Override
	public void setVertexValue(VVertex v, double[] value, AdapterSet a) {
		double[] P = v.getP();
		if (value.length == 2) {
			P[0] = value[0];
			P[1] = value[1];
			P[2] = 0.0;
			P[3] = 1.0;
		} else 
		if (value.length == 3) {
			P[0] = value[0];
			P[1] = value[1];
			P[2] = value[2];
			P[3] = 1.0;
		} else 
		if (value.length == 4) {
			System.arraycopy(value, 0, P, 0, 4);
		} else {
			throw new IllegalArgumentException("invalid dimension in set vertex value of CoVertex");
		}
	}
	
	@Override
	public void setFaceValue(VFace f, double[] value, AdapterSet a) {
		double[] P = f.getP();
		if (value.length == 2) {
			P[0] = value[0];
			P[1] = value[1];
			P[2] = 0.0;
			P[3] = 1.0;
		} else 
		if (value.length == 3) {
			P[0] = value[0];
			P[1] = value[1];
			P[2] = value[2];
			P[3] = 1.0;
		} else
		if (value.length == 4) {
			System.arraycopy(value, 0, P, 0, 4);
		} else {
			throw new IllegalArgumentException("invalid dimension in set vertex value of CoFace");
		}
	}
	

	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		return v.getP();
	}
	
	@Override
	public double[] getFaceValue(VFace f, AdapterSet a) {
		return f.getP();
	}
	
}
