package de.varylab.varylab.functional;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;

public class IncircleFunctional implements Functional<VVertex, VEdge, VFace> {

	private double[]
	    p1 = new double[3],
	    p2 = new double[3],
	    p3 = new double[3],
		p4 = new double[3];
	
	
	public IncircleFunctional() {
	}

	@Override
	public <
		HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>
	> void evaluate(
		HDS hds, 
		DomainValue x, 
		Energy E, 
		Gradient G, 
		Hessian H
	) {
		if (E != null) {
			E.setZero();
			for (VFace f : hds.getFaces()) {
				List<VVertex> b = HalfEdgeUtils.boundaryVertices(f);
				if (b.size() != 4) continue;
				VVertex v1 = b.get(0);
				VVertex v2 = b.get(1);
				VVertex v3 = b.get(2);
				VVertex v4 = b.get(3);
				FunctionalUtils.getPosition(v1, x, p1);
				FunctionalUtils.getPosition(v2, x, p2);
				FunctionalUtils.getPosition(v3, x, p3);
				FunctionalUtils.getPosition(v4, x, p4);
				double l12 = Rn.euclideanDistance(p1, p2);
				double l34 = Rn.euclideanDistance(p3, p4);
				double l23 = Rn.euclideanDistance(p2, p3);
				double l14 = Rn.euclideanDistance(p1, p4);
				double e = l12 + l34 - l23 - l14;
				E.add(e * e);
			}
		}
		if (G != null) {
			G.setZero();
			for (VFace f : hds.getFaces()) {
				List<VVertex> b = HalfEdgeUtils.boundaryVertices(f);
				if (b.size() != 4) continue;
				VVertex v1 = b.get(0);
				VVertex v2 = b.get(1);
				VVertex v3 = b.get(2);
				VVertex v4 = b.get(3);
				FunctionalUtils.getPosition(v1, x, p1);
				FunctionalUtils.getPosition(v2, x, p2);
				FunctionalUtils.getPosition(v3, x, p3);
				FunctionalUtils.getPosition(v4, x, p4);
				double l12 = Rn.euclideanDistance(p1, p2);
				double l34 = Rn.euclideanDistance(p3, p4);
				double l23 = Rn.euclideanDistance(p2, p3);
				double l14 = Rn.euclideanDistance(p1, p4);
				double e = l12 + l34 - l23 - l14;
				for (int i = 0; i < 3; i++) {
					G.add(v1.getIndex() * 3 + i, 2 * ((p1[i] - p2[i]) / l12 - (p1[i] - p4[i]) / l14) * e);
					G.add(v2.getIndex() * 3 + i, 2 * ((p2[i] - p1[i]) / l12 - (p2[i] - p3[i]) / l23) * e);
					G.add(v3.getIndex() * 3 + i, 2 * ((p3[i] - p4[i]) / l34 - (p3[i] - p2[i]) / l23) * e);
					G.add(v4.getIndex() * 3 + i, 2 * ((p4[i] - p3[i]) / l34 - (p4[i] - p1[i]) / l14) * e);
				}
			}
		}
	}
	
	
	
	

	@Override
	public boolean hasHessian() {
		return false;
	}

	@Override
	public <
		HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>
	> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <
		HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace
	>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}

	@Override
	public boolean hasGradient() {
		return true;
	}
}