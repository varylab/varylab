package de.varylab.varylab.plugin.nurbs.math;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class PointDistanceCalculator {
	
	public static double[] surfacePoint(double[] point, NURBSSurface ns){
		double[] p = {1,0,0};
		return p;
	}
	
	public static double distance(double[] surfacePoint, double[] point){
		return Rn.euclideanDistance(surfacePoint, point);
	}

}
