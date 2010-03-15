package de.varylab.varylab.math.geom3d;

import de.jreality.math.Matrix;


public class Quad implements Geom3D {
		private Point[] 
			x = new Point[4];

		public Quad(){
			x[0] = new Point();
			x[1] = new Point();
			x[2] = new Point();
			x[3] = new Point();
		}	

		public Quad(Point a, Point b, Point c,Point d){
			x[0] = new Point(a);
			x[1] = new Point(b);
			x[2] = new Point(c);
			x[3] = new Point(d);
		}
		
		public Quad(Quad q) {
			this(q.getA(), q.getB(), q.getC(), q.getD());
		}

		public Point getA() {
			return x[0];
		}

		public void setA(Point a) {
			x[0] = new Point(a);
		}

		public Point getB() {
			return x[1];
		}

		public void setB(Point b) {
			x[1] = new Point(b);
		}		

		public Point getC() {
			return x[2];
		}

		public void setC(Point c) {
			x[2] = new Point(c);
		}		

		public Point getD() {
			return x[3];
		}

		public void setD(Point d) {
			x[3] = new Point(d);
		}
		
		public Point get(int i) {
			return x[i % 4];
		}
		
		public boolean isPlanar() {
			Matrix M = new Matrix();
			M.setColumn(0, x[0].vec);
			M.setColumn(1, x[1].vec);
			M.setColumn(2, x[2].vec);
			M.setColumn(3, x[3].vec);
			M.setRow(3, new double[]{1,1,1,1});
			return M.getDeterminant() < 1E-5;
		}
		
		public Vector getNormal() {
			if (!isPlanar())
				throw new RuntimeException("Quad is not planar in getNormal()");
			Vector ab = new Vector(x[1]).subtract(x[0]).normalize();
			Vector ad = new Vector(x[3]).subtract(x[0]).normalize();
			return ab.cross(ad);
		}
		
		
		public boolean isQuad(){
			double eps = 1E-7;
			return !(x[0].equals(x[1],eps)||x[0].equals(x[2],eps)||x[0].equals(x[3],eps)||
					x[1].equals(x[2], eps)||x[1].equals(x[3], eps)||
					x[2].equals(x[3], eps));
		}
		@Override
		public String toString() {
	
			return new String("Point a: "+ x[0].toString() +"\n"+
							   "Point b: "+ x[1].toString() +"\n"+ 	
				         	   "Point c: "+ x[2].toString() +"\n"+
					           "Point d: "+ x[3].toString() +"\n");
		}

		public JRGeom3D getJR(AppearanceContext context) {
			return new JRQuad(this, context);
		}
		
		/*
		 * the Quad is supposed to have a negative cross ratio
		 */
		public double computeArea(){
			Triangle t1 = new Triangle(x[0],x[1],x[2]);
			Triangle t2 = new Triangle(x[0],x[1],x[3]);
			return t1.computeArea()+t2.computeArea();
		}
		
		public Point getCenter(){
			Point center = new Point();
			for (int i = 0; i < x.length; i++)
				center.add(x[i]);
			return center.times(0.25).asPoint();
		}
		
}
