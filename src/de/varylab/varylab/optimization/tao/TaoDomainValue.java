/**
 * 
 */
package de.varylab.varylab.optimization.tao;

import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.jpetsc.InsertMode;
import de.jtem.jpetsc.Vec;

public class TaoDomainValue implements DomainValue {

	private Vec
		u = null;
	
	public TaoDomainValue(Vec u) {
		this.u = u;
	}

	@Override
	public void add(int i, double value) {
		u.add(i, value);
	}

	@Override
	public void set(int i, double value) {
		u.setValue(i, value, InsertMode.INSERT_VALUES);
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