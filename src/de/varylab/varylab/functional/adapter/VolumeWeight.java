package de.varylab.varylab.functional.adapter;

import de.jtem.halfedge.Face;

public interface VolumeWeight <F extends Face<?, ?, F>> {
	public Double getWeight(F f);
}