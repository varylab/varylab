package de.varylab.varylab.hds;

import de.jtem.halfedge.Edge;

public class VEdge extends Edge<VVertex, VEdge, VFace>{

	private double
		weight = 1.0;
	
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}

	
}
