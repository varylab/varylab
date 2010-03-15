package de.varylab.varylab.math.geom3d;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;

public class JRVector extends JRGeom3D {
	
	
	protected JRVector(Vector v, AppearanceContext context) {
		super("Vector");
		createScene(v,context);
		context.getObjects().add(this);
	}
	

	
	private void createScene(Vector v, AppearanceContext context) {
		SceneGraphComponent root = new SceneGraphComponent();
		Appearance app = new Appearance();
		app.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR,context.getVectorColor());
		app.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, context.getVectorColor());
		app.setAttribute(VERTEX_DRAW, true);
		app.setAttribute(FACE_DRAW, false);
		app.setAttribute(EDGE_DRAW, true);
		app.setAttribute(SPHERES_DRAW, true);
		app.setAttribute(TUBES_DRAW, true);
		app.setAttribute(POINT_RADIUS, 0.05*context.getScale());
		app.setAttribute(TUBE_RADIUS, 0.05*context.getScale());
		root.setAppearance(app);
		root.setGeometry(Primitives.arrow(0, 0, v.getLength()*context.getScale(), 0, 0.0));
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.rotateFromTo(new double[] {1, 0, 0}, v.vec);
		mb.assignTo(root);
		
		addChild(root);
	}
	
}
