package de.varylab.varylab.hds.calculator;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.calculator.EdgeAverageCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

public class VSubdivisionCalculator implements EdgeAverageCalculator, FaceBarycenterCalculator {

	private double 
		alpha = 0.5;
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return VFace.class == nodeClass || VEdge.class == nodeClass;
	}
	
	@Override
	public double getPriority() {
		return 1;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(F f) {
		E e = f.getBoundaryEdge();
		double[] barycenter = new double[3];
		Rn.add(barycenter, barycenter, ((VVertex)e.getStartVertex()).position);
		int nVerts = 1;
		E e2 = e.getNextEdge();
		while(e2 != e) {
			Rn.add(barycenter, barycenter, ((VVertex)e2.getStartVertex()).position);
			e2 = e2.getNextEdge();
			nVerts++;
		}
		Rn.times(barycenter, 1.0 / nVerts, barycenter);	
		return barycenter;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(E e) {
		VEdge ve = (VEdge)e;
		double[] s = ve.getStartVertex().position;
		double[] t = ve.getTargetVertex().position;
		return Rn.linearCombination(null, alpha, t, 1 - alpha, s);
	}

	@Override
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	@Override
	public void setIgnore(boolean ignore) {
	}

}
