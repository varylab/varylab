package de.varylab.varylab.functional;

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
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public class ConicalFunctional<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>> 
	implements Functional<V, E, F> {

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
		for (V v : hds.getVertices()) {
			if(!HalfEdgeUtils.isBoundaryVertex(v)) {
				FunctionalUtils.getPosition(v, x, vj);
				List<V> star = HalfEdgeUtilsExtra.getVertexStar(v);
				int nn = star.size();
				if(nn == 4) { // vertex has an even number of neighbors!
					E e = v.getIncomingEdge();
					
					double vertexEnergy = 0.0;
					FunctionalUtils.getPosition(v, x, vj);
					double sign = -1.0;
					do {
						FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
						FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);
						double angle = FunctionalUtils.angle(vi, vj, vk);
						vertexEnergy += sign*angle;
						sign *= -1.0;
						e = e.getNextEdge().getOppositeEdge();
					} while(e != v.getIncomingEdge());
					result += vertexEnergy*vertexEnergy;
				} else { // non degree 4 vertices
					
				}	
			} else { // boundary vertex.
				
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
			FunctionalUtils.getPosition(v,x,vv);
			if(!HalfEdgeUtils.isBoundaryVertex(v)) {
				FunctionalUtils.getPosition(v, x, vv);
				List<V> star = HalfEdgeUtilsExtra.getVertexStar(v);
				int nn = star.size();
				if(nn == 4) { // vertex has an even number of neighbors!
					double vertexEnergy = 0.0;
					FunctionalUtils.getPosition(v, x, vv);
					E e = v.getIncomingEdge();
					double sign = -1.0;
					do {
						FunctionalUtils.getPosition(e.getStartVertex(), x, vs);
						FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vt);
						double angle = FunctionalUtils.angle(vs, vv, vt);
						vertexEnergy += sign*angle;
						sign *= -1.0;
						e = e.getNextEdge().getOppositeEdge();
					} while(e != v.getIncomingEdge());
					sign = -1.0;
					do {
						int 
							sind = e.getStartVertex().getIndex(),
							vind = e.getTargetVertex().getIndex(),
							tind = e.getNextEdge().getTargetVertex().getIndex();
						
						FunctionalUtils.getPosition(e.getStartVertex(), x, vs);
						FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vt);
						FunctionalUtils.angleGradient(vs,vv,vt,ds,dv,dt);
						double scale = sign*2.0*vertexEnergy;
						sign *= -1.0;
						Rn.times(ds,scale,ds);
						Rn.times(dv,scale,dv);
						Rn.times(dt,scale,dt);
						
						FunctionalUtils.addVectorToGradient(G, 3*sind, ds);
						FunctionalUtils.addVectorToGradient(G, 3*vind, dv);
						FunctionalUtils.addVectorToGradient(G, 3*tind, dt);						
						e = e.getNextEdge().getOppositeEdge();
					} while(e != v.getIncomingEdge());
				} else { // non degree 4 vertices
					
				}	
			} else { // boundary vertex.
				
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


}
