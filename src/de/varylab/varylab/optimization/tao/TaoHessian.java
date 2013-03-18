/**
 * 
 */
package de.varylab.varylab.optimization.tao;

import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.jpetsc.InsertMode;
import de.jtem.jpetsc.Mat;
import de.jtem.jpetsc.MatStructure;

public class TaoHessian implements Hessian {
	
	private Mat
		H = null;
	
	public TaoHessian(Mat H) {
		this.H = H;
	}

	@Override
	public double get(int i, int j) {
		return H.getValue(i, j);
	}
	@Override
	public void add(int i, int j, double value) {
		H.setValue(i, j, value, InsertMode.ADD_VALUES);
	}
	public void add(double alpha, TaoHessian h) {
		H.aXPY(alpha, h.getMat(), MatStructure.SAME_NONZERO_PATTERN);
	}
	@Override
	public void set(int i, int j, double value) {
		H.setValue(i, j, value, InsertMode.INSERT_VALUES);
	}
	@Override
	public void setZero() {
		H.zeroEntries();
	}

	public Mat getMat() {
		return H;
	}
	
}