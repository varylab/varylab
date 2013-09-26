package de.varylab.varylab.plugin.nurbs.data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;



public class IntersectionPoint {
	
	public enum ClosedBoundary {left, right, upper, lower, interior};
	private double[] point = null;
	private double sameIndexDist;
	private HalfedgePoint parentHP = null;
	private LinkedList<LineSegment> intersectingSegments;
	private ClosedBoundary closedBoundary = null;
	private LinkedList<Integer> indexList = null;
	

	public IntersectionPoint() {
		
	}
	
//	public IntersectionPoint clone(){
//		IntersectionPoint clone = new IntersectionPoint();
//		clone.setClosedBoundary(closedBoundary);
//		clone.setIndexList(indexList);
//		clone.setIntersectingSegments(intersectingSegments);
//		clone.setParentHP(parentHP);
//		clone.setPoint(point);
//		clone.setSameIndexDist(sameIndexDist);
//		return clone;
//	}
	
	
	public ClosedBoundary getClosedBoundary(NURBSSurface ns, double dilation){
		if(closedBoundary == null){
			if(ns.getClosingDir() == ClosingDir.uClosed){
				double [] bound  = ns.getClosedBoundaryValuesPastIntersection(dilation);
				if(getPoint()[0] == bound[0]){
					closedBoundary = ClosedBoundary.left;
				}
				else if(getPoint()[0] == bound[1]){
					closedBoundary = ClosedBoundary.right;
				}
				else{
					closedBoundary = ClosedBoundary.interior;
				}
			}
			else if(ns.getClosingDir() == ClosingDir.vClosed){
				double [] bound  = ns.getClosedBoundaryValuesPastIntersection(dilation);
				if(getPoint()[1] == bound[0]){
					closedBoundary = ClosedBoundary.lower;
				}
				else if(getPoint()[1] == bound[1]){
					closedBoundary = ClosedBoundary.upper;
				}
				else{
					closedBoundary = ClosedBoundary.interior;
				}
			}
			else{
				closedBoundary = ClosedBoundary.interior;
			}
		}
		return closedBoundary;
	}
	
	public void setClosedBoundary(ClosedBoundary cb){
		closedBoundary = cb;
	}


	public LinkedList<Integer> getIndexList() {
		if(indexList != null){
			return indexList;
		} else {
			indexList = new LinkedList<Integer>();
			for (LineSegment ls : intersectingSegments) {
				if(!indexList.contains(ls.getCurveIndex())){
					indexList.add(ls.getCurveIndex());
				}
			}
			return indexList;
		}
	}

	public void setIndexList(LinkedList<Integer> indexList) {
		this.indexList = indexList;
	}

	public double[] getPoint() {
		return point;
	}

	public void setPoint(double[] point) {
		this.point = point;
	}

	public double getSameIndexDist() {
		return sameIndexDist;
	}

	public void setSameIndexDist(double sameIndexDist) {
		this.sameIndexDist = sameIndexDist;
	}

	public HalfedgePoint getParentHP() {
		return parentHP;
	}

	public void setParentHP(HalfedgePoint parentHP) {
		this.parentHP = parentHP;
	}

	public LinkedList<LineSegment> getIntersectingSegments() {
		return intersectingSegments;
	}

	public void setIntersectingSegments(LinkedList<LineSegment> intersectingSegments) {
		this.intersectingSegments = intersectingSegments;
	}
	
	
	public boolean isBoundaryPoint(List<Double> boundaryValues){
		double[] point = getPoint();
		for (Double value : boundaryValues) {
			if(point[0] == value || point[1] == value){
				System.out.println("point " + Arrays.toString(point) + " is a boundary point");
				return true;
			}
		}
		System.out.println("point [" + point[0] + ", " + point[1] +"] is NOT a boundary point");
		return false;
	}


	@Override
	public String toString() {
		return "IntersectionPoint [point=" + Arrays.toString(point) + "]";
	}

}
