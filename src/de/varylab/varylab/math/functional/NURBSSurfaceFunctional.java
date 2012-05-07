package de.varylab.varylab.math.functional;

import java.util.HashMap;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.NURBSTree;

public class NURBSSurfaceFunctional<V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>>
		implements Functional<V, E, F> {

	private HashMap<V, double[]> closestPointMap = new HashMap<V, double[]>();
	private NURBSSurface refSurface = null;

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> void evaluate(HDS hds,
			DomainValue x, Energy E, Gradient G, Hessian H) {
		if (refSurface == null) {
			if (E != null)
				E.setZero();
			if (G != null)
				G.setZero();
			return;
		}
		if (E != null || G != null) {
			double[] vpos = new double[4];
			vpos[3] = 1.0;
			double firstTimeDouble = System.currentTimeMillis();
			NURBSTree nt = null;
			for (V v : hds.getVertices()) {
				FunctionalUtils.getPosition(v, x, vpos);
				double[] pt = refSurface.getClosestPoint(vpos, nt);
				Pn.dehomogenize(pt, pt);
				closestPointMap.put(v, pt);
			}
			double lastTimeDouble = System.currentTimeMillis();
			System.out.println("time functional " + (lastTimeDouble - firstTimeDouble));
		}
		if (E != null) {
			E.set(evaluate(hds, x));
		}
		if (G != null) {
			evaluateGradient(hds, x, G);
		}
		closestPointMap.clear();
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return 3 * hds.numVertices();
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(
			HDS hds) {
		return null;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}

	// Calculate the energy of a given configuration
	public double evaluate(HalfEdgeDataStructure<V, E, F> hds, DomainValue x) {
		double result = 0.0;
		for (V v : hds.getVertices()) {
			double[] pt = closestPointMap.get(v);
			double[] vpos = new double[3];
			FunctionalUtils.getPosition(v, x, vpos);
			result += Rn.euclideanNormSquared(Rn.subtract(null, vpos, pt));
		}
		return result;
	}

	public void evaluateGradient(
	// input
			HalfEdgeDataStructure<V, E, F> hds, DomainValue x,
			// output
			Gradient grad) {
		for (V v : hds.getVertices()) {
			double[] pt = closestPointMap.get(v);
			double[] vpos = new double[3];
			FunctionalUtils.getPosition(v, x, vpos);
			double[] v2pt = Rn.subtract(null, vpos, pt);
			Rn.times(v2pt, 2.0, v2pt);
			FunctionalUtils.addVectorToGradient(grad, 3 * v.getIndex(), v2pt);
		}
	}

	public void evaluateHessian(
	// input
			HalfEdgeDataStructure<V, E, F> G, DomainValue x,
			// output
			Hessian hess) {
	}

	public void setNURBSSurface(NURBSSurface ns) {
		this.refSurface = ns;

	}

}
