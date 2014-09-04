package de.varylab.varylab.plugin.nurbs.math;

import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.VectorFields;

public interface VectorFieldProvider {

	public double[] getVectorField(double[] uv, VectorFields vf);
	
}
