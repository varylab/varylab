package de.varylab.varylab.hds.adapter;

import de.jreality.math.Rn;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.WeightFunction;

public class TextureWeightAdapter implements WeightFunction<VEdge> {

	@Override
	public Double getWeight(VEdge e) {
		double[] ev = Rn.subtract(null, e.getTargetVertex().texcoord, e.getStartVertex().texcoord);
		double w = 1.0/Math.sqrt(ev[0]*ev[0]+ev[1]*ev[1]);
		return w;
	}

}