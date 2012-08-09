package de.varylab.varylab.plugin.nurbs.data;

import java.util.Arrays;
import java.util.LinkedList;

public class LineSegment {
	
	protected double[][] segment;
	protected int indexOnCurve = Integer.MIN_VALUE;
	protected int curveIndex = Integer.MIN_VALUE;
	protected LinkedList<double[]> ePoints;
	protected boolean cyclic;
//	protected boolean max;
	
	public LineSegment(){
		
	}
	
	public LineSegment(double[][] s , int iOC,int cI){
		segment = s;
		indexOnCurve = iOC;
		curveIndex = cI;
	}
	
	public static enum PointStatus {
		upper,
		containsInterior,
		lower
	}
	
	
	public LinkedList<double[]> getePoints() {
		return ePoints;
	}

	public void setePoints(LinkedList<double[]> ePoints) {
		this.ePoints = ePoints;
	}

	public double[][] getSegment() {
		return segment;
	}

	public void setSegment(double[][] segment) {
		this.segment = segment;
	}

	public int getIndexOnCurve() {
		return indexOnCurve;
	}

	public void setIndexOnCurve(int indexOnCurve) {
		this.indexOnCurve = indexOnCurve;
	}

	public int getCurveIndex() {
		return curveIndex;
	}

	public void setCurveIndex(int curveIndex) {
		this.curveIndex = curveIndex;
	}

	
	public boolean isCyclic() {
		return cyclic;
	}

	public void setCyclic(boolean cyclic) {
		this.cyclic = cyclic;
	}

	@Override
	public String toString() {
		return //"LineSegmentIntersection [segment=" + Arrays.toString(segment[0]) + " " + Arrays.toString(segment[1])
				//+ ", index=" + indexOnCurve +
				curveIndex+ "|" + indexOnCurve + " endpoints " + Arrays.toString(segment[0]) + Arrays.toString(segment[1]);
	}
	

}
