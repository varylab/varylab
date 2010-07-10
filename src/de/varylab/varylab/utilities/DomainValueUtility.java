package de.varylab.varylab.utilities;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.LaplaceOperator;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public class DomainValueUtility {

	public static double[] getNormal(VHDS hds, VFace f, DomainValue x) {
		double[] normal = new double[3];
		double[] 
		       v1 = new double[3],
		       v2 = new double[3],
		       v3 = new double[3];
		VEdge e = f.getBoundaryEdge();
		FunctionalUtils.getPosition(e.getStartVertex(), x, v1);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, v2);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(),x,v3);
		Rn.crossProduct(normal, Rn.subtract(null, v1, v2), Rn.subtract(null, v3, v2));
		return normal;
	}
	
	public static double[] getAverageNormal(VHDS hds, VVertex v, DomainValue x) {
		double[] normal = new double[3];
		List<VEdge> star = HalfEdgeUtils.incomingEdges(v);
		int numFaces = 0;
		for (VEdge e : star) {
			if (e.getLeftFace() == null) continue;
			double[] n = getNormal(hds, e.getLeftFace(), x);
			Rn.add(normal, n, normal);
			numFaces++;
		}
		Rn.times(normal, 1.0 / numFaces, normal);
		return normal;
	}
	
	public static double[] getCotanNormal(VHDS hds, VVertex v, DomainValue x) {
		double[] normal = new double[3];
		LaplaceOperator.evaluate(hds, v, x, normal);
		return normal;
	}
	
}
