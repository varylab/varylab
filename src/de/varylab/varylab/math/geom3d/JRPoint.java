package de.varylab.varylab.math.geom3d;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.util.SceneGraphUtility;

public class JRPoint extends JRVector {

	
	
	protected JRPoint(Point p, AppearanceContext context) {
		super(p, context);
		setName("Point");
		SceneGraphUtility.removeChildren(this);
		createScene(p, context);
	}
	
	private void createScene(Point p,AppearanceContext context){
		Appearance app = new Appearance();
		app.setAttribute(RADII_WORLD_COORDINATES, true);
		app.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR,context.getPointColor());
		app.setAttribute(POINT_SHADER + "." + SMOOTH_SHADING, true);
		app.setAttribute(POINT_SHADER + "." + POINT_RADIUS, context.getScale());
		app.setAttribute(SPHERES_DRAW, true);
		app.setAttribute(VERTEX_DRAW, true);
		setAppearance(app);
		setGeometry(Primitives.point(p.vec));
	}
	
	//test usage only
//	public static void main(String[] args) {
//		SceneGraphComponent root = new SceneGraphComponent();
//		
//		AppearanceContext c = new AppearanceContext();
//		c.getPointAppearance().setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR,RED);
//		c.getPointAppearance().setAttribute(POINT_SHADER + "." + SMOOTH_SHADING, true);
//		Point p3 = new Point(1,2,-1);
//		root.addChild(p3.getJR(c));
//		
//		ViewerApp.display(root);
//	}
}
