package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Weight;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.WeightFunction;

public class AdaptedEdgeWeightFunction implements WeightFunction<VEdge> {

	private AdapterSet
		aSet = null;
	
	public AdaptedEdgeWeightFunction(AdapterSet a) {
		aSet = a;
	}
	
	@Override
	public Double getWeight(VEdge e) {
		Double w = aSet.get(Weight.class, e, Double.class);
		return w == null ? 1.0 : w;
	}

}
