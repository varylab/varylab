package de.varylab.varylab.math.constraint;

import java.util.HashMap;

import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public abstract class SubspaceConstraint implements Constraint {
	
	HashMap<VVertex,Subspace> 
		subspaceMap = new HashMap<VVertex, Subspace>();

	@Override
	public void editGradient(VHDS hds, int dim, DomainValue x, Gradient G) {
		for(VVertex v : subspaceMap.keySet()) {
			int i = v.getIndex();
			double[] vg = FunctionalUtils.getVectorFromGradient(G,3*i);
			vg = subspaceMap.get(v).projectOnto(vg);
			FunctionalUtils.setVectorToGradient(G,3*i,vg);
		}
	}

	@Override
	public void editHessian(VHDS hds, int dim, DomainValue x, Hessian H) {
		for(VVertex v : subspaceMap.keySet()) {
			int i = v.getIndex();
			for (int j = 0; j < dim; j++) {
				double[] hj = new double[]{H.get(3*i, j),H.get(3*i+1, j),H.get(3*i+2, j)};
				hj = subspaceMap.get(v).projectOnto(hj);
				H.set(3*i, j, hj[0]);
				H.set(3*i+1, j, hj[1]);
				H.set(3*i+2, j, hj[2]);
			}
		}
	}

}
