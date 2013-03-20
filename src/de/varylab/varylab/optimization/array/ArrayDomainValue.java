package de.varylab.varylab.optimization.array;

import java.util.Arrays;

import de.jtem.halfedgetools.functional.DomainValue;

public class ArrayDomainValue implements DomainValue {

	private double[]
		vec = null;
	
	public ArrayDomainValue(double[] vec) {
		this.vec = vec;
	}
	
	@Override
	public double get(int i) {
		return vec[i];
	}

	@Override
	public void set(int i, double value) {
		vec[i] = value;
	}

	@Override
	public void add(int i, double value) {
		vec[i] += value;
	}

	@Override
	public void setZero() {
		Arrays.fill(vec, 0.0);
	}
	
	public double[] getArray() {
		return vec;
	}


}
