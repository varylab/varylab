package de.varylab.varylab.optimization;

import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariablesWithGradient;
import de.jtem.numericalMethods.util.Arrays;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.optimization.constraint.Constraint;
import de.varylab.varylab.optimization.tao.TaoEnergy;

public class CombinedOptimizableNM implements RealFunctionOfSeveralVariablesWithGradient {

	private VHDS
		hds = null;
	private CombinedFunctional
		fun = null;
	private List<Constraint>
		constraints = new LinkedList<Constraint>();

	public CombinedOptimizableNM(VHDS hds, CombinedFunctional fun) {
		this.hds = hds;
		this.fun = fun;
	}
	
	public void addConstraint(Constraint c) {
		constraints.add(c);
	}
	
	public void removeConstraint(Constraint c) {
		constraints.remove(c);
	}
	
	public class ArrayValue implements DomainValue, Gradient {

		private double[]
		    arr = null;
		
		public ArrayValue(double[] arr) {
			this.arr = arr;
		}
		
		@Override
		public void add(int i, double value) {
			arr[i] += value;
		}

		@Override
		public double get(int i) {
			return arr[i];
		}

		@Override
		public void set(int i, double value) {
			arr[i] = value;
		}

		@Override
		public void setZero() {
			Arrays.fill(arr, 0.0);
		}
		
	}
	
	
	private void applyConstraints(DomainValue x, Gradient G, Hessian H) {
		for (Constraint c : constraints) {
			if (G != null) {
				c.editGradient(hds, getNumberOfVariables(), x, G);
			}
			if (H != null) {
				c.editHessian(hds, getNumberOfVariables(), x, H);
			}
		}
	}
	
	@Override
	public double eval(double[] X, double[] G) {
		ArrayValue xArr = new ArrayValue(X);
		ArrayValue gArr = new ArrayValue(G);
		TaoEnergy E = new TaoEnergy();
		fun.evaluate(hds, xArr, E, gArr, null);
		applyConstraints(xArr, gArr, null);
		return E.get();
	}

	@Override
	public double eval(double[] X) {
		ArrayValue xArr = new ArrayValue(X);
		TaoEnergy E = new TaoEnergy();
		fun.evaluate(hds, xArr, E, null, null);
		return E.get();
	}

	@Override
	public int getNumberOfVariables() {
		return fun.getDimension(hds);
	}

}
