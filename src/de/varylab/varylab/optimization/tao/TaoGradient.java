/**
 * 
 */
package de.varylab.varylab.optimization.tao;

import static de.jtem.jpetsc.InsertMode.ADD_VALUES;
import static de.jtem.jpetsc.InsertMode.INSERT_VALUES;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.jpetsc.Vec;

public class TaoGradient implements Gradient {

	private Vec
		G = null;
	
	public TaoGradient(Vec G) {
		this.G = G;
	}

	@Override
	public double get(int i) {
		return G.getValue(i);
	}
	@Override
	public void add(int i, double value) {
		G.setValue(i, value, ADD_VALUES);
	}
	@Override
	public void add(double alpha, Gradient g) {
		G.aXPY(alpha, ((TaoGradient)g).getVec());
	}
	@Override
	public void set(int i, double value) {
		G.setValue(i, value, INSERT_VALUES);
	}
	@Override
	public void setZero() {
		G.zeroEntries();
	}

	public Vec getVec() {
		return G;
	}
	
}