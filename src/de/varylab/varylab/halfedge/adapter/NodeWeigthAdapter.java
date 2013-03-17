package de.varylab.varylab.halfedge.adapter;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Weight;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;

@Weight
public class NodeWeigthAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, Double> {

	public NodeWeigthAdapter() {
		super(VVertex.class, VEdge.class, VFace.class, Double.class, true, true);
	}

	@Override
	public Double getVertexValue(VVertex v, AdapterSet a) {
		return v.getWeight();
	}
	@Override
	public Double getEdgeValue(VEdge e, AdapterSet a) {
		return e.getWeight();
	}
	@Override
	public Double getFaceValue(VFace f, AdapterSet a) {
		return f.getWeight();
	}
	
	@Override
	public void setVertexValue(VVertex v, Double value, AdapterSet a) {
		v.setWeight(value);
	}
	@Override
	public void setEdgeValue(VEdge e, Double value, AdapterSet a) {
		e.setWeight(value);
	}
	@Override
	public void setFaceValue(VFace f, Double value, AdapterSet a) {
		f.setWeight(value);
	}

}
