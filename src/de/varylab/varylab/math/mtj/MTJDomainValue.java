package de.varylab.varylab.math.mtj;

import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.functional.DomainValue;

public class MTJDomainValue implements DomainValue {

	protected Vector
		x = null;
	
	public MTJDomainValue(Vector G) {
		this.x = G;
	}
	
	@Override
	public void add(int i, double value) {
		x.add(i, value);
	}

	@Override
	public void set(int i, double value) {
		x.set(i, value);
	}
	
	@Override
	public void setZero() {
		x.zero();
	}
	
	@Override
	public double get(int i) {
		return x.get(i);
	}
	
}