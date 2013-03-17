package de.varylab.varylab.math.constraint;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.adapter.VertexGradientAdapter;
import de.varylab.varylab.plugin.smoothing.LaplacianSmoothing;

public class SmoothGradientConstraint implements Constraint {

	
	
	@Override
	public void editGradient(VHDS hds, int dim, DomainValue x, Gradient G) {
		AdapterSet as = new AdapterSet(new VertexGradientAdapter(G));
		LaplacianSmoothing.smoothCombinatorially(hds, as, true);
	}

	@Override
	public void editHessian(VHDS hds, int dim, DomainValue x, Hessian H) {
		// TODO Auto-generated method stub

	}

}
