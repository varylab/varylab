package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

@Position
public class CoordinateArrayAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	private double[]
		x = null;
	private double 
		zScale = 0;

	public CoordinateArrayAdapter(double[] x, double zScale) {
		super(VVertex.class, null, null, double[].class, true, false);
		this.x = x;
		this.zScale = zScale;
	}
	
	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		double[] pos = new double[3];
		int i = v.getIndex();
		pos[0] = x[i*3 + 0];
		pos[1] = x[i*3 + 1];
		pos[2] = x[i*3 + 2] * zScale;
		return pos;
	}
	
}