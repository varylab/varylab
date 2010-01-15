package de.varylab.varylab.math;

import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariablesWithGradient;
import de.jtem.numericalMethods.util.Arrays;
import de.varylab.varylab.hds.VHDS;

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
	
	
	private void applyConstraints(Gradient G, Hessian H) {
		for (Constraint c : constraints) {
			if (G != null) {
				c.editGradient(hds, getNumberOfVariables(), G);
			}
			if (H != null) {
				c.editHessian(hds, getNumberOfVariables(), H);
			}
		}
	}
	
	@Override
	public double eval(double[] X, double[] G) {
		ArrayValue xArr = new ArrayValue(X);
		ArrayValue gArr = new ArrayValue(G);
		SimpleEnergy E = new SimpleEnergy();
		fun.evaluate(hds, xArr, E, gArr, null);
		applyConstraints(gArr, null);
		return E.E;
	}

	@Override
	public double eval(double[] X) {
		ArrayValue xArr = new ArrayValue(X);
		SimpleEnergy E = new SimpleEnergy();
		fun.evaluate(hds, xArr, E, null, null);
		return E.E;
	}

	@Override
	public int getNumberOfVariables() {
		return fun.getDimension(hds);
	}

}
