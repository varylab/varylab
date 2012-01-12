package de.varylab.varylab.plugin.nurbs.data;

import java.util.List;


public class LineSegmentsInBoxDomain {
	
	private List<LineSegment> SegmentList;
	private List<Integer> curveIndicies;
	private double[] xBoxDomain;
	private double[] yBoxDomain;
	private boolean isUpperXBound;
	private boolean isLowerXBound;
	private boolean isUpperYBound;
	private boolean isLowerYBound;
	
	
	public LineSegmentsInBoxDomain() {
		super();
		isUpperXBound = false;
		isLowerXBound = false;
		isUpperYBound = false;
		isLowerYBound = false;
	}


	public List<LineSegment> getSegmentList() {
		return SegmentList;
	}


	public void setSegmentList(List<LineSegment> segmentList) {
		SegmentList = segmentList;
	}


	public List<Integer> getCurveIndicies() {
		return curveIndicies;
	}


	public void setCurveIndicies(List<Integer> curveIndicies) {
		this.curveIndicies = curveIndicies;
	}


	


	public double[] getxBoxDomain() {
		return xBoxDomain;
	}


	public void setxBoxDomain(double[] xBoxDomain) {
		this.xBoxDomain = xBoxDomain;
	}


	public double[] getyBoxDomain() {
		return yBoxDomain;
	}


	public void setyBoxDomain(double[] yBoxDomain) {
		this.yBoxDomain = yBoxDomain;
	}


	public boolean isUpperXBound() {
		return isUpperXBound;
	}


	public void setUpperXBound(boolean isUpperXBound) {
		this.isUpperXBound = isUpperXBound;
	}


	public boolean isLowerXBound() {
		return isLowerXBound;
	}


	public void setLowerXBound(boolean isLowerXBound) {
		this.isLowerXBound = isLowerXBound;
	}


	public boolean isUpperYBound() {
		return isUpperYBound;
	}


	public void setUpperYBound(boolean isUpperYBound) {
		this.isUpperYBound = isUpperYBound;
	}


	public boolean isLowerYBound() {
		return isLowerYBound;
	}


	public void setLowerYBound(boolean isLowerYBound) {
		this.isLowerYBound = isLowerYBound;
	}
	
	

}
