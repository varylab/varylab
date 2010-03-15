package de.varylab.varylab.math.geom3d;


import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;

public class JRBasis extends JRGeom3D {

	
	protected JRBasis(Basis b, AppearanceContext context) {
			
			createScene(b, context);
			context.getObjects().add(this);
			
	}
	
	private void createScene(Basis b,AppearanceContext context) {
		JRVector xVec = b.getX().getJR(buildXContext(context.getScale()));
		JRVector yVec = b.getY().getJR(buildYContext(context.getScale()));
		JRVector zVec = b.getZ().getJR(buildZContext(context.getScale()));
		
		addChild(xVec);
		addChild(yVec);
		addChild(zVec);
	}
	
	private AppearanceContext buildXContext(double scale){
		AppearanceContext app = new AppearanceContext(scale);
		app.setVectorColor(RED);
		return app;
	}
	private AppearanceContext buildYContext(double scale){
		AppearanceContext app = new AppearanceContext(scale);
		app.setVectorColor(GREEN);
		return app;
	}
	private AppearanceContext buildZContext(double scale){
		AppearanceContext app = new AppearanceContext(scale);
		app.setVectorColor(BLUE);
		return app;
	}
	
}
