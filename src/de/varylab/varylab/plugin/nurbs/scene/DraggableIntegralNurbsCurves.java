package de.varylab.varylab.plugin.nurbs.scene;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.varylab.varylab.plugin.interaction.ConstrainedDraggablePointComponent;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurve;

public class DraggableIntegralNurbsCurves extends ConstrainedDraggablePointComponent<NurbsSurfaceConstraint> {
	
	private List<PolygonalLine> polygonalLines;			

	private List<DraggableIntegralNurbsCurves> commonCurves = null;
	
	LinkedList<Integer> indexList;
	
	private IntegralCurve icf = null;
	
	private double[] initialUV = null;

	public DraggableIntegralNurbsCurves(NURBSSurface surface, IntegralCurve icf, double[] uv) {
		super(surface.getSurfacePoint(uv));
		constraint = new NurbsSurfaceConstraint(surface);
		setConstraint(constraint);
		initialUV = uv;
		this.icf = icf;
		createDraggablePoint(surface, uv);
		polygonalLines = icf.computeIntegralLine(uv);
		indexList = new LinkedList<>();
		for (PolygonalLine pl : polygonalLines) {
			indexList.add(pl.getCurveIndex());
		}
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
		polygonalLines = icf.computeIntegralLine(constraint.getUV());
	}
	
	public void setCommonCurves(List<DraggableIntegralNurbsCurves> commonCurves) {
		this.commonCurves = commonCurves;
	}

	public List<DraggableIntegralNurbsCurves> getCommonCurves() {
		return commonCurves;
	}

	public List<PolygonalLine> getPolygonalLines() {
		return polygonalLines;
	}

	public double[] getInitialUV() {
		return initialUV;
	}
	
}