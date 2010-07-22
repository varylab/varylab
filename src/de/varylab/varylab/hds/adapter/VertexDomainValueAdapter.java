package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

@Position
public class VertexDomainValueAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	private DomainValue x = null;
	
	public VertexDomainValueAdapter(DomainValue x) {
		super(VVertex.class, null, null, double[].class, true, true);
		this.x = x;
	}
	
	@Override
	public double getPriority() {
		return 1;
	}
	
	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		double[] pos = new double[3];
		FunctionalUtils.getPosition(v, x, pos);
		return pos;
	}
	
	@Override
	public void setVertexValue(VVertex v, double[] value, AdapterSet a) {
		FunctionalUtils.setVectorToDomainValue(x,3*v.getIndex(),value);
	}
	
}
