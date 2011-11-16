package de.varylab.varylab.plugin.nurbs.data;

import java.util.Arrays;
import java.util.LinkedList;



public class IntersectionPoint {
	
	protected double[] point = null;
	protected double sameIndexDist;
	protected HalfedgePoint parentHP = null;
	protected LinkedList<LineSegment> intersectingSegments;

	
	public IntersectionPoint() {
		//point = new double[2];
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





	@Override
	public String toString() {
		return "IntersectionPoint [point=" + Arrays.toString(point) + "]";
	}



	



	


	



	
	

}
