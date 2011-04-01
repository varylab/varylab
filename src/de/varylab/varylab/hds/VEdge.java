package de.varylab.varylab.hds;

import de.jtem.halfedge.Edge;

public class VEdge extends Edge<VVertex, VEdge, VFace>{

	private double
		weight = 1.0;
	private int
		geodesicLabel = -1;
	
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getGeodesicLabel() {
		return geodesicLabel;
	}
	public void setGeodesicLabel(int geodesicLabel) {
		this.geodesicLabel = geodesicLabel;
	}
	
	@Override
	public void copyData(VEdge e) {
		super.copyData(e);
		weight = e.weight;
		geodesicLabel = e.geodesicLabel;
	}
	
}
