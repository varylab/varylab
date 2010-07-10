package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

@Position
public class VertexGradientAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	private Gradient g = null;

	public VertexGradientAdapter(Gradient g) {
		super(VVertex.class, null, null, double[].class, true, true);
		this.g = g;
	}

	@Override
	public double getPriority() {
		return 1;
	}

	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		return FunctionalUtils.getVectorFromGradient(g, 3*v.getIndex());
	}

	@Override
	public void setVertexValue(VVertex v, double[] value, AdapterSet a) {
		FunctionalUtils.setVectorToGradient(g,3*v.getIndex(),value);
	}
		
}
