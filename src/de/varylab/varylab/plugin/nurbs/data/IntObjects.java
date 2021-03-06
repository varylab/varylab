package de.varylab.varylab.plugin.nurbs.data;

import java.util.LinkedList;

import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.VectorFields;


public class IntObjects {
	
	protected LinkedList<double[]> points;
	protected double[] orientation;
	protected boolean nearby;
	protected VectorFields maxMin;
	protected boolean cyclic = false;
	//only for debugging

	public IntObjects(){
		points = null;
		orientation = null;
		nearby = false;
	}
	
	public IntObjects(LinkedList<double[]> p,double[] o,boolean n, VectorFields m){
		points = p;
		orientation = o;
		nearby = n;
		maxMin = m;
	}
	
	public IntObjects(LinkedList<double[]> pointList, double[] ori,	boolean nearBy2, boolean conj) {
		this(pointList, ori, nearBy2, conj?VectorFields.FIRST:VectorFields.SECOND);
	}

	public LinkedList<double[]> getPoints() {
		return points;
	}

	public void setPoints(LinkedList<double[]> points) {
		this.points = points;
	}

	public double[] getOrientation() {
		return orientation;
	}

	public void setOrientation(double[] orientation) {
		this.orientation = orientation;
	}

	public boolean isNearby() {
		return nearby;
	}

	public void setNearby(boolean nearby) {
		this.nearby = nearby;
	}

	public VectorFields isMaxMin() {
		return maxMin;
	}

	public void setMaxMin(VectorFields maxMin) {
		this.maxMin = maxMin;
	}

	public boolean isCyclic() {
		return cyclic;
	}

	public void setCyclic(boolean cyclic) {
		this.cyclic = cyclic;
	}
	

	
}
