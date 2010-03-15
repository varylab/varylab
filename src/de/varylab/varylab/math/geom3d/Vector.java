package de.varylab.varylab.math.geom3d;

import static de.jreality.math.MatrixBuilder.euclidean;
import static de.jreality.math.Rn.crossProduct;
import static de.jreality.math.Rn.euclideanAngle;
import static de.jreality.math.Rn.euclideanDistance;
import static de.jreality.math.Rn.euclideanNorm;
import static de.jreality.math.Rn.euclideanNormSquared;
import static de.jreality.math.Rn.innerProduct;
import static java.lang.System.arraycopy;

import java.util.Arrays;

import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.SceneGraphComponent;

public class Vector implements Geom3D {

	
	protected double[]
	    vec = {1, 0, 0};
	
	/**
	 * Constructs the vector (1, 0, 0)
	 */
	public Vector() {
	}
	
	/**
	 * Copy constructor
	 * @param p the vector to copy
	 */
	public Vector(Vector p) {
		arraycopy(p.vec, 0, vec, 0, 3);
	}
	
	/**
	 * Constructs a vector with components (x1, x2, x3)
	 * @param x1
	 * @param x2
	 * @param x3
	 */
	public Vector(double x1, double x2, double x3) {
		vec[0] = x1;
		vec[1] = x2;
		vec[2] = x3;
	}
	

	/**
	 * Constructs a vector from the given array
	 * @param vec the array of components
	 */
	public Vector(double[] vec) {
		arraycopy(vec, 0, this.vec, 0, 3);
	}
	
	
	public Vector set(Vector v) {
		arraycopy(v.vec, 0, vec, 0, 3);
		return this;
	}
	
	
	public Vector set(double x1, double x2, double x3) {
		vec[0] = x1;
		vec[1] = x2;
		vec[2] = x3;
		return this;
	}
	
	
	public Vector set(double[] vec) {
		arraycopy(vec, 0, this.vec, 0, 3);
		return this;
	}
	
	public void set(int index, double val) {
		vec[index] = val;
	}
	
	
	public double[] get() {
		return vec;
	}
	
	public double get(int index) {
		return vec[index];
	}
	
	public double x() {
		return vec[0];
	}
	
	public double y() {
		return vec[1];
	}
	
	public double z() {
		return vec[2];
	}
	
	public void setX(double x) {
		this.vec[0] = x;
	}
	
	public void setY(double y) {
		this.vec[1] = y;
	}
	
	public void setZ(double z) {
		this.vec[2] = z;
	}
	
	public Vector normalize() {
		Rn.normalize(vec, vec);
		return this;
	}

	public Vector times(double s) {
		Rn.times(vec, s, vec);
		return this;
	}
	
	public Vector scaleTo(double s) {
		return times(s / getLength());
	}
	
	public double getAngle(Vector v2) {
		return euclideanAngle(vec, v2.vec);
	}
	
	
	public double getLength() {
		return euclideanNorm(vec);
	}
	
	public double getLengthSquared() {
		return euclideanNormSquared(vec);
	}
	
	
	public double dot(Vector v) {
		return innerProduct(v.get(), vec);
	}
	
	public Vector cross(Vector v) {
		double[] tmp = new double[3];
		crossProduct(tmp, vec, v.get());
		set(tmp);
		return this;
	}
	
	public Vector getNormal(Vector v2) {
		if (isParallel(v2))
			throw new IllegalArgumentException("Parallel vectors in getNormal");
		Vector n = new Vector(this);
		n.cross(v2);
		n.normalize();
		return n;
	}
	
	
	public Vector add(Vector v2) {
		Rn.add(vec, vec, v2.vec);
		return this;
	}
	
	
	public Vector subtract(Vector v2) {
		Rn.subtract(vec, vec, v2.vec);
		return this;
	}
	
	public Vector makeOrthogonalTo(Vector v2) {
		if (isParallel(v2))
			throw new IllegalArgumentException("Parallel vectors in makeOrthogonal");
		Vector tmp = new Vector(v2);
		tmp.times(this.dot(v2) / v2.getLengthSquared());
		subtract(tmp);
		return this;
	}
	
	
	public Vector getBisection(Vector v2) {
		Vector result = new Vector(this);
		double alpha = getAngle(v2);
		Vector n = getNormal(v2);
		euclidean().rotate(alpha / 2, n.vec).getMatrix().transformVector(result.vec);
		return result;
	}
	
	
	public Point asPoint() {
		return new Point(vec);
	}
	
	public Vector projectOnto(Vector v){
		double scale = dot(v)/v.getLengthSquared();
		set(v).times(scale);
		return this;
	}
	
	public boolean isParallel(Vector v2, double eps) {
		double l = getLength();
		double l2 = v2.getLength();
		double s = dot(v2) / l / l2;
		return Math.abs(s) > 1.0 - eps;
	}
	
	public boolean isParallel(Vector v2) {
		return isParallel(v2, 1E-10);
	}
	
	
	public boolean isEqual(Vector v2, double eps) {
		return euclideanDistance(vec, v2.vec) <= eps;
	}
	
	public boolean isEqual(Vector v2) {
		return isEqual(v2, 1E-10);
	}
	
	@Override
	public boolean equals(Object obj) {
		return isEqual((Vector)obj);
	}
	
	
	
	@Override
	public String toString() {
		return "Vector: " + Arrays.toString(vec);
	}
	
	public JRVector getJR(AppearanceContext context) {
		return new JRVector(this, context);
	}
	
	
	public static void main(String[] args) {
		AppearanceContext context = new AppearanceContext();
		AppearanceContext normalContext = new AppearanceContext();
		
		Vector v1 = new Vector(1,1,1);
		Vector v2 = new Vector(2,1,4);
		SceneGraphComponent root = new SceneGraphComponent();
		root.addChild(v1.getJR(context));
		root.addChild(v2.getJR(context));
		root.addChild(v1.getNormal(v2).getJR(normalContext));
		root.addChild(v1.getBisection(v2).getJR(context));
		JRViewer.display(root);
	}
	
	
}
