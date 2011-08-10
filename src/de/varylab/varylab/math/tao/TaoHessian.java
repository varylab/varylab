/**
 * 
 */
package de.varylab.varylab.math.tao;

import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.jpetsc.InsertMode;
import de.jtem.jpetsc.Mat;

public class TaoHessian implements Hessian {
	
	private Mat
		H = null;
	
	public TaoHessian(Mat H) {
		this.H = H;
	}

	@Override
	public void add(int i, int j, double value) {
		H.add(i, j, value);
	}

	@Override
	public void setZero() {
		H.zeroEntries();
	}

	@Override
	public void set(int i, int j, double value) {
		H.setValue(i, j, value, InsertMode.INSERT_VALUES);
	}
	
	@Override
	public double get(int i, int j) {
		return H.getValue(i, j);
	}
	
}