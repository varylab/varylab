package de.varylab.varylab.plugin.affineminimal;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
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

class AffineMinimalFunctional implements Functional<VVertex, VEdge, VFace> {

	@Override
	public <HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>> void evaluate(
			HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
		if (E != null) {
			E.setZero();
		}
		if (G != null) {
			G.setZero();
		}
		double e = 0.0;
		double[][] vol = new double[3][3];
		for (VEdge e1 : hds.getEdges()) {
			if (e1.getLeftFace() == null) {
				continue;
			}
			VEdge e2 = getOppositeEdgeInFace(e1);
			if (e2 == null) {
				continue;
			}
			VEdge e3 = getOppositeEdgeInFace(e2.getOppositeEdge());
			if (e3 == null) {
				continue;
			}

			vol[0] = getEdgeVector(e1, x);
			vol[1] = getEdgeVector(e2, x);
			vol[2] = getEdgeVector(e3.getOppositeEdge(), x);
			double det = Rn.determinant(vol);
			e += det * det;
			if (G != null) {
				double[] e1xe2 = Rn.crossProduct(null, vol[0], vol[1]), e1xe3 = Rn
						.crossProduct(null, vol[0], vol[2]), e2xe3 = Rn
						.crossProduct(null, vol[1], vol[2]);
				double[] g = new double[3];

				double scale = -2.0 * det;
				Rn.times(g, scale, e2xe3);
				FunctionalUtils.addVectorToGradient(G, 3 * e1.getStartVertex()
						.getIndex(), g);

				scale = 2.0 * det;
				Rn.times(g, scale, e2xe3);
				FunctionalUtils.addVectorToGradient(G, 3 * e1.getTargetVertex()
						.getIndex(), g);

				scale = 2.0 * det;
				Rn.times(g, scale, e1xe3);
				FunctionalUtils.addVectorToGradient(G, 3 * e2.getStartVertex()
						.getIndex(), g);

				scale = -2.0 * det;
				Rn.times(g, scale, e1xe3);
				FunctionalUtils.addVectorToGradient(G, 3 * e2.getTargetVertex()
						.getIndex(), g);

				scale = 2.0 * det;
				Rn.times(g, scale, e1xe2);
				FunctionalUtils.addVectorToGradient(G, 3 * e3.getStartVertex()
						.getIndex(), g);

				scale = -2.0 * det;
				Rn.times(g, scale, e1xe2);
				FunctionalUtils.addVectorToGradient(G, 3 * e3.getTargetVertex()
						.getIndex(), g);
			}
		}
		if (E != null) {
			E.set(e);
		}
	}

	private double[] getEdgeVector(VEdge e, DomainValue x) {
		double[] v1 = new double[3], v2 = new double[3];
		FunctionalUtils.getPosition(e.getStartVertex(), x, v1);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, v2);

		return Rn.subtract(null, v2, v1);
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>> int getDimension(
			HDS hds) {
		return 3 * hds.numVertices();
	}

	@Override
	public boolean hasHessian() {
		return false;
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>> int[][] getNonZeroPattern(
			HDS hds) {
		return null;
	}

	private <E extends Edge<?, E, F>, F extends Face<?, E, F>> E getOppositeEdgeInFace(
			E e) {
		F f = e.getLeftFace();
		if (f == null) {
			return null;
		}
		E oe = e;
		if ((HalfEdgeUtils.boundaryEdges(f).size() % 2) == 0) {
			for (int i = 0; i < HalfEdgeUtils.boundaryEdges(f).size() / 2; ++i) {
				oe = oe.getNextEdge();
			}
		} else {
			return null;
		}
		return oe;
	}

	@Override
	public boolean hasGradient() {
		return true;
	}
}