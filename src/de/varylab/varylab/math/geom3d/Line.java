package de.varylab.varylab.math.geom3d;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;

public class Line implements Geom3D {

	protected Plane
	    plane1 = new Plane(0, 1, 0, 0),
	    plane2 = new Plane(0, 0, 1, 0);
	
	
	public Line() {
	}
	
	public Line(Point p, Vector d) {
		Vector o = new Vector(-d.vec[1],d.vec[2],-d.vec[0]);
		o.makeOrthogonalTo(d);
		plane1 = new Plane(o,p);
		plane2 = new Plane(o.getNormal(d),p);
	}
	
	public Line(Plane p1, Plane p2) throws IllegalArgumentException {
		if (p1.isParallel(p2)) {
			throw new IllegalArgumentException("Planes are parallel");
		}
		plane1 = p1;
		plane2 = p2;
	}
	
	
	public boolean isOnLine(Point p) {
		return plane1.isInPlane(p) && plane2.isInPlane(p);
	}
	
	/**
	 * calculates the intersection of this line and a given plane, if
	 * there is one.
	 * 
	 * @param plane3, the plane which is intersected by this line
	 * @return the intersection point
	 * @throws IllegalArgumentException
	 */
	public Point planeIntersection(Plane plane3)throws IllegalArgumentException{
		if(plane1.isParallel(plane3)||plane2.isParallel(plane3))
			throw new IllegalArgumentException("At least one plane is parallel");
		
		DenseVector d = new DenseVector(new double[]{-plane1.d,-plane2.d,-plane3.d});
		DenseMatrix A = new DenseMatrix(new double[][]{
				{plane1.n.get()[0],plane1.n.get()[1],plane1.n.get()[2]},
				{plane2.n.get()[0],plane2.n.get()[1],plane2.n.get()[2]},
				{plane3.n.get()[0],plane3.n.get()[1],plane3.n.get()[2]}});
		DenseVector x = new DenseVector(3);
		A.solve(d, x);
		return new Point(x.get(0),x.get(1),x.get(2));
	}
	
	public boolean intersectsTriangle(Triangle t){
		 
		Point p = this.planeIntersection(t.getPlane());
		return t.isInTriangle(p);
	}

	public JRLine getJR(AppearanceContext context) {
		return new JRLine(this, context);
	}
}
