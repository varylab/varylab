package de.varylab.varylab.optimization.constraint;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.utilities.DomainValueUtility;

public class TangentialConstraint implements Constraint {

	@Override
	public void editGradient(VHDS hds, int dim, DomainValue x, Gradient G) {
		for(VVertex v : hds.getVertices()) {
			int i = v.getIndex();
			double[] vg = FunctionalUtils.getVectorFromGradient(G,3*i);
			Rn.projectOntoComplement(vg, vg, DomainValueUtility.getAverageNormal(hds, v, x));
			FunctionalUtils.setVectorToGradient(G,3*i,vg);
		}
	}

	@Override
	public void editHessian(VHDS hds, int dim, DomainValue x, Hessian H) {
		for(VVertex v : hds.getVertices()) {
			int i = v.getIndex();
			double[] vn = DomainValueUtility.getAverageNormal(hds, v, x);
			for (int j = 0; j < dim; j++) {
				double[] hj = new double[]{H.get(3*i, j),H.get(3*i+1, j),H.get(3*i+2, j)};
				Rn.projectOntoComplement(hj, hj, vn);
				H.set(3*i, j, hj[0]);
				H.set(3*i+1, j, hj[1]);
				H.set(3*i+2, j, hj[2]);
			}
		}
	}
}
