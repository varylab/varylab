/**
 * 
 */
package de.varylab.varylab.optimization.tao;

import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.jpetsc.InsertMode;
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
		G.setValue(i, value, InsertMode.ADD_VALUES);
	}
	public void add(double alpha, TaoGradient g) {
		G.aXPY(alpha, g.getVec());
	}
	@Override
	public void set(int i, double value) {
		G.setValue(i, value, InsertMode.INSERT_VALUES);
	}
	@Override
	public void setZero() {
		G.zeroEntries();
	}

	public Vec getVec() {
		return G;
	}
	
}