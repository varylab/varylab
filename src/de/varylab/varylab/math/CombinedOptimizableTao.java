package de.varylab.varylab.math;

import static de.varylab.jpetsc.InsertMode.INSERT_VALUES;
import static de.varylab.jtao.TaoAppAddHess.PreconditionerType.SAME_NONZERO_PATTERN;

import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.jpetsc.Mat;
import de.varylab.jpetsc.Vec;
import de.varylab.jtao.TaoAppAddCombinedObjectiveAndGrad;
import de.varylab.jtao.TaoAppAddHess;
import de.varylab.jtao.TaoApplication;
import de.varylab.varylab.hds.VHDS;

public class CombinedOptimizableTao extends TaoApplication implements
		TaoAppAddCombinedObjectiveAndGrad, TaoAppAddHess {

	private VHDS
		hds = null;
	private CombinedFunctional
		fun = null;
	private List<Constraint>
		constraints = new LinkedList<Constraint>();
		

	public CombinedOptimizableTao(VHDS hds, CombinedFunctional fun) {
		this.hds = hds;
		this.fun = fun;
	}
	
	
	public void addConstraint(Constraint c) {
		constraints.add(c);
	}
	
	public void removeConstraint(Constraint c) {
		constraints.remove(c);
	}


	public static class TaoU implements DomainValue {

		private Vec
			u = null;
		
		public TaoU(Vec u) {
			this.u = u;
		}

		@Override
		public void add(int i, double value) {
			u.add(i, value);
		}

		@Override
		public void set(int i, double value) {
			u.setValue(i, value, INSERT_VALUES);
		}

		@Override
		public void setZero() {
			u.zeroEntries();
		}

		@Override
		public double get(int i) {
			return u.getValue(i);
		}
		
	}
	
	
	private static class TaoGradient implements Gradient {

		private Vec
			G = null;
		
		public TaoGradient(Vec G) {
			this.G = G;
		}
		
		@Override
		public void add(int i, double value) {
			G.add(i, value);
		}

		@Override
		public void set(int i, double value) {
			G.setValue(i, value, INSERT_VALUES);
		}
		
		@Override
		public void setZero() {
			G.zeroEntries();
		}

		@Override
		public double get(int i) {
			return G.getValue(i);
		}
		
	}
	
	
	private static class TaoHessian implements Hessian {
		
		private Mat
			H = null;
		
		public TaoHessian(Mat H) {
			this.H = H;
		}

		@Override
		public void add(int i, int j, double value) {
			H.add(i, j, value);
		}

		@Override
		public void setZero() {
			H.zeroEntries();
		}

		@Override
		public void set(int i, int j, double value) {
			H.setValue(i, j, value, INSERT_VALUES);
		}
		
		@Override
		public double get(int i, int j) {
			return H.getValue(i, j);
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
	public double evaluateObjectiveAndGradient(Vec x, Vec g) {
		TaoU u = new TaoU(x);
		TaoGradient G = new TaoGradient(g);
		SimpleEnergy E = new SimpleEnergy();
		fun.evaluate(hds, u, E, G, null);
		applyConstraints(u, G, null);
		g.assemble();
		return E.get();
	}

	@Override
	public PreconditionerType evaluateHessian(Vec x, Mat H, Mat Hpre) {
		TaoU u = new TaoU(x);
		TaoHessian taoHess = new TaoHessian(H);
		fun.evaluate(hds, u, null, null, taoHess);
		applyConstraints(u, null, taoHess);
		H.assemble();
		return SAME_NONZERO_PATTERN;
	}

	
	public int getDomainDimension() {
		return hds.numVertices() * 3;
	}
	
	
}
