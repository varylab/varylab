package de.varylab.varylab.math;

import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.hds.VHDS;

public interface Constraint{
	
	public void editGradient(VHDS hds, int dim, Gradient G);
	
	public void editHessian(VHDS hds, int dim, Hessian G);
	
}