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
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;

public class JRLine extends JRGeom3D {

	
	protected JRLine(Line l, AppearanceContext context) {
		createScene(l,context);
	}

	private void createScene(Line l, AppearanceContext context) {
		Vector dir =  new Vector(Rn.crossProduct(null, l.plane1.n.get(), l.plane2.n.get()));		
		
		Plane orth = new Plane(dir,0);
		Point start = l.planeIntersection(orth);
		Appearance app = new Appearance();
		app.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, context.getLineColor());
		app.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, context.getLineColor());
		app.setAttribute(VERTEX_DRAW, true);
		app.setAttribute(FACE_DRAW, false);
		app.setAttribute(EDGE_DRAW, true);
		app.setAttribute(SPHERES_DRAW, true);
		app.setAttribute(TUBES_DRAW, true);
		app.setAttribute(POINT_RADIUS, 0.05*context.getScale());
		app.setAttribute(TUBE_RADIUS, 0.05*context.getScale());
		setAppearance(app);
		setGeometry(Primitives.arrow(0, 0, 200, 0, 0.0));
		
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.rotateFromTo(new double[] {1, 0, 0}, dir.vec);
		mb.translate(Rn.add(null, Rn.times(null, 100, dir.get()), start.get()));
		mb.assignTo(this);
	}
	
//	public static void main(String[] args) {
//		SceneGraphComponent root = new SceneGraphComponent();
//		AppearanceContext context = new AppearanceContext();
//		context.getLineAppearance().setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.CYAN );
//		context.getLineAppearance().setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.CYAN);
//		Line l = new Line(new Point(0,0,0),new Vector(1,0,0));
//		root.addChild(l.getJR(context));
//		Plane p1 = new Plane(new Vector(0,1,0),0);
//		Plane p2 = new Plane(new Vector(0,0,1),0);
//		Line l2 = new Line(p1,p2);
//		root.addChild(l2.getJR(new AppearanceContext()));
//		Point p = new Point(0,0,0);
//		root.addChild(p.getJR(new AppearanceContext()));
//		ViewerApp.display(root);
//	}
	

}
