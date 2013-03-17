package de.varylab.varylab.halfedge.adapter;

import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;

@Position
public class CoordinateMTJAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	private Vector
		x = null;
	private double 
		zScale = 0;

	public CoordinateMTJAdapter(Vector x, double zScale) {
		super(VVertex.class, null, null, double[].class, true, false);
		this.x = x;
		this.zScale = zScale;
	}
	
	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		double[] pos = new double[3];
		int i = v.getIndex();
		pos[0] = x.get(i*3 + 0);
		pos[1] = x.get(i*3 + 1);
		pos[2] = x.get(i*3 + 2) * zScale;
		return pos;
	}
	
}