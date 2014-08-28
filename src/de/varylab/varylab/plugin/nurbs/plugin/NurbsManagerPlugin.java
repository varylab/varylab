package de.varylab.varylab.plugin.nurbs.plugin;

import static java.awt.GridBagConstraints.BOTH;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.misc.VertexEditorPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsWeightAdapter;
import de.varylab.varylab.plugin.nurbs.algorithm.ComputeVectorFields;
import de.varylab.varylab.plugin.nurbs.algorithm.ExtractControlMesh;
import de.varylab.varylab.plugin.nurbs.algorithm.LinearDeformation;
import de.varylab.varylab.plugin.nurbs.algorithm.NurbsSurfaceFromMesh;
import de.varylab.varylab.plugin.nurbs.algorithm.ProjectToNurbsSurface;
import de.varylab.varylab.plugin.nurbs.algorithm.SplitAtEdge;
import de.varylab.varylab.plugin.nurbs.algorithm.SplitInTheMiddle;
import de.varylab.varylab.plugin.nurbs.algorithm.StretchXYZ;
import de.varylab.varylab.plugin.nurbs.algorithm.UVUnroll;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurvesOriginal;
import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;
import de.varylab.varylab.plugin.nurbs.type.NurbsUVCoordinate;

public class NurbsManagerPlugin extends ShrinkPanelPlugin implements HalfedgeListener {
	
	private static Logger logger = Logger.getLogger(NurbsManagerPlugin.class.getName());
	
	private HalfedgeInterface 
		hif = null;

	private GeodesicPanel
		geodesicPanel = new GeodesicPanel();
	
	private NURBSSurface activeNurbsSurface;

