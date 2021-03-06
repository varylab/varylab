package de.varylab.varylab.halfedge.adapter;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Weight;
import de.varylab.varylab.functional.adapter.VolumeWeight;
import de.varylab.varylab.halfedge.VFace;

public class AdaptedFaceWeightFunction implements VolumeWeight<VFace> {

	private AdapterSet
		aSet = null;
	
	public AdaptedFaceWeightFunction(AdapterSet a) {
		aSet = a;
	}
	
	@Override
	public Double getWeight(VFace f) {
		if (!aSet.isAvailable(Weight.class, f.getClass(), Double.class)) {
			return 1.0;
		}
		Double w = aSet.get(Weight.class, f, Double.class);
		return w == null ? 1.0 : w;
	}

}
