package de.varylab.varylab.plugin.nurbs.scene;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.varylab.varylab.plugin.interaction.ConstrainedDraggablePointComponent;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.data.SignedUV;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.VectorFields;

public class DraggableIntegralNurbsCurves extends ConstrainedDraggablePointComponent<NurbsSurfaceConstraint> {
	
	private Map<VectorFields, PolygonalLine>
		vfLineMap = new LinkedHashMap<>(2);

	private List<DraggableIntegralNurbsCurves> commonCurves = null;
	
	private IntegralCurveFactory icf = null;
	
	private double sign = 1.0;
	
	private final SignedUV initialUV;

	public DraggableIntegralNurbsCurves(NURBSSurface surface, IntegralCurveFactory icf, SignedUV uv) {
		super(surface.getSurfacePoint(uv.getPoint()));
		constraint = new NurbsSurfaceConstraint(surface);
		setConstraint(constraint);
		this.initialUV = uv;
		this.icf = icf.getCopy();
		createDraggablePoint(surface, uv.getPoint());
		recomputeCurves(coords);
		updateComponent();
	}
	
	public void setSign(double sign){
		this.sign = sign;
	}
	
	public double getSign(){
		return sign;
	}
	
	public void createDraggablePoint( NURBSSurface surface, double[] uv){
		Appearance Ap = new Appearance();
		Ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		setAppearance(Ap);
		DefaultGeometryShader idgs = ShaderUtility.createDefaultGeometryShader(Ap, false);
		DefaultPointShader ipointShader = (DefaultPointShader)idgs.getPointShader();
		ipointShader.setDiffuseColor(Color.orange);
	}
	
	public void recomputeCurves(double[] p) {
		updateCoords(p);
		double[] uv = constraint.getUV();
		vfLineMap.clear();
		switch (icf.getVectorFields()) {
		case FIRST:
			vfLineMap.put(VectorFields.FIRST,icf.curveLine(uv, VectorFields.FIRST));
			break;
		case SECOND:
			vfLineMap.put(VectorFields.SECOND,icf.curveLine(uv, VectorFields.SECOND));
			break;
		case BOTH:
			vfLineMap.put(VectorFields.FIRST,icf.curveLine(uv, VectorFields.FIRST));
			vfLineMap.put(VectorFields.SECOND,icf.curveLine(uv, VectorFields.SECOND));
			break;
		}
	}
	
	public void setCommonCurves(List<DraggableIntegralNurbsCurves> commonCurves) {
		this.commonCurves = commonCurves;
	}

	public List<DraggableIntegralNurbsCurves> getCommonCurves() {
		return commonCurves;
	}

	public Collection<PolygonalLine> getPolygonalLines() {
		return vfLineMap.values();
	}

	public SignedUV getInitialUV() {
		return initialUV;
	}

	@Override
	public void updateComponent() {
		super.updateComponent();
		removeAllChildren();
		if(icf != null) {
		switch (icf.getVectorFields()) {
		case FIRST:
			addChild(createSceneGraphComponent(vfLineMap.get(VectorFields.FIRST)));
			break;
		case SECOND:
			addChild(createSceneGraphComponent(vfLineMap.get(VectorFields.SECOND)));
			break;
		case BOTH:
			addChild(createSceneGraphComponent(vfLineMap.get(VectorFields.FIRST)));
			addChild(createSceneGraphComponent(vfLineMap.get(VectorFields.SECOND)));
			break;
		}
		}
	}
	
	public SceneGraphComponent createSceneGraphComponent(PolygonalLine pl) {
		NURBSSurface surface = constraint.getSurface();
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
		lineApp.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		lineApp.setAttribute(CommonAttributes.EDGE_DRAW, true);
		sgc.setAppearance(lineApp);
		return sgc;
	}

	public VectorFields getVectorFields() {
		return icf.getVectorFields();
	}

	public void setVectorFields(VectorFields vf) {
		icf.setVectorFields(vf);
		updateComponent();
	}

	public double getTol() {
		return icf.getTol();
	}

	public void setTol(double d) {
		icf.setTol(d);
	}

}