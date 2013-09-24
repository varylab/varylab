package de.varylab.varylab.halfedge;

import de.jreality.math.Matrix;
import de.jtem.halfedge.Vertex;

public class VVertex extends Vertex<VVertex, VEdge, VFace> {

	private double[]
	    P = {0, 0, 0, 1},
	    T = {0, 0, 0, 1};
	private boolean
		variable = true;
	private double
		weight = 1.0;

	public boolean isVariable() {
		return variable;
	}
	public void setVariable(boolean variable) {
		this.variable = variable;
	}
	
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void applyTransformation(Matrix t) {
		double[] homPosition = new double[4];
		System.arraycopy(P, 0, homPosition, 0, P.length);
		homPosition[3]=1;
		homPosition = t.multiplyVector(homPosition);
		for(int i = 0; i < 3; i++ ) {
			P[i] = homPosition[i]/homPosition[3];
		}
	}
	
	@Override
	public void copyData(VVertex v) {
		super.copyData(v);
		if (v.P != null) P = v.P.clone();
		if (v.T != null) T = v.T.clone();
		variable = v.variable;
		weight = v.weight;
	}
	
	public double[] getP() {
		return P;
	}
	public void setP(double[] p) {
		System.arraycopy(p, 0, P, 0, 4);
	}
	public double[] getT() {
		return T;
	}
	public void setT(double[] t) {
		System.arraycopy(t, 0, T, 0, 4);
	}
	
}
