package de.varylab.varylab.math.functional;

import de.jtem.halfedge.Face;

public class PlanarFacesAdapters {

	public static interface VolumeWeight <F extends Face<?, ?, F>> {
		public Double getWeight(F f);
	}
	
}
