package de.varylab.varylab.math.geom3d;


public class Basis implements Geom3D {

	private Vector
		x = new Vector(1, 0, 0),
		y = new Vector(0, 1, 0),
		z = new Vector(0, 0, 1);
	
	/**
	 * Construct the standard basis
	 */
	public Basis() {
	}
	
	/**
	 * Construct a basis from three vectors. No check for linear 
	 * independence is performed.
	 * @param x
	 * @param y
	 * @param z
	 */
	public Basis(Vector x, Vector y, Vector z) {
		setX(x);
		setY(y);
		setZ(z);
	}
	
	public Vector getX() {
		return x;
	}
	
	public Basis setX(Vector x) {
		this.x.set(x);
		return this;
	}
	
	public Vector getY() {
		return y;
	}
	
	public Basis setY(Vector y) {
		this.y.set(y);
		return this;
	}
	
	public Vector getZ() {
		return z;
	}
	
	public Basis setZ(Vector z) {
		this.z.set(z);
		return this;
	}
	
	
	public Basis times(double factor) {
		x.times(factor);
		y.times(factor);
		z.times(factor);
		return this;
	}
	
	
	public double getDeterminant() {
		return x.x()*y.y()*z.z() + y.x()*z.y()*x.z() + z.x()*x.y()*y.z() 
			 - z.x()*y.y()*x.z() - x.x()*z.y()*y.z() - z.z()*y.x()*x.y();
	}
	
	
	@Override
	public String toString() {
		return "Basis: " + x + ", " + y + ", " + z;
	}

	public JRBasis getJR(AppearanceContext context) {
		return new JRBasis(this, context);
	}
}
