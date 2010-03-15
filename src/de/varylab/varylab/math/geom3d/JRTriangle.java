package de.varylab.varylab.math.geom3d;

import static de.jreality.shader.CommonAttributes.AMBIENT_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.AMBIENT_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_SHADER;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.Appearance;

public class JRTriangle extends JRGeom3D {

	protected JRTriangle(Triangle t, AppearanceContext context) {
		createScene(t, context);
	}
	
	private void createScene(Triangle q, AppearanceContext context) {
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(3);
		ifsf.setFaceCount(1);
		double[][] verts = new double[3][];
		verts[0] = q.getA().vec;
		verts[1] = q.getB().vec;
		verts[2] = q.getC().vec;
		int[][] faces = new int[][] {{0, 1, 2}};
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceIndices(faces);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		setGeometry(ifsf.getIndexedFaceSet());
		Appearance app = new Appearance();
		app.setAttribute(VERTEX_SHADER + "." + AMBIENT_COLOR, context.getCircleColor());
		app.setAttribute(VERTEX_SHADER + "." + AMBIENT_COEFFICIENT, 1.0);
		app.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, context.getCircleColor());
		app.setAttribute(POINT_SHADER + "." + SMOOTH_SHADING, true);
		app.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, context.getCircleColor());
		app.setAttribute(LINE_SHADER + "." + SMOOTH_SHADING, true);
		app.setAttribute(VERTEX_DRAW, false);
		app.setAttribute(EDGE_DRAW, true);
		app.setAttribute(FACE_DRAW, true);
		app.setAttribute(SPHERES_DRAW, true);
		app.setAttribute(TUBES_DRAW, true);
		app.setAttribute(POINT_RADIUS, 0.01*context.getScale());
		app.setAttribute(TUBE_RADIUS, 0.01*context.getScale());
		setAppearance(app);
	}
	
}
