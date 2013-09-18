package de.varylab.varylab.plugin.nurbs.data;

import java.util.Arrays;
import java.util.LinkedList;


public class HalfedgePoint {
	
	

	private IntersectionPoint point;
	private LinkedList<IntersectionPoint> nbrs;
	private LinkedList<IntersectionPoint> unusedNbrs;
	private IntersectionPoint before;
	
	

	public HalfedgePoint(){
		
	}
	
	public HalfedgePoint(IntersectionPoint p, LinkedList<IntersectionPoint> n){
		point = p;
		nbrs = n;
	}
	

	public HalfedgePoint(IntersectionPoint point, int indexOnMaxCurve,
			int indexOnMinCurve, int maxCuveIndex, int minCurveIndex,
			LinkedList<IntersectionPoint> maxNbrs,
			LinkedList<IntersectionPoint> minNbrs) {
		super();
		this.point = point;
		this.unusedNbrs = maxNbrs;
	}
	
	

	public IntersectionPoint getPrevious() {
		return before;
	}

	public void setPrevious(IntersectionPoint before) {
		this.before = before;
	}

	public LinkedList<IntersectionPoint> getNbrs() {
		return nbrs;
	}

	public void setNbrs(LinkedList<IntersectionPoint> nbrs) {
		this.nbrs = nbrs;
	}




	public IntersectionPoint getIntersectionPoint() {
		return point;
	}

	public void setIntersectionPoint(IntersectionPoint point) {
		this.point = point;
	}


	public LinkedList<IntersectionPoint> getUnusedNbrs() {
		return unusedNbrs;
	}

	public void setUnusedNbrs(LinkedList<IntersectionPoint> unusedNbrs) {
		this.unusedNbrs = unusedNbrs;
	}




	@Override
	public String toString() {
		System.out.println("HalfedgePoint:" + Arrays.toString(point.getPoint()));
		for (IntersectionPoint n : nbrs) {
			System.out.println(Arrays.toString(n.getPoint()));
		}
		return "";
	}
	
	

	
}
