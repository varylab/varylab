package de.varylab.varylab.math.geom3d;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_SIZE;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;

public class JRCircle extends JRGeom3D {

	private static Geometry
		circleGeometry = Primitives.regularPolygon(120);
	
	protected JRCircle(Circle c, AppearanceContext context){
		super("Circle");
		createScene(c, context);
		context.getObjects().add(this);
	}
	
	private void createScene(Circle c, AppearanceContext context) {
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.translate(c.center.vec);
		mb.scale(c.radius);
		double[] z = {0, 0, 1};
		mb.rotateFromTo(z, c.plane.n.vec);
		mb.assignTo(this);
		
		setGeometry(circleGeometry);
		Appearance app = new Appearance();
		app.setAttribute(RADII_WORLD_COORDINATES, true);
		app.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, context.getCircleColor());
		app.setAttribute(POINT_SHADER + "." + SMOOTH_SHADING, true);
		app.setAttribute(POINT_SHADER + "." + POINT_RADIUS, 0.01*context.getScale());
		app.setAttribute(POINT_SHADER + "." + POINT_SIZE, 0.05*context.getScale());
		app.setAttribute(POINT_SHADER + "." + SPHERES_DRAW, true);
		app.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, context.getCircleColor());
		app.setAttribute(LINE_SHADER + "." + SMOOTH_SHADING, true);
		app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, 0.05*context.getScale());
//		app.setAttribute(LINE_SHADER + "." + LINE_WIDTH, 3);
		app.setAttribute(TUBES_DRAW, false);
		app.setAttribute(SPHERES_DRAW, true);
		app.setAttribute(VERTEX_DRAW, false);
		app.setAttribute(EDGE_DRAW, true);
		app.setAttribute(FACE_DRAW, false);
		setAppearance(app);
	}	

}
