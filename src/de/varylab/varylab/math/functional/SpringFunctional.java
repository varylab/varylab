package de.varylab.varylab.math.functional;

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
import de.varylab.varylab.math.functional.EdgeLengthAdapters.Length;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.WeightFunction;

public class SpringFunctional<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>> 
	implements Functional<V, E, F> {

	private Length<E>
		length = null;
	private WeightFunction<E> 
		weight = null;
	
	private double power = 2.0;
	
	private boolean diagonals = false;
	
	public SpringFunctional(Length<E> l0, WeightFunction<E> w, boolean diagonals) {
		this.length = l0;
		this.weight = w;
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
		double[] s = new double[3];
		double[] t = new double[3];
		double result = 0.0;
		for (E e : hds.getPositiveEdges()) {
			FunctionalUtils.getPosition(e.getStartVertex(), x, s);
			FunctionalUtils.getPosition(e.getTargetVertex(), x, t);
			double el = Rn.euclideanDistance(s, t);
			result += weight.getWeight(e)*Math.pow(el-length.getTargetLength(e),power);
		}
		if(diagonals) {
			for(F f: hds.getFaces()) {
				List<V> vertices = HalfEdgeUtils.boundaryVertices(f);
				for(int i = 0; i < vertices.size(); ++i) {
					for(int j = 0; j < vertices.size(); ++j) {
						V vi = vertices.get(i),
						  vj = vertices.get(j);
						FunctionalUtils.getPosition(vi, x, s);
						FunctionalUtils.getPosition(vj, x, t);
						double el = Rn.euclideanDistance(s, t);
						result += Math.pow(el,power);
					}
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
			Gradient grad
	) {
		grad.setZero();
		double[] s = new double[3];
		double[] t = new double[3];
		double[] smt = new double[3];
		for (V v : hds.getVertices()) {
			FunctionalUtils.getPosition(v, x, s);
			for (E e : HalfEdgeUtils.incomingEdges(v)) {
				double tl = length.getTargetLength(e);
				FunctionalUtils.getPosition(e.getStartVertex(), x, t);
				Rn.subtract(smt, s, t);
				if(Rn.euclideanDistance(s,t)==0) {
					continue;
				}
				double factor = (1-tl/Rn.euclideanDistance(s, t));
				int off = v.getIndex() * 3;
				for (int d = 0; d < 3; d++) {
					grad.add(off + d, power*Math.pow((s[d] - t[d]),power-1.0) * factor * weight.getWeight(e));
				}
			}
		}
		if(diagonals) {
			for(F f: hds.getFaces()) {
				List<V> vertices = HalfEdgeUtils.boundaryVertices(f);
				for(int i = 0; i < vertices.size(); ++i) {
					for(int j = 0; j < vertices.size(); ++j) {
						V vi = vertices.get(i),
						  vj = vertices.get(j);
						FunctionalUtils.getPosition(vi, x, s);
						FunctionalUtils.getPosition(vj, x, t);
						Rn.subtract(smt, s, t);
						if(Rn.euclideanDistance(s,t)==0) {
							continue;
						}
						double factor = 1;
						int off = vi.getIndex() * 3;
						for (int d = 0; d < 3; d++) {
							grad.add(off + d, power*Math.pow((s[d] - t[d]),power-1.0) * factor);
						}
						off = vj.getIndex() * 3;
						for (int d = 0; d < 3; d++) {
							grad.add(off + d, -power*Math.pow((s[d] - t[d]),power-1.0) * factor);
						}
					}
				}
			}
		}
	}

	public void evaluateHessian(
		// input
			HalfEdgeDataStructure<V, E, F> G, DomainValue x,
		// output
			Hessian hess) {
		double[] 
		       vs = new double[3],
		       vt = new double[3],
		       smt = new double[3];
		for (V v : G.getVertices()) {
			FunctionalUtils.getPosition(v, x, vs);
			int ti = v.getIndex();
			for (E e : HalfEdgeUtils.incomingEdges(v)) {
				double tl = length.getTargetLength(e);
				V s = e.getStartVertex();
				int si = s.getIndex();
				FunctionalUtils.getPosition(s, x, vt);
				Rn.subtract(smt, vs, vt);
				double el = Rn.euclideanNorm(smt);
				if(el == 0) {
					continue;
				}
				double el2 = Rn.euclideanNormSquared(smt);
				double el3 = el2*el;
				double factor = -2*(1/el3)*weight.getWeight(e);
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						if(i==j) {
							hess.add(ti*3+i, si*3+i, 
									factor*(-tl*(el2-smt[i]*smt[i])+el3)
							);
							hess.add(ti*3+i,ti*3+j,-factor*(-tl*(el2-smt[i]*smt[i])+el3));
						} else {
							hess.add(ti*3+i,si*3+j,
									factor*(tl*smt[i]*smt[j])
							);
							hess.add(ti*3+i,ti*3+j,-factor*(tl*smt[i]*smt[j]));
						}
					}
				}
			}
			
		}
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return 3*hds.numVertices();
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(
			HDS hds) {
		int[][] nonZeroPattern = new int[3*hds.numVertices()][];
		for (V v : hds.getVertices()) {
			int ti = v.getIndex();
			List<V> neighs = HalfEdgeUtils.neighboringVertices(v);
			nonZeroPattern[3*ti] = new int[3*(neighs.size()+1)];
			nonZeroPattern[3*ti+1] = new int[3*(neighs.size()+1)];
			nonZeroPattern[3*ti+2] = new int[3*(neighs.size()+1)];

			for (int j = 0; j < 3; j++) {
				for (int j2 = 0; j2 < 3; j2++) {
					nonZeroPattern[3*ti+j][j2] = 3*ti+j2;						
				}
			}
			int i=1;
			for (V v2 : neighs) {
				int si = v2.getIndex();
				for (int j = 0; j < 3; j++) {
					for (int j2 = 0; j2 < 3; j2++) {
						nonZeroPattern[3*ti+j][3*i+j2] = 3*si+j2;						
					}
				}
				++i;
			}
		}
		return nonZeroPattern;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}

	public void setLength(Length<E> length) {
		this.length = length;
	}

	public void setWeight(WeightFunction<E> weight) {
		this.weight = weight;
	}

	public void setDiagonals(boolean diags) {
		diagonals = diags;
		
	}

}
