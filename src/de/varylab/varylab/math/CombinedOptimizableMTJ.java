package de.varylab.varylab.math;

import java.util.LinkedList;
import java.util.List;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.mtjoptimization.Optimizable;
import de.varylab.varylab.hds.VHDS;

public class CombinedOptimizableMTJ implements Optimizable {

	private VHDS
		hds = null;
	private CombinedFunctional
		fun = null;
	private List<Constraint>
		constraints = new LinkedList<Constraint>();

	public CombinedOptimizableMTJ(VHDS hds, CombinedFunctional fun) {
		this.hds = hds;
		this.fun = fun;
	}
	
	public void addConstraint(Constraint c) {
		constraints.add(c);
	}
	
	public void removeConstraint(Constraint c) {
		constraints.remove(c);
	}
	
	
	protected static class MTJU implements DomainValue {

		protected Vector
			u = null;
		
		public MTJU(Vector u) {
			this.u = u;
		}
		
		@Override
		public void add(int i, double value) {
			u.add(i, value);
		}

		@Override
		public void set(int i, double value) {
			u.set(i, value);
		}

		@Override
		public void setZero() {
			u.zero();
		}
		
		@Override
		public double get(int i) {
			return u.get(i);
		}
		
	}
	
	
	protected static class MTJGradient implements Gradient {

		protected Vector
			G = null;
		
		public MTJGradient(Vector G) {
			this.G = G;
		}
		
		@Override
		public void add(int i, double value) {
			G.add(i, value);
		}

		@Override
		public void set(int i, double value) {
			G.set(i, value);
		}
		
		@Override
		public void setZero() {
			G.zero();
		}
		
		@Override
		public double get(int i) {
			return G.get(i);
		}
		
	}
	
	
	protected static class MTJHessian implements Hessian {
		
		protected Matrix
			H = null;
		
		public MTJHessian(Matrix H) {
			this.H = H;
		}

		@Override
		public void add(int i, int j, double value) {
			H.add(i, j, value);
		}

		@Override
		public void set(int i, int j, double value) {
			H.set(i, j, value);
		}
		
		@Override
		public void setZero() {
			H.zero();
		}
		
		@Override
		public double get(int i, int j) {
			return H.get(i, j);
		}
		
	}
	
	
	private void applyConstraints(DomainValue x, Gradient G, Hessian H) {
		for (Constraint c : constraints) {
			if (G != null) {
				c.editGradient(hds, getDomainDimension(), x, G);
			}
			if (H != null) {
				c.editHessian(hds, getDomainDimension(), x, H);
			}
		}
	}
	
	
	@Override
	public Double evaluate(Vector x, Vector gradient, Matrix hessian) {
		MTJU u = new MTJU(x);
		MTJGradient G = new MTJGradient(gradient);
		MTJHessian H = new MTJHessian(hessian);
		SimpleEnergy E = new SimpleEnergy();
		fun.evaluate(hds, u, E, G, H);
		applyConstraints(u, G, H);
		return E.get();
	}

	@Override
	public Double evaluate(Vector x, Vector gradient) {
		MTJU u = new MTJU(x);
		MTJGradient G = new MTJGradient(gradient);
		SimpleEnergy E = new SimpleEnergy();
		fun.evaluate(hds, u, E, G, null);
		applyConstraints(u, G, null);
		return E.get();
	}

	@Override
	public Double evaluate(Vector x, Matrix hessian) {
		MTJU u = new MTJU(x);
		MTJHessian H = new MTJHessian(hessian);
		SimpleEnergy E = new SimpleEnergy();
		fun.evaluate(hds, u, E, null, H);
		applyConstraints(u, null, H);
		return E.get();
	}

	@Override
	public Double evaluate(Vector x) {
		MTJU u = new MTJU(x);
		SimpleEnergy E = new SimpleEnergy();
		fun.evaluate(hds, u, E, null, null);
		return E.get();
	}

	@Override
	public Integer getDomainDimension() {
		return fun.getDimension(hds);
	}

	@Override
	public Matrix getHessianTemplate() {
		int dim = getDomainDimension();
		return new CompRowMatrix(dim, dim, SparseUtility.makeNonZeros(hds));
	}

}
