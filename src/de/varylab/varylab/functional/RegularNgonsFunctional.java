package de.varylab.varylab.functional;

import java.util.HashSet;
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

public class RegularNgonsFunctional<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>> 
	implements Functional<V, E, F> {

	private HashSet<Integer> 
		sizesList = new HashSet<Integer>();
	
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
		double[]
		       vi = new double[3],
		       vj = new double[3],
		       vk = new double[3];
		for (F f : hds.getFaces()) {
			List<E> boundaryEdges = HalfEdgeUtils.boundaryEdges(f);
			E e = boundaryEdges.get(0);
			int size = boundaryEdges.size();
			if(!sizesList.contains(size)) {
				continue;
			} else {
				double targetAngle = (size-2)*Math.PI/size;
				for(int i = 0; i < size; ++i) {
					FunctionalUtils.getPosition(e.getPreviousEdge().getStartVertex(), x, vi);
					FunctionalUtils.getPosition(e.getStartVertex(), x, vj);
					FunctionalUtils.getPosition(e.getTargetVertex(), x, vk);
					double angle = FunctionalUtils.angle(vi, vj, vk);
					result += (angle-targetAngle)*(angle-targetAngle);
					e = e.getNextEdge();
				}
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
		       vi = new double[3],
		       vj = new double[3],
		       vk = new double[3],
		       di = new double[3],
		       dj = new double[3],
		       dk = new double[3];
		G.setZero();
		for (F f : hds.getFaces()) {
			List<E> boundaryEdges = HalfEdgeUtils.boundaryEdges(f);
			E e = boundaryEdges.get(0);
			int size = boundaryEdges.size();
			if(!sizesList.contains(size)) {
				continue;
			} else {
				double targetAngle = (size-2)*Math.PI/size;
				for(int i = 0; i < size; ++i) {
					int 
						iind = e.getPreviousEdge().getStartVertex().getIndex(),
						jind = e.getStartVertex().getIndex(),
						kind = e.getTargetVertex().getIndex();

					FunctionalUtils.getPosition(e.getPreviousEdge().getStartVertex(), x, vi);
					FunctionalUtils.getPosition(e.getStartVertex(), x, vj);
					FunctionalUtils.getPosition(e.getTargetVertex(), x, vk);
					double angle = FunctionalUtils.angle(vi, vj, vk);
					double scale = 2*(angle-targetAngle);

					FunctionalUtils.angleGradient(vi,vj,vk,di,dj,dk);
					Rn.times(di,scale,di);
					Rn.times(dj,scale,dj);
					Rn.times(dk,scale,dk);
					
					FunctionalUtils.addVectorToGradient(G, 3*iind, di);
					FunctionalUtils.addVectorToGradient(G, 3*jind, dj);
					FunctionalUtils.addVectorToGradient(G, 3*kind, dk);	
					e = e.getNextEdge();
				}
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

	public void setSizes(int i) {
		sizesList.clear();
		sizesList.add(i);
	}


	@Override
	public boolean hasGradient() {
		return true;
	}
}
