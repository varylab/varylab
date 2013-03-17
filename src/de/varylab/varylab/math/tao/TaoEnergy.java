package de.varylab.varylab.math.tao;

import de.jtem.halfedgetools.functional.Energy;

public class TaoEnergy implements Energy {

	private double 
		E = 0.0;
	
	@Override
	public double get() {
		return E;
	}
	
	@Override
	public void add(double E) {
		this.E += E;
	}

	@Override
	public void set(double E) {
		this.E = E;
	}

	@Override
	public void setZero() {
		E = 0.0;
	}
	
}