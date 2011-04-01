package de.varylab.varylab.hds;

import de.jreality.math.Matrix;
import de.jtem.halfedge.Vertex;

public class VVertex extends Vertex<VVertex, VEdge, VFace> {

	public double[]
	    position = null,
	    texcoord = null;
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
		System.arraycopy(position, 0, homPosition, 0, position.length);
		homPosition[3]=1;
		homPosition = t.multiplyVector(homPosition);
		for(int i = 0; i < 3; i++ ) {
			position[i] = homPosition[i]/homPosition[3];
		}
	}
	
	@Override
	public void copyData(VVertex v) {
		super.copyData(v);
		if (v.position != null) position = v.position.clone();
		if (v.texcoord != null) texcoord = v.texcoord.clone();
		variable = v.variable;
		weight = v.weight;
	}
	
}
