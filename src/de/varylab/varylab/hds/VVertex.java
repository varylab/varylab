package de.varylab.varylab.hds;

import geom3d.Point;
import de.jreality.math.Matrix;
import de.jtem.halfedge.Vertex;
import de.varylab.discreteconformal.heds.bsp.HasBspPos;

public class VVertex extends Vertex<VVertex, VEdge, VFace> implements HasBspPos {

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

	public Point getPosition() {
		return new Point(position);
	}
	public void setPosition(Point p) {
		position = p.get();
	}
	
	public Point getTexCoord() {
		return new Point(texcoord);
	}
	public void setTexCoord(Point tc) {
		texcoord = tc.get();
	}
	
	@Override
	public Point getBspPos() {
		return new Point(position);
	}
	@Override
	public void setBspPos(Point p) {
		position = p.get();
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
	
}
