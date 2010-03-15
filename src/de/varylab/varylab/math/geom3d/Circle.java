package de.varylab.varylab.math.geom3d;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;


public class Circle implements Geom3D {

	protected Point
	    center = new Point(0.0, 0.0, 0.0);
	protected Plane
		plane = new Plane(1, 0, 0, 0);
	protected double
		radius = 1.0;
	
	public Circle() {
		
	}
	
	public Circle(Circle c) {
		center.set(c.center);
		plane.set(c.plane);
		radius = c.radius;
	}
	
	public Circle(Point p1, Point p2, Point p3) {
		Triangle t = new Triangle(p1, p2, p3);
		Circle c = t.getCircumCircle();
		center = c.center;
		plane = c.plane;
		radius = c.radius;
	}
	
	
	public Circle(Point center, Vector normal, double radius) {
		this.center.set(center);
		this.plane = new Plane(normal, center);
		this.radius = radius;
	}
	

	public void set(Circle c) {
		center.set(c.center);
		plane.set(c.plane);
		radius = c.getRadius();
	}
	
	public Point getCenter() {
		return center;
	}


	public void setCenter(Point center) {
		this.center.set(center);
	}



	public Plane getPlane() {
		return plane;
	}

	public void setPlane(Plane plane) {
		this.plane = plane;
	}

	public double getRadius() {
		return radius;
	}


	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	
	public boolean isOnCircle(Point p, double eps) {
		if (!plane.isInPlane(p))
			return false;
		return abs(center.distanceTo(p) - radius) < eps;
	}
	
	public boolean isOnCircle(Point p) {
		return isOnCircle(p, 1E-10);
	}
	
	
	public boolean intersectsTriangle(Triangle t) {
		return false;
	}
	/**
	 * 
	 * @param t
	 * @return
	 */
	public List<Point> intersect(Plane p1) {
		/*
		 * Solves the system of equations
		 * a1*x + b1*y + c1*z + d1 = 0
		 * a2*x + b2*y + c2*z + d2 = 0
		 * x^2   + y^2  + z^2      = r^2
		 */
		boolean rotate=false;
		LinkedList<Point> result = new LinkedList<Point>();
		Plane p2 = getPlane();
		Matrix m = new Matrix();
		if (p1.isParallel(p2)) // no intersection for parallel planes
			return result;
		double r = radius;
		double a1 = p1.n.vec[0];
		double b1 = p1.n.vec[1];
		double c1 = p1.n.vec[2];
		double d1 = p1.d;
		double a2 = p2.n.vec[0];
		double b2 = p2.n.vec[1];
		double c2 = p2.n.vec[2];
		double d2 = p2.d;
		double xb = center.x();
		double yb = center.y();
		double zb = center.z();
		double δ = (b2*a1 - b1*a2);
		
		if (δ == 0.0) {
			rotate=true;
			m = MatrixBuilder.euclidean().rotate(Math.PI/4, p1.n.vec).rotate(Math.PI/4, p2.n.vec).getMatrix();
			double[] n1 = p1.n.vec;
			double[] n2 = p2.n.vec;
			
			n1 = m.multiplyVector(n1);
			n2 = m.multiplyVector(n2);
			
			double[] centre = center.vec;
			centre = m.multiplyVector(centre);
			a1=n1[0];
			b1=n1[1];
			c1=n1[2];
			a2=n2[0];
			b2=n2[1];
			c2=n2[2];
			xb = centre[0];
			yb = centre[1];
			zb = centre[2];
			δ = (b2*a1 - b1*a2);
			if(δ==0){
				System.err.println("δ1 in intersect method is 0");
				return result;
			}
		}
		double α1 = (c2*a1 - a2*c1) / -δ;
		double α2 = (d2*a1 - a2*d1) / -δ;
		double β1 = (b1*c2 - b2*c1) / δ;
		double β2 = (b1*d2 - b2*d1) / δ;
		double γ1 = 1 + α1*α1 + β1*β1; 
		double γ2 = 2 * (β1*(β2 - xb) + α1*(α2 - yb) - zb);
		double γ3 = (β2 - xb)*(β2 - xb) + (α2 - yb)*(α2 - yb) + zb*zb - r*r;
		double p = γ2 / γ1; 
		double q = γ3 / γ1;
		double Θ = p*p/4 - q;
		if (Θ < 0) // only imaginary ⇒ no intersection
			return result;
		
		double z1 = -p / 2 + sqrt(Θ); 
		double y1 = α1*z1 + α2;
		double x1 = β1*z1 + β2;
		
		double z2 = -p / 2 - sqrt(Θ); 
		double y2 = α1*z2 + α2;
		double x2 = β1*z2 + β2;
		if(rotate==true){
			m.transpose();
			double[] point1 = new double[]{x1,y1,z1};
			double[] point2 = new double[]{x2,y2,z2};
			point1 = m.multiplyVector(point1);
			point2 = m.multiplyVector(point2);
			result.add(new Point(point1));
			result.add(new Point(point2));
			return result;
		}
		
		result.add(new Point(x1, y1, z1));
		result.add(new Point(x2, y2, z2));
		return result;
	}


	public List<Point> intersect(Triangle t) {
		List<Point> planeIntersections = intersect(t.getPlane());
		ArrayList<Point> result = new ArrayList<Point>();
		for (Point p : planeIntersections)
			if (t.isInTriangle(p))
				result.add(p);
		return result;
	}
	
	
	public Point projectOnto(Point p) {
		Point result = plane.projectOnto(p);
		double dist = result.distanceTo(center);
		Vector vec = center.vectorTo(result);
		double distToRad = radius - dist;
		if (distToRad == 0.0) {
			return p;
		} else {
			vec.scaleTo(distToRad);
		}
		return result.add(vec).asPoint();
	}
	
	
	public JRCircle getJR(AppearanceContext context) {
		return new JRCircle(this,context);
	}
	
	
}
