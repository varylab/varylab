package de.varylab.varylab.math.geom3d;

import static de.jreality.math.Rn.euclideanDistance;
import static java.lang.Math.abs;

import java.util.Arrays;

import de.jreality.math.Rn;

public class Point extends Vector {

	
	public Point() {
		vec[0] = 0.0;
		vec[1] = 0.0;
		vec[2] = 0.0;
	}
	
	public Point(Vector p) {
		super(p);
	}
	
	public Point(double[] p) {
		super(p);
	}
	
	public Point(double x, double y, double z) {
		super(x, y, z);
	}
	

	public Vector vectorTo(Vector p2) {
		Vector result = new Vector();
		Rn.subtract(result.get(), p2.vec, vec);
		return result;
	}
	
	
	public Point move(Vector v) {
		Rn.add(vec, vec, v.get());
		return this;
	}
	
	public Point onLine(Point x, double a) {
		Vector r = new Point(x).subtract(this).times(a);
		return new Point(this).add(r).asPoint();
	}
	
	
	public double distanceTo(Vector p2) {
		return euclideanDistance(vec, p2.vec);
	}
	
	public boolean equals(Point p){
		 return (vec[0]==p.vec[0] && vec[1]==p.vec[1] && vec[2]==p.vec[2]);
	}
	public boolean equals(Point p, double eps){
		 return (abs(vec[0]-p.vec[0])<eps && abs(vec[1]-p.vec[1])<eps && abs(vec[2]-p.vec[2])<eps);
	}
	
	public Point zero() {
		vec[0] = 0.0;
		vec[1] = 0.0;
		vec[2] = 0.0;
		return this;
	}
	
	
	@Override
	public String toString() {
		return "Point: " + Arrays.toString(vec);
	}
	
	
	@Override
	public JRPoint getJR(AppearanceContext context) {
		return new JRPoint(this, context);
	}
	
}
