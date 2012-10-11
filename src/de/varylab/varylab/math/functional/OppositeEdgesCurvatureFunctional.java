package de.varylab.varylab.math.functional;

import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.math.GeodesicUtility;

public class OppositeEdgesCurvatureFunctional<
	V extends Vertex<V, E, F>, 
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	
	private AdapterSet
		adapters = new AdapterSet();
	
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>>
	void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
		if (E != null) {
			E.set(evaluate(hds, x));
		}
		if (G != null) {
			evaluateGradient(hds, x, G);
		}
		if (H != null) {
			evaluateHessian(hds, x, H);
		}
	}

	
	
	// Calculate the energy of a given configuration
	public double evaluate(HalfEdgeDataStructure<V, E, F> hds, DomainValue x) {
		double result = 0.0;
		final double[] 
		       vv = new double[3],
		       vs = new double[3],
		       vt = new double[3];
		for (V v : hds.getVertices()) {
			FunctionalUtils.getPosition(v, x, vv);
			Map<E, E> geodesicPairs = GeodesicUtility.findGeodesicPairs(v, false, false, adapters);

			double[] angles = new double[geodesicPairs.size()];
			int i = 0;
			for (E e : geodesicPairs.keySet()) {
				E ee = geodesicPairs.get(e);
				FunctionalUtils.getPosition(e.getStartVertex(), x, vs);
				FunctionalUtils.getPosition(ee.getStartVertex(), x, vt);
				angles[i++] = Math.PI-FunctionalUtils.angle(vs,vv,vt); 
			}
			result += Rn.euclideanNormSquared(angles);
		}
		return result;
	}

	public void evaluateGradient(
			//input
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
			//output
			Gradient G
	) {
		double[] 
		       vv = new double[3],
		       vs = new double[3],
		       vt = new double[3],
		       ds = new double[3],
		       dv = new double[3],
		       dt = new double[3];
		G.setZero();
		for (V v : hds.getVertices()) {
				FunctionalUtils.getPosition(v, x, vv);
				int vi = v.getIndex();
				Map<E, E> geodesicPairs = GeodesicUtility.findGeodesicPairs(v, false, false, adapters);
				double[] angles = new double[geodesicPairs.size()];
				int i = 0;
				for (E e : geodesicPairs.keySet()) {
					E ee = geodesicPairs.get(e);
					FunctionalUtils.getPosition(e.getStartVertex(), x, vs);
					FunctionalUtils.getPosition(ee.getStartVertex(), x, vt);
					angles[i++] = Math.PI-FunctionalUtils.angle(vs,vv,vt); 
				}
				i = 0;
				for (E e : geodesicPairs.keySet()) {
					E ee = geodesicPairs.get(e);
					V s = e.getStartVertex();
					V t = ee.getStartVertex();
					int	si = s.getIndex();
					int ti = t.getIndex();

					FunctionalUtils.getPosition(s, x, vs);
					FunctionalUtils.getPosition(t, x, vt);
					FunctionalUtils.angleGradient(vs, vv, vt, ds, dv, dt);
					double scale = -2.0*angles[i++];
					Rn.times(ds,scale, ds);
					Rn.times(dv,scale, dv);
					Rn.times(dt,scale, dt);

					FunctionalUtils.addVectorToGradient(G, 3*si, ds);
					FunctionalUtils.addVectorToGradient(G, 3*vi, dv);
					FunctionalUtils.addVectorToGradient(G, 3*ti, dt);
				}
		}
	}

	public void evaluateHessian(
			// input
			HalfEdgeDataStructure<V, E, F> G, DomainValue x,
			// output
			Hessian hess) {
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return 3*hds.numVertices();
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}

	public void setAdapters(AdapterSet adapters) {
		this.adapters = adapters;
	}

}
