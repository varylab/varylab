package de.varylab.varylab.plugin.nurbs.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;



public class IntersectionPoint {
	
	private static Logger logger = Logger.getLogger(IntersectionPoint.class.getName());
	
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
	private LinkedList<CurveTangent> curveTangents = null;
	

	
	
	public IntersectionPoint() {
		
	}
	
	
	public LinkedList<CurveTangent> getCurveTangents() {
		return curveTangents;
	}


	public void setCurveTangents(LinkedList<CurveTangent> curveTangents) {
		this.curveTangents = curveTangents;
	}
	
	
	// nicht fertig !!!
	public LinkedList<CurveTangent> determineCurveTangents() {
		LinkedList<CurveTangent> ct = new LinkedList<>();
		LinkedList<Integer> indexList = new LinkedList<>();
		for (LineSegment ls : intersectingSegments) {
			if(!indexList.contains(ls.getCurveIndex())){
				indexList.add(ls.getCurveIndex());
			}
		}
		return ct;
	}




	public Boolean isBoundaryPoint(List<Double> boundaryValues){
		if(boundaryPoint == null){
			double[] point = getPoint();
			for (Double value : boundaryValues) {
				if(point[0] == value || point[1] == value){
					logger.info("point " + Arrays.toString(point) + " is a boundary point");
					return true;
				}
			}
			logger.info("point [" + point[0] + ", " + point[1] +"] is NOT a boundary point");
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
	
	public boolean listContainsPoint(IntersectionPoint p, List<IntersectionPoint> ipList){
		for (IntersectionPoint ip : ipList) {
			if(p.getPoint()[0] == ip.getPoint()[0] && p.getPoint()[1] == ip.getPoint()[1]){
//			if(p == ip){
				return true;
			}
		}
		return false;
	}
	
	public double getAngle(double[] vec){
		double angle = Math.atan2(vec[1], vec[0]);
		if(angle < 0){
			angle += 2 * Math.PI;
		}
		return angle;
	}
	
	public LinkedList<IntersectionPoint> removeMultiplePoints(List<IntersectionPoint> ipList){
		LinkedList<IntersectionPoint> singleList = new LinkedList<>();
		for (IntersectionPoint ip : ipList) {
			if(!listContainsPoint(ip, singleList)){
				singleList.add(ip);
			}
		}
		return singleList;
	}
	
	public  void makeOrientedNbrs (){
			LinkedList<IntersectionPoint> orientedNbrs = new LinkedList<>();
			HashMap<Double, IntersectionPoint> angleMap = new HashMap<>();
			PriorityQueue<Double> angleQueue = new PriorityQueue<>();
			setNbrs(removeMultiplePoints(getNbrs()));
			IntersectionPoint first = getNbrs().remove();
			double[] firstVec = Rn.subtract(null, first.getPoint(), getPoint());
			double firstAngle = getAngle(firstVec);
			angleQueue.add(2 * Math.PI);
			angleMap.put(2 * Math.PI, first);
			for (IntersectionPoint nbr : getNbrs()) {
				double[] currVec = Rn.subtract(null, nbr.getPoint(), getPoint());
				double currAngle = getAngle(currVec);
				currAngle -= firstAngle;
				if(currAngle < 0){
					currAngle += 2 * Math.PI;
				}
				angleQueue.add(currAngle);
				angleMap.put(currAngle, nbr);
			}
			while(!angleQueue.isEmpty()){
				orientedNbrs.add(angleMap.get(angleQueue.poll()));
			}
			first = orientedNbrs.getFirst();
			orientedNbrs.add(first);
			Collections.reverse(orientedNbrs);
			setNbrs(orientedNbrs);
		
	}
	
	
	


	@Override
	public String toString() {
		String str = new String();
		str += "IntersectionPoint [point=" + Arrays.toString(point) + "] , curve indices = ";
		for (LineSegment ls : intersectingSegments) {
			str += ls.getCurveIndex() + " , ";
		}
//		return "IntersectionPoint [point=" + Arrays.toString(point) + "] , curve indices = " + cur;
		return str;
	}

}
