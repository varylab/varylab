package de.varylab.varylab.plugin.nurbs.scene;

import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.plugin.PointSelectionPlugin.Parameter;

public class NurbsSurfaceDirectionConstraint extends NurbsSurfaceConstraint {

	private double[] 
			initialUV = null;
	private Parameter 
			parameterDirection = Parameter.UV;

	public NurbsSurfaceDirectionConstraint(NURBSSurface surface, double[] initialUV, Parameter parameterDirection) {
		super(surface);
		this.initialUV = initialUV;
		this.parameterDirection = parameterDirection;
	}
	
	@Override
	public double[] project(double[] src) {
		uv = surface.getClosestPointDomainDir(src,initialUV,parameterDirection);
		return surface.getSurfacePoint(uv[0], uv[1]);
	}
	

	public void setParameterDirection(Parameter p) {
		parameterDirection = p;
	}
	
	public Parameter getParameterDirection() {
		return parameterDirection;
	}
	
	public void setInitialUV(double[] initialUV) {
		this.initialUV = initialUV;
	}

	public void resetInitialUV() {
		if(uv != null){
			System.arraycopy(uv, 0, initialUV, 0, 2);
		}
	}

}
