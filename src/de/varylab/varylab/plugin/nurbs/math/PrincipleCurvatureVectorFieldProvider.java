package de.varylab.varylab.plugin.nurbs.math;

import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.VectorFields;

public class PrincipleCurvatureVectorFieldProvider implements VectorFieldProvider {

	private NURBSSurface surface = null;
	
	public PrincipleCurvatureVectorFieldProvider(NURBSSurface surf) {
		surface = surf;
	}
	
	@Override
	public double[] getVectorField(double[] uv, VectorFields vf) {
		return getMaxMinCurv(uv, vf);
	}

	public double[] getMaxMinCurv(double[] p, VectorFields vf) {
		if (vf == VectorFields.FIRST) {
			return NURBSCurvatureUtility.curvatureAndDirections(surface, p).getPrincipalDirections()[1];
		} else {	
			return NURBSCurvatureUtility.curvatureAndDirections(surface, p).getPrincipalDirections()[0];
		}
	}
}
