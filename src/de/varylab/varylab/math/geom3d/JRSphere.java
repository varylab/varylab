package de.varylab.varylab.math.geom3d;

import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;

public class JRSphere extends JRGeom3D {

	private Appearance sphereAppearance = new Appearance();
	
	protected JRSphere(Sphere s, AppearanceContext context) {
			createScene(s,context);
	}

	private void createScene(Sphere s, AppearanceContext context) {
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.translate(s.center.vec);
		mb.scale(s.radius);
		mb.assignTo(this);

		sphereAppearance.setAttribute(VERTEX_DRAW,false);
		setGeometry(Primitives.sphere(30));
		setAppearance(sphereAppearance);
	}
	

}
