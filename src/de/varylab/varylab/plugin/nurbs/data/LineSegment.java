package de.varylab.varylab.plugin.nurbs.data;

import java.util.Arrays;
import java.util.LinkedList;

import de.jreality.math.Rn;

public class LineSegment {
	
	private double[][] segment = new double[2][];
	private int indexOnCurve = Integer.MIN_VALUE;
	private int curveIndex = Integer.MIN_VALUE;
	private int shiftedIndex = Integer.MAX_VALUE;
	private boolean cyclic;
	private int rightShift;
	private int upShift;
	
	public LineSegment(){
		
	}
	
	public LineSegment(double[] start, double[] target){
		segment[0] = start;
		segment[1] = target;
	}
	
	public LineSegment(double[][] s , int iOC, int cI){
		segment = s;
		indexOnCurve = iOC;
		curveIndex = cI;
	}
	
	public static enum PointStatus {
		upper,
		containsInterior,
		lower
	}
	
	public int getRightShift() {
		return rightShift;
	}

	public void setRightShift(int rightShift) {
		this.rightShift = rightShift;
	}

	public int getUpShift() {
		return upShift;
	}

	public void setUpShift(int upShift) {
		this.upShift = upShift;
	}

	public int getShiftedIndex() {
		return shiftedIndex;
	}

	public void setShiftedIndex(int shiftedIndex) {
		this.shiftedIndex = shiftedIndex;
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
	
	private  LinkedList<double[][]> getTwoSegments(){
		LinkedList<double[][]> twoSegments = new LinkedList<double[][]>();
		double[] p0 = getSegment()[0];
		double[] p1 = getSegment()[1];
		double[] midPoint = new double[2];
		if(p0[0] == p1[0]){
			midPoint[0] = p0[0];
			midPoint[1] = (p0[1] + p1[1]) / 2.0;
		}
		if(p0[1] == p1[1]){
			midPoint[1] = p0[1];
			midPoint[0] = (p0[0] + p1[0]) / 2.0;
		}
		else{
			Rn.times(midPoint, 0.5, Rn.add(null, p0, p1));
		}
		double[][] seg0 = {p0, midPoint};
		double[][] seg1 = {midPoint, p1};
		twoSegments.add(seg0);
		twoSegments.add(seg1);
		return twoSegments;
	}		
	
	public LinkedList<LineSegment> createClosedBoundaryLine(){
		LinkedList<LineSegment> closedBoundary = new LinkedList<LineSegment>();
		int curveIndex = getCurveIndex();
		LinkedList<double[][]> twoSegments = getTwoSegments();
		LineSegment seg0 = new LineSegment(twoSegments.getFirst(), 1, curveIndex);
		seg0.setCyclic(true);
		LineSegment seg1 = new LineSegment(twoSegments.getLast(), 2, curveIndex);
		seg1.setCyclic(true);
		closedBoundary.add(seg0);
		closedBoundary.add(seg1);
		return closedBoundary;
	}

	@Override
	public String toString() {
		return //"LineSegmentIntersection [segment=" + Arrays.toString(segment[0]) + " " + Arrays.toString(segment[1])
				//+ ", index=" + indexOnCurve +
				"curve index = " +curveIndex+ ", index on curve = " + indexOnCurve + ", endpoints " + Arrays.toString(segment[0]) + Arrays.toString(segment[1]) + " rightShift = " + rightShift + ", upShift = " + upShift;
	}


}
