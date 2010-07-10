package de.varylab.varylab.plugin.remeshing;


public class Compass {

	private Slope[] slopes = null;
	private Slope[] ijSlopes = null;
	
	public Compass(Slope[] s, Slope[] ijs) {
		slopes = new Slope[s.length];
		ijSlopes = new Slope[ijs.length];
		for (int i = 0; i < s.length; i++) {
			slopes[i] = new Slope(s[i]);
			ijSlopes[i] = ijs[i];
		}
	}
	
	public Slope getClosestSlope(double dx, double dy) {
		Slope closestSlope = slopes[0];
		double angle = (Math.abs(dx) < 1E-3)?Math.PI/2:Math.atan(dy/dx);
		double distance = Math.abs(angle - closestSlope.getAngle()) % Math.PI;
		for(int i = 1; i < slopes.length; ++i) {
			if(distance > Math.abs(angle - slopes[i].getAngle())) {
				distance = Math.abs(angle - slopes[i].getAngle());
				closestSlope = slopes[i];
			}
		}
		return closestSlope.times((int)Math.signum(dx*closestSlope.dx+dy*closestSlope.dy));
	}

	public Slope getIJSlope(Slope slope) {
		for (int i = 0; i < slopes.length; i++) {
			int sign = slope.compare(slopes[i]);
			if(sign != 0) {
				return ijSlopes[i].times(sign);
			}
		}
		return null;
	}
	
	
}
