package de.varylab.varylab.plugin.nurbs.data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;



public class IntersectionPoint {
	
	public enum ClosedBoundary {left, right, upper, lower, interior};
	public enum FaceVertex {faceVertex, noFaceVertex};
	private double[] point = null;
	private double sameIndexDist;
	private LinkedList<LineSegment> intersectingSegments;
	private ClosedBoundary closedBoundary = null;
	private FaceVertex faceVertex = null;
	private LinkedList<Integer> indexList = null;
	private LinkedList<IntersectionPoint> nbrs;
	private LinkedList<IntersectionPoint> unusedNbrs;
	private IntersectionPoint previous;
	private IntersectionPoint opposite = null;
	private Boolean boundaryPoint = null;
	

	public IntersectionPoint() {
		
	}
	

	public Boolean isBoundaryPoint(List<Double> boundaryValues){
		if(boundaryPoint == null){
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
		else{
			return boundaryPoint;
		}
		
	}
	
	

	public void setBoundaryPoint(boolean boundaryPoint) {
		this.boundaryPoint = boundaryPoint;
	}






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
	

	public FaceVertex getFaceVertex() {
		if(faceVertex != null){
			return faceVertex;
		}
		else{
			if(getIndexList().size() > 2){
				return FaceVertex.faceVertex;
			}
			return FaceVertex.noFaceVertex;
		}
	}
	
	

	public IntersectionPoint getOpposite() {
		return opposite;
	}

	public void setOpposite(IntersectionPoint opposite) {
		this.opposite = opposite;
	}

	public void setFaceVertex(FaceVertex faceVertex) {
		this.faceVertex = faceVertex;
	}

	public LinkedList<IntersectionPoint> getNbrs() {
		return nbrs;
	}

	public void setNbrs(LinkedList<IntersectionPoint> nbrs) {
		this.nbrs = nbrs;
	}

	public LinkedList<IntersectionPoint> getUnusedNbrs() {
		return unusedNbrs;
	}

	public void setUnusedNbrs(LinkedList<IntersectionPoint> unusedNbrs) {
		this.unusedNbrs = unusedNbrs;
	}

	public IntersectionPoint getPrevious() {
		return previous;
	}

	public void setPrevious(IntersectionPoint previous) {
		this.previous = previous;
	}

	public ClosedBoundary getClosedBoundary() {
		return closedBoundary;
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



	public LinkedList<LineSegment> getIntersectingSegments() {
		return intersectingSegments;
	}

	public void setIntersectingSegments(LinkedList<LineSegment> intersectingSegments) {
		this.intersectingSegments = intersectingSegments;
	}
	
	
	


	@Override
	public String toString() {
		return "IntersectionPoint [point=" + Arrays.toString(point) + "]";
	}

}
