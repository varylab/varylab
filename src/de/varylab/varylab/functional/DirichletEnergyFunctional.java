/**
 * 
 */
package de.varylab.varylab.functional;

import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;

/**
 * @author Thilo Roerig
 *
 */
public class DirichletEnergyFunctional <V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>> implements Functional<V, E, F> {

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> void evaluate(HDS hds,
			DomainValue x, Energy E, Gradient G, Hessian H) {
		
		if (E != null) {
			E.set(evaluate(hds, x));
		}
		if (G != null) {
			evaluateGradient(hds, x, G);
		}
		
	}

	public double evaluate(HalfEdgeDataStructure<V, E, F> hds, DomainValue x) {
		double result = 0.0;
		for (E e : hds.getPositiveEdges()) {
			result += 0.5*w(e,x)*getLengthSquared(e, x);
		}
		return result;
	}


	public void evaluateGradient(HalfEdgeDataStructure<V, E, F> hds, DomainValue x, Gradient G) {

		G.setZero();
		
		double[] sum = new double[3];
		
		List<V> boundaryV = new LinkedList<V>();
		for(V v : hds.getVertices()) {
			if(HalfEdgeUtils.isBoundaryVertex(v)) {
				boundaryV.add(v);
			}
		}
		
		// interior vertices
		for (V v : hds.getVertices()) {
			
			sum = new double[] {0.0,0.0,0.0};
			int off = v.getIndex() * 3;
			
			double[] vpos = FunctionalUtils.getPosition(v, x, null);
			
			for (E e : HalfEdgeUtils.incomingEdges(v)) {
				
				V w = e.getStartVertex();
				
				double weight = w(e, x);
				
				double[] wpos = FunctionalUtils.getPosition(w, x, null);
				
				Rn.add(sum, sum, Rn.times(null, weight, Rn.subtract(null, vpos, wpos)));
			}

			FunctionalUtils.addVectorToGradient(G, off, sum);
		}
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}
	
	public void getPosition(V v, DomainValue x, double[] pos) {
		pos[0] = x.get(v.getIndex() * 3 + 0);
		pos[1] = x.get(v.getIndex() * 3 + 1);
		pos[2] = x.get(v.getIndex() * 3 + 2);
	}
	
	private double cot(Double phi) {
		return 1.0 / StrictMath.tan(phi);
	}
	
	private double getLengthSquared(E e, DomainValue x) {
		double[] s = FunctionalUtils.getPosition(e.getStartVertex(), x, null);
		double[] t = FunctionalUtils.getPosition(e.getTargetVertex(), x, null);
		return Rn.euclideanDistanceSquared(s, t);
	}
	
	private double getAlpha(E e, DomainValue x){
		double a = getLengthSquared(e.getNextEdge(), x);
		double b = getLengthSquared(e.getPreviousEdge(), x);
		double c = getLengthSquared(e, x);
		return Math.acos((a + b - c) / (2.0 * Math.sqrt(a * b)));
	}
	

	private double w(E e, DomainValue x) {
		double val = 0.0;
		double w = 0.5;
		
		if(e.getLeftFace() == null) {
			val = 0.5*cot(getAlpha(e.getOppositeEdge(),x));
		} else if(e.getRightFace() == null) {
			val = 0.5*cot(getAlpha(e,x));
		} else { // interior edge
			double alpha_ij = getAlpha(e,x);
			double alpha_ji = getAlpha(e.getOppositeEdge(),x);

			// optimize
			val = w * (cot(alpha_ij) + cot(alpha_ji));
		}
		
		return val;
	}

	@Override
	public boolean hasGradient() {
		return true;
	}

}
