package de.varylab.varylab.hds.adapter;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.WeightFunction;

public class ConstantWeight implements WeightFunction<VEdge> {

	public double 
		w = 1.0;
	public boolean 
		ignoreBoundary = false;
	
	public ConstantWeight(double w, boolean ignoreBoundary) {
		this.w = w;
	}
	
	@Override
	public Double getWeight(VEdge e) {
		if (HalfEdgeUtils.isBoundaryEdge(e) && ignoreBoundary) {
			return 0.0;
		} else {
			return w;
		}
	}
	
}