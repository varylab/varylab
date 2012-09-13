package de.varylab.varylab.hds;

import de.jtem.halfedge.Face;

public class VFace extends Face<VVertex, VEdge, VFace> {

	public double[]
		P = {0, 0, 0, 1};
	private double
		weight = 1.0;
	
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Override
	public void copyData(VFace f) {
		super.copyData(f);
		weight = f.weight;
	}
	
}
