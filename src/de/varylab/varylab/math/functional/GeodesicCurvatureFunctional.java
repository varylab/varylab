package de.varylab.varylab.math.functional;

import static de.jreality.math.Rn.euclideanAngle;
import static de.jreality.math.Rn.euclideanNormSquared;
import static de.jreality.math.Rn.innerProduct;
import static de.jreality.math.Rn.subtract;
import static de.jreality.math.Rn.times;
import static de.jtem.halfedgetools.functional.FunctionalUtils.getPosition;
import static de.varylab.varylab.math.functional.OppositeEdgesCurvatureFunctional.findGeodesicPairs;
import static java.lang.Math.PI;

import java.util.Map;

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

public class GeodesicCurvatureFunctional <
	V extends Vertex<V, E, F>, 
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	private AdapterSet
		aSet = new AdapterSet();
	private double[]	
		vec1 = new double[3],
		vec2 = new double[3],
		vec3 = new double[3],
		vec4 = new double[3];		
	
	public void setAdapters(AdapterSet aSet) {
		this.aSet = aSet;
	}
	
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
		if (E != null) {
			E.set(evaluateEnergy(hds, x));
		}
		if (G != null) {
			
		}
	}
	
	public double evaluateEnergy(HalfEdgeDataStructure<V, E, F> hds, DomainValue x) {
		double result = 0.0;
		final double[] 
			vv = new double[3],
			vs = new double[3],
			vt = new double[3];
		for (V v : hds.getVertices()) {
			getPosition(v, x, vv);
			double[] n = aSet.getD(Normal.class, v);
			Map<E, E> geodesicPairs = findGeodesicPairs(v, false, aSet);
			double[] angles = new double[geodesicPairs.size()];
			int i = 0;
			for (E e : geodesicPairs.keySet()) {
				E ee = geodesicPairs.get(e);
				getPosition(e.getStartVertex(), x, vs);
				getPosition(ee.getStartVertex(), x, vt);
				subtract(vec1, vs, vv);
				subtract(vec2, vt, vv);
				subtract(vec1, vec1, times(vec3, innerProduct(n, vec1), n));
				subtract(vec2, vec2, times(vec4, innerProduct(n, vec2), n));
				angles[i++] = PI - euclideanAngle(vec1, vec2); 
			}
			result += euclideanNormSquared(angles);
		}
		return result;
	}
	
	
	public boolean hasHessian() {
		return false;
	}
	
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int getDimension(HDS hds) {
		return hds.getVertices().size() * 3;
	}
	
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int[][] getNonZeroPattern(HDS hds) {return null;}
	
}
