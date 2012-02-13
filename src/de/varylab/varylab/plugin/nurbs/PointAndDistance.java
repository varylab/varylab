package de.varylab.varylab.plugin.nurbs;

public class PointAndDistance {
	
	private double[] point;
	
	private double distance;

	public PointAndDistance(){
		
	}
	
	public PointAndDistance(double[] point, double distance) {
		super();
		this.point = point;
		this.distance = distance;
	}

	public double[] getPoint() {
		return point;
	}

	public void setPoint(double[] point) {
		this.point = point;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	

}
