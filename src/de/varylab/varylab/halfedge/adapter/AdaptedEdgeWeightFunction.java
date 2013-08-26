package de.varylab.varylab.halfedge.adapter;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Weight;
import de.varylab.varylab.functional.adapter.WeightFunction;
import de.varylab.varylab.halfedge.VEdge;

public class AdaptedEdgeWeightFunction implements WeightFunction<VEdge> {

	private AdapterSet
		aSet = null;
	
	public AdaptedEdgeWeightFunction(AdapterSet a) {
		aSet = a;
	}
	
	@Override
	public Double getWeight(VEdge e) {
		if (!aSet.isAvailable(Weight.class, e.getClass(), Double.class)) {
			return 1.0;
		}
		Double w = aSet.get(Weight.class, e, Double.class);
		return w == null ? 1.0 : w;
	}

}
