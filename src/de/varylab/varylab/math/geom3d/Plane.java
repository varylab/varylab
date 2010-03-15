package de.varylab.varylab.math.geom3d;

import static de.jreality.math.Rn.add;
import static de.jreality.math.Rn.crossProduct;
import static de.jreality.math.Rn.innerProduct;
import static de.jreality.math.Rn.normalize;
import static de.jreality.math.Rn.subtract;
import static de.jreality.math.Rn.times;
import static java.lang.Math.abs;

import java.util.Arrays;

import de.jreality.math.Rn;

public class Plane implements Geom3D {

	protected Vector
	    n = new Vector(0, 0, 1);
	protected double 
		d = 0.0;
	
	public Plane() {

	}

	
	public Plane(Plane p) {
		n.set(p.n);
		d = p.d;
	}
	
	/**
	 * 
	 * @param a,b,c,d the parameters in the equation: 
	 *         a*x1 + b*x2 + c*x3 + d = 0
	 */
	public Plane(double a, double b, double c, double d) {
		n.set(a, b, c);
		this.d = d / n.getLength();
		n.normalize();
	}
	
	public Plane(Point p1, Point p2, Point p3) {
		double[] xy = subtract(null, p2.get(), p1.get());
		double[] xz = subtract(null, p3.get(), p1.get());
		double[] n = crossProduct(null, xy, xz);
		normalize(n, n);
		double d = -Rn.innerProduct(n, p1.get());
		this.n.set(n);
		this.d = d;
	}
	
	public Plane(double[] normal,double d){
		n.set(normal);
		n.normalize();
		this.d = -d;
	}
	public Plane(Vector n, double d) {
		this.n = n;
		n.normalize();
		this.d = -d;
	}
	
	public Plane(Vector normal, Point p) {
		n.set(normal);
		n.normalize();
		d = -n.dot(p);
	}
	
	public void flipNormal() {
		n.times(-1);
		d *= -1;
	}
	
	public boolean isParallel(Plane p2){
		double x = Rn.innerProduct(n.get(), p2.n.get());
		return abs(x) > 1 - 1E-8;
	}
	
	public boolean isInPlane(Point p, double eps) {
		return abs(innerProduct(n.get(), p.get()) + d) < eps;
	}
	
	public boolean isInPlane(Point p) {
		return isInPlane(p, 1E-8);
	}
	
	
	public Point projectOnto(Point p) {
		double dp = innerProduct(n.get(), p.get()) + d;
		double[] mov = times(null, -dp, n.get());
		Point result = new Point(add(null, p.get(), mov));
		return result;
	}
	/**
	 * 
	 * @param p
	 * @return true, if the point is above or on this plane  
	 *          	  in the direction of this normal
	 *          false, if its "under" this plane
	 */
	public boolean isAbove(Point p){
		return Rn.innerProduct(n.get(), p.get())>=-d;
	}
	@Override
	public String toString() {
		return "Plane n: " + Arrays.toString(n.get()) + " d: " + d;
	}


	public Plane set(Plane p) {
		n.set(p.n);
		d = p.d;
		return this;
	}
	
	
	public Vector getN() {
		return n;
	}


	public void setN(Vector n) {
		this.n = n;
	}


	public double getD() {
		return d;
	}


	public void setD(double d) {
		this.d = d;
	}


	public JRPlane getJR(AppearanceContext context) {
		return new JRPlane(this,context);
	}
	
}
