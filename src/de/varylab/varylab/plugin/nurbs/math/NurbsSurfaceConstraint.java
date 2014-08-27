package de.varylab.varylab.plugin.nurbs.math;

import de.varylab.varylab.plugin.interaction.PointConstraint;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class NurbsSurfaceConstraint implements PointConstraint {

	private NURBSSurface surface = null;

	public NurbsSurfaceConstraint(NURBSSurface surface) {
		this.surface  = surface;
	}
	
	@Override
	public double[] project(double[] src) {
		return surface.getClosestPoint(src);
	}

}
