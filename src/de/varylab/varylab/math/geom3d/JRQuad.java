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

public class JRQuad extends JRGeom3D {

	protected JRQuad(Quad q, AppearanceContext context) {
		createScene(q, context);
	}
	
	private void createScene(Quad q, AppearanceContext context) {
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(4);
		ifsf.setFaceCount(1);
		double[][] verts = new double[4][];
		verts[0] = q.getA().vec;
		verts[1] = q.getB().vec;
		verts[2] = q.getC().vec;
		verts[3] = q.getD().vec;
		int[][] faces = new int[][] {{0, 1, 2, 3}};
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
