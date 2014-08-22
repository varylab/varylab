package de.varylab.varylab.plugin.nurbs.scene;

import java.awt.Color;
import java.util.List;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;

public class PolygonalLineComponentProvider implements SceneGraphComponentProvider<PolygonalLine> {

	private NURBSSurface
		surface = null;
	
	@Override
	public SceneGraphComponent createSceneGraphComponent(PolygonalLine pl) {
		if(surface == null) {
			return null;
		}
		List<LineSegment> segments = pl.getpLine();
		double u,v;
		double[][] points = new double[segments.size()+1][];
		int i = 0;
		for(LineSegment segment : segments) {
			u = (segment.getSegment())[0][0];
			v = (segment.getSegment())[0][1];
			points[i] = surface.getSurfacePoint(u, v);
			++i;
			if(i == segments.size()) {
				u = (segment.getSegment())[1][0];
				v = (segment.getSegment())[1][1];
				points[i] = surface.getSurfacePoint(u, v);
			}
		}
		IndexedLineSetFactory lsf = IndexedLineSetUtility.createCurveFactoryFromPoints(points, false);
		lsf.update();
		boolean maxMin = pl.getDescription().toLowerCase().contains("max");
		SceneGraphComponent sgc = new SceneGraphComponent("Integral Curve:" + (maxMin?"max":"min")+" curvature");
		sgc.setGeometry(lsf.getGeometry());
		Appearance lineApp = new Appearance();
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(lineApp, false);
		DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
		DefaultLineShader lineShader = (DefaultLineShader)dgs.getLineShader();
		if(maxMin){
			pointShader.setDiffuseColor(Color.red);
			lineShader.setDiffuseColor(Color.red);
		} else {
			pointShader.setDiffuseColor(Color.cyan);
			lineShader.setDiffuseColor(Color.cyan);
		}
		sgc.setAppearance(lineApp);
		return sgc;
	}

	public void setSurface(NURBSSurface surface) {
		this.surface = surface;
	}
}
