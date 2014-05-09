package de.varylab.varylab.functional;

import static de.jreality.math.Rn.innerProduct;
import static de.jreality.math.Rn.subtract;
import static de.jreality.math.Rn.times;
import static de.jtem.halfedgetools.functional.FunctionalUtils.getPosition;

import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.utilities.GeodesicUtility;

public class GeodesicCurvatureFunctional <
	V extends Vertex<V, E, F>, 
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	private AdapterSet
		aSet = new AdapterSet();
	private double[]	
		T1 = new double[3],
		T2 = new double[3],
		vec1 = new double[3],
		vec2 = new double[3],
		vv = new double[3],
		vs = new double[3],
		vt = new double[3];
	
	public void setAdapters(AdapterSet aSet) {
		this.aSet = aSet;
	}
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
		if (E != null) E.setZero();
		if (G != null) G.setZero();
		for (V v : hds.getVertices()) {
			getPosition(v, x, vv);
			double[] n = aSet.getD(Normal.class, v);
			Map<E, E> geodesicPairs = GeodesicUtility.findGeodesicPairs(v, false, false, aSet);
			for (E e : geodesicPairs.keySet()) {
				E ee = geodesicPairs.get(e);
				getPosition(e.getStartVertex(), x, vs);
				getPosition(ee.getStartVertex(), x, vt);
				subtract(T1, vs, vv);
				subtract(T2, vt, vv);
				subtract(T1, T1, times(vec1, innerProduct(n, T1), n));
				subtract(T2, T2, times(vec2, innerProduct(n, T2), n));
				double dot2 = Rn.innerProduct(T1, T2);
				dot2 *= dot2;
				double T1T1 = Rn.euclideanNormSquared(T1);
				double T2T2 = Rn.euclideanNormSquared(T2);
				if (E != null) {
					double dif = dot2 - T1T1*T2T2;
					E.add(dif * dif);
				}
				if (G != null) {
					
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
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int getDimension(HDS hds) {
		return hds.getVertices().size() * 3;
	}
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int[][] getNonZeroPattern(HDS hds) {return null;}

	@Override
	public boolean hasGradient() {
		return true;
	}
}
