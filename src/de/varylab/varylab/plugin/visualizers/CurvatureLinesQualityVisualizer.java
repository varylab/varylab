package de.varylab.varylab.plugin.visualizers;

import static de.jtem.halfedge.util.HalfEdgeUtils.isBoundaryEdge;
import static java.lang.Math.PI;
import static java.lang.Math.abs;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.ui.AppearanceInspector;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.CurvatureField;
import de.jtem.halfedgetools.adapter.type.CurvatureFieldMax;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition4d;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class CurvatureLinesQualityVisualizer extends VisualizerPlugin {

	private ContentAppearance
		contentAppearance = null;
	private JPanel
		panel = new JPanel();
	private CurvatureLineQualityAdapter
		curvatureLineQualityAdapter = new CurvatureLineQualityAdapter();
	private Matrix
		texMatrix = new Matrix();
	private double[]
	    xAxis = {1,0,0,0};
	
	public CurvatureLinesQualityVisualizer() {
		panel.setLayout(new GridBagLayout());
	}
	
	
	private class CurvatureLineQualityAdapter extends AbstractAdapter<Double> {

		public CurvatureLineQualityAdapter() {
			super(Double.class, true, false);
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getE(E e, AdapterSet a) {
			if (!a.isAvailable(CurvatureField.class, Edge.class, double[].class)) {
				return -1.0;
			}
			if (isBoundaryEdge(e)) {
				return 0.0;
			}
			AppearanceInspector ai = contentAppearance.getAppearanceInspector();
			texMatrix = ai.getTextureMatrix();
			V s = e.getStartVertex();
			V t = e.getTargetVertex();
			double[] texS = a.getD(TexturePosition4d.class, s).clone();
			double[] texT = a.getD(TexturePosition4d.class, t).clone();
			texMatrix.transformVector(texS);
			texMatrix.transformVector(texT);
			Pn.dehomogenize(texS, texS);
			Pn.dehomogenize(texT, texT);
			double[] texVec = Rn.subtract(null, texT, texS);
			double alphaTex = Rn.euclideanAngle(texVec, xAxis);
			if (alphaTex > PI/2) {
				alphaTex = PI - alphaTex;
			}
			
			double[] xVec = a.getD(CurvatureFieldMax.class, e);
			double[] eVec = a.getD(EdgeVector.class, e);
			double alphaSurf = Rn.euclideanAngle(xVec, eVec);
			if (alphaSurf > PI/2) {
				alphaSurf = PI - alphaSurf;
			}
			return abs(alphaSurf - alphaTex);
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Edge.class.isAssignableFrom(nodeClass);
		}
		
		
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		contentAppearance = c.getPlugin(ContentAppearance.class);
	}
	
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	@Override
	public String getName() {
		return "Curvature Line Quality";
	}

	@Override
	public AdapterSet getAdapters() {
		AdapterSet result = new AdapterSet();
		result.add(curvatureLineQualityAdapter);
		return result;
	}
}
