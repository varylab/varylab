package de.varylab.varylab.math.geom3d;

import static java.lang.Math.PI;
import static java.lang.Math.sin;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;


public class Triangle implements Geom3D {

	
	protected Point
	    a = new Point(0.0, 0.0, 0.0),
		b = new Point(1.0, 0.0, 0.0),
		c = new Point(0.0, 1.0, 0.0);
	
	public Triangle(){
		
	}
	
	public Triangle(Triangle t) {
		a.set(t.getA());
		b.set(t.getB());
		c.set(t.getC());
	}
	
	
	public Triangle(Point a, Point b, Point c) {
		this.a.set(a);
		this.b.set(b);
		this.c.set(c);
	}
	
	public Point[] getVertices() {
		return new Point[] {a, b, c};
	}
	
	public Point getA() {
		return a;
	}
	
	public Point getB() {
		return b;
	}
	
	public Point getC() {
		return c;
	}
	
	public Triangle set(Triangle t) {
		a.set(t.a);
		b.set(t.b);
		c.set(t.c);
		return this;
	}
	
	
	public void setA(Point a) {
		this.a.set(a);
	}
	
	public void setB(Point b) {
		this.b.set(b);
	}
	
	public void setC(Point c) {
		this.c.set(c);
	}
	
	public Plane getPlane() {
		return new Plane(a, b, c);
	}
	
	public Point getBaryCenter() {
		Vector result = new Vector(0, 0, 0).add(a).add(b).add(c);
		result.times(1/3.);
		return new Point(result);
	}
	

	/**
	 * Calculates the radius of the circum of three points in 3-space
	 * @param triangle the three points
	 * @return the radius of the circum
	 */
	public double getCircumRadius() {
		Vector ab = a.vectorTo(b);
		Vector ac = a.vectorTo(c);
		double alpha = ab.getAngle(ac);
		if (alpha < 1E-8 || alpha + 1E-8 > PI)
			throw new IllegalArgumentException("Collinear triangle vertices in getCircumCenterRadius()");
		double la = b.distanceTo(c);
		return la / 2.0 / sin(alpha);
	}

	/**
	 * Calculates the circumcenter of three points in 3-space
	 * @param three points in 3-space
	 * @return center, this triangle's circumcenter
	 */
	public Point getCircumCenter() {
		Vector ab = a.vectorTo(b);
		Vector ac = a.vectorTo(c);
		Vector n = ab.getNormal(ac);
		double dab = (ab.dot(a) + ab.dot(b)) / 2;
		double dac = (ac.dot(a) + ac.dot(c)) / 2;
		double dn = n.dot(c);
		
		no.uib.cipr.matrix.Vector E1 = new DenseVector(ab.get());
		no.uib.cipr.matrix.Vector E2 = new DenseVector(ac.get());
		no.uib.cipr.matrix.Vector E3 = new DenseVector(n.get());
		no.uib.cipr.matrix.Vector d = new DenseVector(new double[] {dab, dac, dn});
		
		Matrix A = new DenseMatrix(new no.uib.cipr.matrix.Vector[] {E1, E2, E3}).transpose();
		no.uib.cipr.matrix.Vector x = new DenseVector(3);
		A.solve(d, x);
		return new Point (x.get(0), x.get(1), x.get(2));
	}

	
	/**
	 * Calculates the circum circle of a triangle in 3-space
	 * @param triangle
	 * @return circum circle
	 * @throws IllegalArgumentException
	 */
	public Circle getCircumCircle() {
		Point center = getCircumCenter();
		double radius = getCircumRadius();
		return new Circle(center, getPlane().getN(), radius);
	}
	
	/**
	 * Checks, whether the given point is inside this triangle
	 * @param p, the point that is checked
	 * @return true, if the point is inside
	 */
	public boolean isInTriangle(Point p){
		if(!this.getPlane().isInPlane(p))
			return false;
		Vector ab = a.vectorTo(b);
		Vector bc = b.vectorTo(c);
		Vector ca = c.vectorTo(a);
		
		Vector nab = a.vectorTo(c);
		nab.makeOrthogonalTo(ab);
		Vector nbc = b.vectorTo(a);
		nbc.makeOrthogonalTo(bc);
		Vector nca = c.vectorTo(b);
		nca.makeOrthogonalTo(ca);
		
		Plane pab = new Plane(nab, a);  
		Plane pbc = new Plane(nbc, b); 
		Plane pca = new Plane(nca, c); 
		return pab.isAbove(p) && pbc.isAbove(p) && pca.isAbove(p);
	}
	
	public double computeArea(){
		double g = a.vectorTo(b).getLength();
		Vector v = new Vector(a).add(a.vectorTo(c).projectOnto(a.vectorTo(b)));
		double h = v.asPoint().distanceTo(c);
		return g*h/2.0;
	}
	
	
	@Override
	public String toString() {
		return "Triangle: " + a + ", " + b + ", " + c;
	}

	public JRTriangle getJR(AppearanceContext context) {
		return new JRTriangle(this, context);
	}
	
	
	/**
	 * Angle at a
	 */
	public static double angleAt(Point a, Point b, Point c) {
		Vector ab = a.vectorTo(b);
		Vector ac = a.vectorTo(c);
		Vector cr = ab.cross(ac);
		double angle = Math.atan2(Math.sqrt(cr.dot(cr)), ab.dot(ac));
		if (!(0 <= angle && angle <= PI))
			throw new IllegalArgumentException("Illegal triangle in angleAt()");
		return angle;
	}
	
	
	public double angleAtA() {
		return angleAt(getA(), getB(), getC());
	}
	
	
	public double angleAtB() {
		return angleAt(getB(), getC(), getA());
	}
	
	public double angleAtC() {
		return angleAt(getC(), getA(), getB());
	}
	
}
