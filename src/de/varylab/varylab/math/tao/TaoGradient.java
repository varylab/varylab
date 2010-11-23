/**
 * 
 */
package de.varylab.varylab.math.tao;

import static de.varylab.jpetsc.InsertMode.INSERT_VALUES;
import de.jtem.halfedgetools.functional.Gradient;
import de.varylab.jpetsc.Vec;

public class TaoGradient implements Gradient {

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