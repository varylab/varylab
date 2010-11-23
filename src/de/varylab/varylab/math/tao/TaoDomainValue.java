/**
 * 
 */
package de.varylab.varylab.math.tao;

import static de.varylab.jpetsc.InsertMode.INSERT_VALUES;
import de.jtem.halfedgetools.functional.DomainValue;
import de.varylab.jpetsc.Vec;

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