package de.varylab.varylab.functional;

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

public class ConstantDirectionFieldFunctional<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>> 
	implements Functional<V, E, F> {

	private double
		strength = 9.81E-3;

	private double[] 
	    dir = new double[] {0,0,1};
	
	public ConstantDirectionFieldFunctional(double s, double[] dir) {
		this.strength = s;
		Rn.times(this.dir, s, Rn.normalize(null, dir));
	}
	
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
		for(V v : hds.getVertices()) {
			double[] vv = new double[3];
			FunctionalUtils.getPosition(v, x, vv);
			result += Rn.innerProduct(dir,vv);
		}
		return result;
	}

	public void evaluateGradient(
		//input
			HalfEdgeDataStructure<V, E, F> G,
			DomainValue x,
		//output
			Gradient grad
	) {
		grad.setZero();
		for (V v : G.getVertices()) {
			FunctionalUtils.addVectorToGradient(grad,3*v.getIndex(),dir);
		}
	}

	public void evaluateHessian(
		// input
			HalfEdgeDataStructure<V, E, F> G, DomainValue x,
		// output
			Hessian hess) {
		hess.setZero();
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return 3*hds.numVertices();
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(
			HDS hds) {
		return new int[3*hds.numVertices()][3*hds.numVertices()];
	}

	@Override
	public boolean hasHessian() {
		return true;
	}

	public void setStrength(double s) {
		this.strength = s;
		Rn.times(this.dir, strength, Rn.normalize(null, dir));
	}

	public void setDirection(double[] dir) {
		Rn.times(this.dir, strength, Rn.normalize(null, dir));
	}
}
