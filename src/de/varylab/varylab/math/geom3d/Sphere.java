package de.varylab.varylab.math.geom3d;


public class Sphere implements Geom3D {
	
	protected Point center = new Point(0.0,0.0,0.0);
	protected double radius = 1;
	
	public Sphere (){
		
	}
	public Sphere(Point center, double radius){
		this.center = center;
		this.radius = radius;
	}
	
	public JRSphere getJR(AppearanceContext context) {
		return new JRSphere(this, context);
	}
	
}
