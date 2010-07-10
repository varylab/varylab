package de.varylab.varylab.plugin.remeshing;

public class Slope {

	double 
		dx = 1,
		dy = 1;
	
	public Slope(double dx, double dy) {
		this.dx=dx;
		this.dy=dy;
	}
	
	public Slope(Slope slope) {
		this.dx = slope.dx;
		this.dy = slope.dy;
	}

	public double getAngle() {
		if(Math.abs(dx) < 1E-3) { 
			return Math.PI/2;
		} else {
			return Math.atan(dy/dx);
		}
	}
	
	public double distance(double[] pt) {
		return -dy*pt[0] + dx*pt[1];
	}
	
	@Override
	public String toString() {
		return new String("( " + dx +", " + dy +" )");
	}
	
	public int compare(Slope s) {
		if(dx == s.dx && dy == s.dy) {
			return 1;
		} else if(dx == -s.dx && dy == -s.dy) {
			return -1;
		} else {
			return 0;
		}
	}

	public Slope times(double d) {
		return new Slope(dx*d,dy*d);
	}
	
	public double[] toArray() {
		return new double[]{dx,dy};
	}
}
