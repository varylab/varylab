package de.varylab.varylab.functional;

import static de.jtem.halfedgetools.functional.FunctionalUtils.getPosition;

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
import de.varylab.varylab.utilities.GeodesicUtility;

public class CircumcircleCurvatureFunctional<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>> 
	implements Functional<V, E, F> {

	private AdapterSet adapters = new AdapterSet();

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
	}

	// Calculate the energy of a given configuration
	public double evaluate(HalfEdgeDataStructure<V, E, F> hds, DomainValue x) {
		double result = 0.0;
		final double[] 
		       vv = new double[3],
		       vs = new double[3],
		       vt = new double[3];
		for (V v : hds.getVertices()) {
			getPosition(v, x, vv);
			Map<E, E> geodesicPairs = GeodesicUtility.findGeodesicPairs(v, false, false, adapters);
//			if (geodesicPairs.isEmpty() || HalfEdgeUtils.isBoundaryVertex(v)) { //boundary??
//				continue;
//			}
			for(E e : geodesicPairs.keySet()) {
				getPosition(e.getStartVertex(), x, vs);
				getPosition(geodesicPairs.get(e).getStartVertex(), x, vt);
				result += GeodesicUtility.circumcircleCurvatureSquared(vs,vv,vt);
			}
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
			int vi = v.getIndex();
			getPosition(v, x, vv);
			Map<E, E> geodesicPairs = GeodesicUtility.findGeodesicPairs(v, false, false, adapters);
//			if (geodesicPairs.isEmpty() || HalfEdgeUtils.isBoundaryVertex(v)) { //boundary??
//				continue;
//			}
			for(E e : geodesicPairs.keySet()) {
				E ee = geodesicPairs.get(e);
				V s = e.getStartVertex();
				V t = ee.getStartVertex();
				int	si = s.getIndex();
				int ti = t.getIndex();
				getPosition(s, x, vs);
				getPosition(t, x, vt);
				double[] v2s = Rn.subtract(null, vs, vv);
				double[] v2t = Rn.subtract(null, vt, vv);
				double[] s2t = Rn.subtract(null, vt, vs);
				double v2s2 = Rn.euclideanNormSquared(v2s);
				double v2t2 = Rn.euclideanNormSquared(v2t);
				double s2t2 = Rn.euclideanNormSquared(s2t);
				double innerProduct = Rn.innerProduct(v2s, v2t);
				double numerator = 4.0*(v2s2*v2t2-innerProduct*innerProduct);
				double denominator = v2s2*v2t2*s2t2;
				Rn.times(ds, 1.0/(denominator*denominator),
					Rn.subtract(null, 
						Rn.times(null, 8.0*denominator, Rn.subtract(null, Rn.times(null,v2t2,v2s),Rn.times(null,innerProduct,v2t))),
						Rn.times(null, numerator*2.0*v2t2,Rn.subtract(null, Rn.times(null, s2t2, v2s), Rn.times(null, v2s2, s2t) ))));
				Rn.times(dv, 1.0/(denominator*denominator),
						Rn.add(null, 
							Rn.times(null, 8.0*denominator, Rn.add(null, Rn.times(null,innerProduct-v2s2,v2t), Rn.times(null,innerProduct-v2t2,v2s))),
							Rn.times(null, numerator*2*s2t2, Rn.add(null, Rn.times(null, v2s2, v2t), Rn.times(null, v2t2, v2s)))));
				Rn.times(dt, 1.0/(denominator*denominator),
						Rn.subtract(null, 
							Rn.times(null, 8.0*denominator, Rn.subtract(null, Rn.times(null,v2s2,v2t),Rn.times(null,innerProduct,v2s))),
							Rn.times(null, numerator*2*v2s2,Rn.add(null, Rn.times(null, s2t2, v2t), Rn.times(null, v2t2, s2t)))));
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
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(
			HDS hds) {
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
