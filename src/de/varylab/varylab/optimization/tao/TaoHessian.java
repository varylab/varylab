/**
 * 
 */
package de.varylab.varylab.optimization.tao;

import static de.jtem.jpetsc.InsertMode.ADD_VALUES;
import static de.jtem.jpetsc.InsertMode.INSERT_VALUES;
import static de.jtem.jpetsc.MatStructure.SAME_NONZERO_PATTERN;
import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.jpetsc.Mat;

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
		H.setValue(i, j, value, ADD_VALUES);
	}
	@Override
	public void add(double alpha, Hessian h) {
		H.aXPY(alpha, ((TaoHessian)h).getMat(), SAME_NONZERO_PATTERN);
	}
	@Override
	public void set(int i, int j, double value) {
		H.setValue(i, j, value, INSERT_VALUES);
	}
	@Override
	public void setZero() {
		H.zeroEntries();
	}

	public Mat getMat() {
		return H;
	}
	
}