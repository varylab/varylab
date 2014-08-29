package de.varylab.varylab.plugin.nurbs.scene;

import de.varylab.varylab.plugin.interaction.PointConstraint;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class NurbsSurfaceConstraint implements PointConstraint {

	protected NURBSSurface surface = null;
	
	protected double[] uv = null;

	public NurbsSurfaceConstraint(NURBSSurface surface) {
		this.surface  = surface;
	}
	
	@Override
	public double[] project(double[] src) {
		uv = surface.getClosestPointDomain(src);
		return surface.getSurfacePoint(uv);
	}

	public double[] getUV() {
		return uv;
	}

}
