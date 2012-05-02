package de.varylab.varylab.plugin.datasource;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.CurvatureFieldMin;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public class IsothermicityMeasure extends Plugin {


	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface CurvatureMinAngle {}
	
	@CurvatureMinAngle
	protected class CurvatureAngleAdapter extends AbstractAdapter<Double> {

		public CurvatureAngleAdapter() {
			super(Double.class, true, false);
		}
		
		public <
			V extends Vertex<V,E,F>, 
			E extends Edge<V,E,F>, 
			F extends Face<V,E,F>
		> Double getE(E e, AdapterSet a) {
			double[] N = a.getD(Normal.class, e);
			double[] Kmin = a.getD(CurvatureFieldMin.class, e);
			double[] E = a.getD(EdgeVector.class, e);
			return getSignedAngle(N, Kmin, E);
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Edge.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public String toString() {
			return "Principle Curvature Angle";
		}
		
	}
	
	
	protected class IsothermicityMeasureAdapter extends AbstractAdapter<Double> {
		
		public IsothermicityMeasureAdapter() {
			super(Double.class, true, false);
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getV(V v, AdapterSet a) {
			double s = 1;
			if (HalfEdgeUtils.isBoundaryVertex(v)) {
				return s;
			}
			for (E e : HalfEdgeUtils.incomingEdges(v)) {
				double alphaE = a.get(CurvatureMinAngle.class, e, Double.class);
				double alphaEPrev = a.get(CurvatureMinAngle.class, e.getPreviousEdge(), Double.class);
				double alphaENext = a.get(CurvatureMinAngle.class, e.getNextEdge(), Double.class);
				double sl = calculateTriangleAngle(alphaE, alphaENext, alphaEPrev);
				double sr = calculateTriangleAngle(alphaENext, alphaEPrev, alphaE);
				s *= sl/sr;
//				double k1 = a.get(PrincipalCurvatureMax.class, e, Double.class);
//				double k2 = a.get(PrincipalCurvatureMin.class, e, Double.class);
//				s *= (k1 - k2)*(k1 - k2);
			}
			return s;
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Vertex.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public String toString() {
			return "Isothermicity";
		}
		
	}
	
	/**
	 * Calculate the angle between the edges that belong to alpha1 and alpha2.
	 * @param alpha1
	 * @param alpha2
	 * @param alpha3
	 * @return
	 */
	protected double calculateTriangleAngle(double alpha1, double alpha2, double alpha3) {
		alpha1 = normalizeAngle(alpha1);
		alpha2 = normalizeAngle(alpha2);
		alpha3 = normalizeAngle(alpha3);
		double beta = abs(alpha2 - alpha1);
		if ((alpha3 > alpha2 && alpha3 > alpha1) || (alpha3 < alpha2 && alpha3 < alpha1)) {
			return beta;
		} else {
			return PI - beta;
		}
	}
	
	protected double normalizeAngle(double a) {
		a %= 2*PI;
		if (a > PI/2) {
			return a - PI;
		} else if (a < PI/2) {
			return PI + a;
		} else {
			return a;
		}
	}
	
	
	/**
	 * Returns the angle between v1 and v2 in the range ]-pi/2, pi/2]. 
	 * Where the sign is the sign of the determinant |N v1 v2|. 
	 * @param v1
	 * @param v2
	 * @param N
	 * @return
	 */
	protected double getSignedAngle(double[] N, double[] v1, double[] v2) {
		double[][] T = {N, v1, v2};
		double sign = Math.signum(Rn.determinant(T));
		double alpha = Rn.euclideanAngle(v1, v2);
		if (alpha > PI/2) {
			alpha = -(PI - alpha);
		}
		return sign * alpha;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		c.getPlugin(HalfedgeInterface.class).addAdapter(new CurvatureAngleAdapter(), true);
		c.getPlugin(HalfedgeInterface.class).addAdapter(new IsothermicityMeasureAdapter(), true);
	}
	
}