	public NurbsManagerPlugin() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
		

		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new GridLayout());
		
		c.weighty = 1.0;
		shrinkPanel.add(tablePanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(geodesicPanel, c);
	}


	private class GeodesicPanel extends ShrinkPanel implements ActionListener{
		
		private static final long 
			serialVersionUID = 1L;
		private SpinnerNumberModel
			tolExpModel = new SpinnerNumberModel(-3, -30.0, 0, 1),
			epsExpModel = new SpinnerNumberModel(-2, -30.0, 0, 1),
			nearbyModel = new SpinnerNumberModel(-3, -30.0, 0, 1);
		private JSpinner
			tolSpinner = new JSpinner(tolExpModel),
			epsSpinner = new JSpinner(epsExpModel),
			nearbySpinner = new JSpinner(nearbyModel);
		private JButton
			goButton = new JButton("Go");
		private JRadioButton
			segmentButton = new JRadioButton("Geodesic Segment");
		
		
		public GeodesicPanel() {
			super("Geodesic");
			setShrinked(true);
			setLayout(new GridBagLayout());
			GridBagConstraints lc = LayoutFactory.createLeftConstraint();
			GridBagConstraints rc = LayoutFactory.createRightConstraint();
			add(new JLabel("Tolerance Exp"), lc);
			add(tolSpinner, rc);
			add(new JLabel("Eps Exp"), lc);
			add(epsSpinner, rc);
			add(new JLabel("nearby target Exp"), lc);
			add(nearbySpinner, rc);
			add(segmentButton, rc);
			add(goButton, rc);
			goButton.addActionListener(this);
			JScrollPane curvePanel = new JScrollPane(new JLabel("all curves"));
			curvePanel.setToolTipText("teyety");
			add(curvePanel);
		}
		
		@Override
		public void actionPerformed(ActionEvent e){
			double tol = tolExpModel.getNumber().doubleValue();
			tol = Math.pow(10, tol);
			double eps = epsExpModel.getNumber().doubleValue();
			eps = Math.pow(10, eps);
			double nearby = nearbyModel.getNumber().doubleValue();
			nearby = Math.pow(10, nearby);
			
			Set<Vertex<?,?,?>> verts = hif.getSelection().getVertices();
			AdapterSet as = hif.getAdapters();
			double[] a = new double [2];
			double[] b = new double [2];
			if(verts.size() != 2){
				logger.info("Select only two vertices!");
			}else{
				int index = 0;
				for (Vertex<?,?,?> v : verts) {
					index = index + 1;
					if(index == 1){
						a = as.getD(NurbsUVCoordinate.class, v);
					}else{
						b = as.getD(NurbsUVCoordinate.class, v);
					}
				}
				
			if(segmentButton.isSelected()){
				LinkedList<double[]> points = IntegralCurvesOriginal.geodesicSegmentBetweenTwoPoints(activeNurbsSurface, a, b, eps, tol, nearby);
				PointSetFactory psf = new PointSetFactory();
				int p = activeNurbsSurface.getUDegree();
				int q = activeNurbsSurface.getVDegree();
				double[] U = activeNurbsSurface.getUKnotVector();
				double[] V = activeNurbsSurface.getVKnotVector();
				double[][][]Pw = activeNurbsSurface.getControlMesh();
				double[][] u = new double[points.size()][];
				double[][] surfacePoints = new double[points.size()][];
				for (int i = 0; i < u.length; i++) {
					u[i] = points.get(i);
				}
				psf.setVertexCount(u.length);
				for (int i = 0; i < u.length; i++) {
					double[] S = new double[4];
					NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, u[i][0], u[i][1], S);
					surfacePoints[i] = S;
				}
				psf.setVertexCoordinates(surfacePoints);
				psf.update();
				SceneGraphComponent sgc = new SceneGraphComponent("geodesic segment");
				SceneGraphComponent minCurveComp = new SceneGraphComponent("Geodesic Segment");
				sgc.addChild(minCurveComp);
				sgc.setGeometry(psf.getGeometry());
				Appearance labelAp = new Appearance();
				sgc.setAppearance(labelAp);
				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(labelAp, false);
				DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
				pointShader.setDiffuseColor(Color.orange);
				hif.getActiveLayer().addTemporaryGeometry(sgc);
				double length = 0;
				for (int i = 0; i < surfacePoints.length - 1; i++) {
					double [] realPoint1 = new double[3];
					realPoint1[0] = surfacePoints[i][0];
					realPoint1[1] = surfacePoints[i][1];
					realPoint1[2] = surfacePoints[i][2];
					double [] realPoint2 = new double[3];
					realPoint2[0] = surfacePoints[i + 1][0];
					realPoint2[1] = surfacePoints[i + 1][1];
					realPoint2[2] = surfacePoints[i + 1][2];
					length = length + Rn.euclideanDistance(realPoint1, realPoint2);
				}
				logger.info("Geodesic segment length: " + length);
			}else{
				LinkedList<double[]> points = IntegralCurvesOriginal.geodesicExponentialGivenByTwoPoints(activeNurbsSurface, a, b, eps, tol,nearby);
				points.addAll(IntegralCurvesOriginal.geodesicExponentialGivenByTwoPoints(activeNurbsSurface, b, a, eps, tol, nearby));
				PointSetFactory psf = new PointSetFactory();
				int p = activeNurbsSurface.getUDegree();
				int q = activeNurbsSurface.getVDegree();
				double[] U = activeNurbsSurface.getUKnotVector();
				double[] V = activeNurbsSurface.getVKnotVector();
				double[][][]Pw = activeNurbsSurface.getControlMesh();
				double[][] u = new double[points.size()][];
				double[][] surfacePoints = new double[points.size()][];
				for (int i = 0; i < u.length; i++) {
					u[i] = points.get(i);
				}
				psf.setVertexCount(u.length);
				for (int i = 0; i < u.length; i++) {
					double[] S = new double[4];
					NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, u[i][0], u[i][1], S);
					surfacePoints[i] = S;
				}
				psf.setVertexCoordinates(surfacePoints);
				psf.update();
				SceneGraphComponent sgc = new SceneGraphComponent("geodesic segment");
				SceneGraphComponent minCurveComp = new SceneGraphComponent("Geodesic Segment");
				sgc.addChild(minCurveComp);
				sgc.setGeometry(psf.getGeometry());
				Appearance labelAp = new Appearance();
				sgc.setAppearance(labelAp);
				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(labelAp, false);
				DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
				pointShader.setDiffuseColor(Color.orange);
				hif.getActiveLayer().addTemporaryGeometry(sgc);
				
			}
			}
		}
	}
	

	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addAdapter(new NurbsWeightAdapter(), true);
		hif.addHalfedgeListener(this);
		
		c.getPlugin(NurbsIOPlugin.class);
		c.getPlugin(IntegralCurvesPlugin.class);
		c.getPlugin(SurfaceProjectionPlugin.class);
		
		c.getPlugin(ExtractControlMesh.class);
		c.getPlugin(NurbsSurfaceFromMesh.class);
		c.getPlugin(ProjectToNurbsSurface.class);
		
		c.getPlugin(VertexEditorPlugin.class);
		c.getPlugin(QuadMeshGenerator.class);
		c.getPlugin(ComputeVectorFields.class);
		c.getPlugin(UVUnroll.class);
		c.getPlugin(SplitInTheMiddle.class);
		c.getPlugin(SplitAtEdge.class);
		c.getPlugin(StretchXYZ.class);
		c.getPlugin(LinearDeformation.class);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Nurbs Manager", "Nurbs Team");
		return info;
	}

	@SuppressWarnings("unused")
	private void addUmbilicalPoints(double[][] umbillicPoints, HalfedgeLayer layer) {
		PointSetFactory psfu = new PointSetFactory();
	
		psfu.setVertexCount(umbillicPoints.length);
	
		psfu.setVertexCoordinates(umbillicPoints);
		psfu.update();
		SceneGraphComponent sgcu = new SceneGraphComponent("Umbilics");
		SceneGraphComponent umbilicComp = new SceneGraphComponent("Max Curve");
		sgcu.addChild(umbilicComp);
		sgcu.setGeometry(psfu.getGeometry());
		Appearance uAp = new Appearance();
		sgcu.setAppearance(uAp);
		DefaultGeometryShader udgs = ShaderUtility.createDefaultGeometryShader(uAp, false);
		DefaultPointShader upointShader = (DefaultPointShader)udgs.getPointShader();
		upointShader.setDiffuseColor(Color.GREEN);
		layer.addTemporaryGeometry(sgcu);
		
	}

	private void updateActiveNurbsSurface(HalfedgeLayer layer) {
		AdapterSet as = layer.getAdapters();
		as.addAll(layer.getVolatileAdapters());
		NurbsUVAdapter nurbsUVAdapter = as.query(NurbsUVAdapter.class);
		if(nurbsUVAdapter == null) {
			nurbsUVAdapter = layer.getActiveAdapters().query(NurbsUVAdapter.class);
		}
		
		if(nurbsUVAdapter != null) {
			activeNurbsSurface = nurbsUVAdapter.getSurface();
			// TODO: this should not be needed.
//			double[][] umbilicalPoints = computeUmbilicalPoints();
//			
//			if(umbilicalPoints.length>0){
//				addUmbilicalPoints(umbilicalPoints,hif.getActiveLayer());
//			}
		} else {
			activeNurbsSurface = null;
		}
	}
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addContentUI();
		v.addBasicUI();
		v.registerPlugin(new NurbsManagerPlugin());
		v.startup();
	}
	
	@Override
	public void dataChanged(HalfedgeLayer layer) {
		updateActiveNurbsSurface(layer);
		
	}

	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
		updateActiveNurbsSurface(layer);
	}

	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		if(old == active) {
			return;
		}
		updateActiveNurbsSurface(active);
	}

	@Override
	public void layerCreated(HalfedgeLayer layer) {
	}

	@Override
	public void layerRemoved(HalfedgeLayer layer) {
	}

	public NURBSSurface getActiveNurbsSurface() {
		return activeNurbsSurface;
	}
}
