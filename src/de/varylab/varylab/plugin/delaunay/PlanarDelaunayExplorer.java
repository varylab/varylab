package de.varylab.varylab.plugin.delaunay;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.LayoutFactory;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.algorithm.computationalgeometry.ConvexHull;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.misc.VertexEditorPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.SelectionListener;
import de.jtem.halfedgetools.util.GeometryUtility;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.generator.RandomPointsUnitDisc;
import de.varylab.varylab.utilities.HalfedgeUtility;

public class PlanarDelaunayExplorer extends ShrinkPanelPlugin implements ActionListener, SelectionListener, HalfedgeListener {

	private JPanel
		panel = new JPanel();
	
	private ShrinkPanel
		stepPanel = new ShrinkPanel("Step by Step"),
		visualizationPanel = new ShrinkPanel("Visualization");
	
	private SpinnerNumberModel
		seedModel = new SpinnerNumberModel(123, 0, Integer.MAX_VALUE, 1);
	
	private JSpinner
		seedSpinner = new JSpinner(seedModel);
	
	private JButton
		generateRandomPointsButton = new JButton("Generate points in unit disc"),
		liftToHyperboloidButton = new JButton("Lift to hyperboloid"),
		calculateConvexHullButton = new JButton("Calculate convex hull"),
		extractHyperbolicFacesButton = new JButton("Extract hyperbolic faces"),
		projectToDiscButton = new JButton("Project to disc"),
		hyperbolicDelaunayButton = new JButton("Hyperbolic Delaunay"),
		euclideanDelaunayButton = new JButton("Euclidean Delaunay");

	private JCheckBox
		circlesBox = new JCheckBox("Show circles"),
		unitDiscBox = new JCheckBox("Show unit disc"),
		hyperboloidBox = new JCheckBox("Show hyperboloid"),
		paraboloidBox = new JCheckBox("Show paraboloid");
	
	private ButtonGroup
		modelGroup = new ButtonGroup();
	
	private JRadioButton
		poincareButton = new JRadioButton("Poincare model"),
		kleinButton = new JRadioButton("Klein model");
	
	private RandomPointsUnitDisc 
		pointsGenerator = null;

	private HalfedgeInterface 
		hif = null;

	private SceneGraphComponent 
		auxComponent = new SceneGraphComponent("Hyperbolic Delaunay Aux"),
		unitDiscComponent = new SceneGraphComponent("Unit disc"),
		circlesComponent = new SceneGraphComponent("Circumcircles"),
		hyperboloidComponent = new SceneGraphComponent("Hyperboloid"),
		paraboloidComponent = new SceneGraphComponent("Paraboloid"),
		poincareGeodesicsComponent = new SceneGraphComponent("Poincare");
	
	private Map<Face<?,?,?>, SceneGraphComponent>
		faceComponentMap = new HashMap<Face<?,?,?>, SceneGraphComponent>();

	private ContentAppearance contentAppearance = null;
	
	public PlanarDelaunayExplorer() {
		panel.setLayout(new GridBagLayout());
		visualizationPanel.setLayout(new GridBagLayout());
		stepPanel.setLayout(new GridBagLayout());
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		
		generateRandomPointsButton.addActionListener(this);
		liftToHyperboloidButton.addActionListener(this);
		calculateConvexHullButton.addActionListener(this);
		extractHyperbolicFacesButton.addActionListener(this);
		projectToDiscButton.addActionListener(this);
		hyperbolicDelaunayButton.addActionListener(this);
		euclideanDelaunayButton.addActionListener(this);
		paraboloidBox.addActionListener(this);
		
		
		stepPanel.add(new JLabel("Seed"),lc);
		stepPanel.add(seedSpinner,rc);
		stepPanel.add(generateRandomPointsButton,rc);
		stepPanel.add(liftToHyperboloidButton,rc);
		
		stepPanel.add(calculateConvexHullButton,rc);
		stepPanel.add(extractHyperbolicFacesButton,rc);
		stepPanel.add(projectToDiscButton,rc);
		stepPanel.setShrinked(true);
		
		circlesBox.addActionListener(this);
		unitDiscBox.addActionListener(this);
		hyperboloidBox.addActionListener(this);
		poincareButton.addActionListener(this);
		kleinButton.addActionListener(this);
		
		unitDiscBox.setSelected(true);
		visualizationPanel.add(unitDiscBox,rc);
		visualizationPanel.add(hyperboloidBox,rc);
		visualizationPanel.add(paraboloidBox,rc);
		visualizationPanel.add(circlesBox,rc);
		modelGroup.add(poincareButton);
		modelGroup.add(kleinButton);
		visualizationPanel.add(poincareButton,rc);
		poincareButton.setSelected(true);
		visualizationPanel.add(kleinButton, rc);

		panel.add(hyperbolicDelaunayButton,rc);
		panel.add(euclideanDelaunayButton,rc);
		panel.add(stepPanel,rc);
		panel.add(visualizationPanel,rc);
		
		shrinkPanel.add(panel);

		initUnitCircle();
		initHyperboloidComponent();
		initParaboloidComponent();
		initPoincareComponent();
		initCirclesComponent();
		initAuxComponent();
		
	}

	private void initParaboloidComponent() {
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute(CommonAttributes.EDGE_DRAW, false);
		app.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.SMOOTH_SHADING, true);
		app.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, Color.RED);
		paraboloidComponent.setAppearance(app);
	}

	private void initHyperboloidComponent() {
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute(CommonAttributes.EDGE_DRAW, false);
		app.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.SMOOTH_SHADING, true);
		hyperboloidComponent.setAppearance(app);
	}
	
	private void initPoincareComponent() {
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute(CommonAttributes.EDGE_DRAW, true);
		app.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.SMOOTH_SHADING, true);

		poincareGeodesicsComponent.setAppearance(app);
	}

	private void initAuxComponent() {
//		Appearance app = new Appearance();
//		app.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.PICKABLE, false);
//		app.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.PICKABLE, false);
//		app.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.PICKABLE, false);
//		auxComponent.setAppearance(app);
		auxComponent.setPickable(false);
		auxComponent.addChild(unitDiscComponent);
		auxComponent.addChild(circlesComponent);
		auxComponent.addChild(hyperboloidComponent);
		auxComponent.addChild(paraboloidComponent);
		auxComponent.addChild(poincareGeodesicsComponent);
	}

	private void initCirclesComponent() {
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute(CommonAttributes.EDGE_DRAW, true);
		app.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
		app.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, Color.RED);
		circlesComponent.setAppearance(app);
	}

	private void initUnitCircle() {
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute(CommonAttributes.EDGE_DRAW, true);
		app.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, Color.BLACK);
		unitDiscComponent.setAppearance(app);
		unitDiscComponent.setGeometry(IndexedLineSetUtility.circle(100));
	}
	
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src == generateRandomPointsButton) {
			pointsGenerator.setSeed(seedModel.getNumber().intValue());
			pointsGenerator.execute();
			resetCheckBoxes();
			poincareGeodesicsComponent.setVisible(false);
		} else if(src == liftToHyperboloidButton) {
			if(poincareButton.isSelected()) {
				HyperbolicDelaunayUtility.poincareToHyperboloid(hif.get(), hif.getAdapters());
			}
			if(kleinButton.isSelected()) {
				HyperbolicDelaunayUtility.kleinToHyperboloid(hif.get(), hif.getAdapters());
			}
			hif.update();
			poincareGeodesicsComponent.setVisible(false);
		} else if(src == calculateConvexHullButton) {
			HalfedgeUtility.retainEdges(hif.get(new VHDS()), new HashSet<VEdge>());
			ConvexHull.convexHull(hif.get(), hif.getAdapters(), 1E-8);
			hif.update();
			poincareGeodesicsComponent.setVisible(false);
		} else if(src == extractHyperbolicFacesButton) {
			HyperbolicDelaunayUtility.extractHyperbolicFaces(hif.get(), hif.getAdapters());
			hif.update();
			poincareGeodesicsComponent.setVisible(false);
		} else if(src == projectToDiscButton) {
			if(poincareButton.isSelected()) {
				HyperbolicDelaunayUtility.hyperboloidToPoincare(hif.get(), hif.getAdapters());
			}
			if(kleinButton.isSelected()) {
				HyperbolicDelaunayUtility.hyperboloidToKlein(hif.get(), hif.getAdapters());
			}
			hif.update();
			updatePoincareComponent();
			poincareGeodesicsComponent.setVisible(poincareButton.isSelected());
			contentAppearance.getAppearanceInspector().setShowLines(kleinButton.isSelected());
			contentAppearance.getAppearanceInspector().setShowFaces(kleinButton.isSelected());
		} else if(src == circlesBox || src == unitDiscBox || src == hyperboloidBox || src == paraboloidBox) {
			updateAuxComponent();
		} else if(src == hyperbolicDelaunayButton) {
			HyperbolicDelaunayUtility.poincareToHyperboloid(hif.get(), hif.getAdapters());
			HalfedgeUtility.retainEdges(hif.get(new VHDS()), new HashSet<VEdge>());
			ConvexHull.convexHull(hif.get(), hif.getAdapters(), 1E-8);
			HyperbolicDelaunayUtility.extractHyperbolicFaces(hif.get(), hif.getAdapters());
			HyperbolicDelaunayUtility.hyperboloidToPoincare(hif.get(), hif.getAdapters());
			hif.update();
			if(poincareButton.isSelected()) {
				updatePoincareComponent();
			}
			contentAppearance.getAppearanceInspector().setShowLines(kleinButton.isSelected());
			contentAppearance.getAppearanceInspector().setShowFaces(kleinButton.isSelected());
			poincareGeodesicsComponent.setVisible(poincareButton.isSelected());
		} else if(src == euclideanDelaunayButton) {
			EuclideanDelaunayUtility.liftToParaboloid(hif.get(), hif.getAdapters());
			HalfedgeUtility.retainEdges(hif.get(new VHDS()), new HashSet<VEdge>());
			ConvexHull.convexHull(hif.get(), hif.getAdapters(), 1E-8);
			EuclideanDelaunayUtility.extractEuclideanLowerFaces(hif.get(), hif.getAdapters());
			EuclideanDelaunayUtility.projectVertically(hif.get(), hif.getAdapters());
			hif.update();
			poincareButton.setSelected(true);
			poincareGeodesicsComponent.setVisible(false);
		} else if(src == kleinButton) {
			HyperbolicDelaunayUtility.poincareToKlein(hif.get(), hif.getAdapters());
			hif.update();
			poincareGeodesicsComponent.setVisible(false);
			contentAppearance.getAppearanceInspector().setShowLines(true);
			contentAppearance.getAppearanceInspector().setShowFaces(true);
			circlesBox.setSelected(false);
			circlesBox.setEnabled(false);
			circlesComponent.setVisible(false);
		} else if(src == poincareButton) {
			HyperbolicDelaunayUtility.kleinToPoincare(hif.get(), hif.getAdapters());
			hif.update();
			poincareGeodesicsComponent.setVisible(true);
			updatePoincareComponent();
			contentAppearance.getAppearanceInspector().setShowLines(false);
			contentAppearance.getAppearanceInspector().setShowFaces(false);
			circlesBox.setEnabled(true);
		}
		hif.removeTemporaryGeometry(auxComponent);
		hif.addTemporaryGeometry(auxComponent);
	}

	private void resetCheckBoxes() {
		hyperboloidBox.setSelected(false);
		paraboloidBox.setSelected(false);
		circlesBox.setSelected(false);
	}
	
	private void updateAuxComponent() {
		
		updateCirclesComponent();
		updateHyperboloidComponent();
		updateParaboloidComponent();
//		updatePoincareComponent();
		
		unitDiscComponent.setVisible(unitDiscBox.isSelected());
		if(poincareButton.isSelected()) {
			circlesComponent.setVisible(circlesBox.isSelected());
		} else {
			circlesComponent.setVisible(false);
		}
		hyperboloidComponent.setVisible(hyperboloidBox.isSelected());
		paraboloidComponent.setVisible(paraboloidBox.isSelected());
	}

	private void updateHyperboloidComponent() {
		
		VHDS hds = hif.get(new VHDS());
		AdapterSet as = hif.getAdapters();
		double maxZ = 0.0;
		for(VVertex v : hds.getVertices()) {
			double[] coords = as.getD(Position3d.class,v);
			if(maxZ < coords[2]) {
				maxZ = coords[2];
			}
		}
 
		if(maxZ < 1) {
			maxZ = 2.0;
		}
		double uparam = 1.05*(Math.log(maxZ + Math.sqrt(maxZ*maxZ - 1.0)));
		
		ParametricSurfaceFactory psf = new ParametricSurfaceFactory(new Hyperboloid());
		psf.setUMin(0);
		psf.setUMax(uparam);
		psf.setVMin(0.0);
		psf.setVMax(2*Math.PI);
		psf.setClosedInVDirection(true);
		//subdivisions of th domain
		psf.setULineCount(20);
		psf.setVLineCount(40);
		//generate edges and normals
		psf.setGenerateEdgesFromFaces(true);
		psf.setGenerateVertexNormals(true);
		psf.setGenerateFaceNormals(true);
		//generate the IndexFaceSet
		psf.update();
		hyperboloidComponent.setGeometry(psf.getGeometry());
		
	}

	private static class Hyperboloid implements Immersion {
		@Override
		public void evaluate(double u, double v, double[] xyz, int index) {
			xyz[3*index]= Math.sinh(u)*Math.cos(v);
			xyz[3*index+1]= Math.sinh(u)*Math.sin(v);
			xyz[3*index+2]= Math.cosh(u);
		}
		@Override
		public int getDimensionOfAmbientSpace() { return 3;	}
		@Override
		public boolean isImmutable() { return true; }
	};
	
	private void updateParaboloidComponent() {
		
		VHDS hds = hif.get(new VHDS());
		AdapterSet as = hif.getAdapters();
		double maxZ = 0.0;
		for(VVertex v : hds.getVertices()) {
			double[] coords = as.getD(Position3d.class,v);
			if(maxZ < coords[2]) {
				maxZ = coords[2];
			}
		}
		if(maxZ == 0) {
			maxZ = 1.0;
		}
		double uparam = 1.05*Math.sqrt(maxZ); 

		ParametricSurfaceFactory psf = new ParametricSurfaceFactory(new Paraboloid());
		psf.setUMin(0);
		psf.setUMax(uparam);
		psf.setVMin(0.0);
		psf.setVMax(2*Math.PI);
		psf.setClosedInVDirection(true);
		//subdivisions of th domain
		psf.setULineCount(20);
		psf.setVLineCount(40);
		//generate edges and normals
		psf.setGenerateEdgesFromFaces(true);
		psf.setGenerateVertexNormals(true);
		psf.setGenerateFaceNormals(true);
		//generate the IndexFaceSet
		psf.update();
		paraboloidComponent.setGeometry(psf.getGeometry());
		
	}
	
	private static class Paraboloid implements Immersion {
		@Override
		public void evaluate(double u, double v, double[] xyz, int index) {
			xyz[3*index]= u*Math.cos(v);
			xyz[3*index+1]= u*Math.sin(v);
			xyz[3*index+2]= u*u;
		}
		@Override
		public int getDimensionOfAmbientSpace() { return 3;	}
		@Override
		public boolean isImmutable() { return true; }
	};

	private void updatePoincareComponent() {
		poincareGeodesicsComponent.removeAllChildren();
		VHDS hds = hif.get(new VHDS());
		AdapterSet as = hif.getAdapters();
		for(VEdge e : hds.getPositiveEdges()) {
			double[] 
					v1 = as.getD(Position3d.class, e.getStartVertex()),
					v2 = as.getD(Position3d.class, e.getTargetVertex()),
					v3 = Rn.times(null, 1/Rn.euclideanNormSquared(v1), v1);
			double[] circle = GeometryUtility.circumCircle(v1, v2, v3);
			IndexedLineSet circularArc = circularArc(v1,v2,circle, 30);
			SceneGraphComponent sgc = new SceneGraphComponent(e.toString());
			sgc.setGeometry(circularArc);
			poincareGeodesicsComponent.addChild(sgc);
		}
	}
	
	private IndexedLineSet circularArc(double[] v1, double[] v2, double[] circle, int n) {
		double[][] points = new double[n+1][v1.length];
		double[] center = new double[]{circle[0], circle[1], circle[2]};
//		double radius = circle[3];
		double angle = FunctionalUtils.angle(v1, center, v2);
		double step = angle/n;
		double[] b1 = Rn.subtract(null, v1, center);
		double[] b2 = Rn.crossProduct(null, b1, Rn.normalize(null, Rn.crossProduct(null, b1, Rn.subtract(null, v2, center))));
		for(int i = 0; i < n+1; ++i) {
			points[i] = Rn.add(null, center, Rn.linearCombination(null, Math.cos(-i*step), b1, Math.sin(-i*step), b2)); 
		}
		return IndexedLineSetUtility.createCurveFromPoints(points, false);
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		pointsGenerator = c.getPlugin(RandomPointsUnitDisc.class);
		hif = c.getPlugin(HalfedgeInterface.class);
		contentAppearance = c.getPlugin(ContentAppearance.class);
		hif.addSelectionListener(this);
		hif.addHalfedgeListener(this);
	}
	
	public static void main(String[] args) {
		NativePathUtility.set("native");
		JRHalfedgeViewer.initHalfedgeFronted();
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addContentUI();
		v.addContentSupport(ContentType.Raw);
		v.registerPlugin(VarylabMain.class);
		v.registerPlugin(ConsolePlugin.class);
		v.registerPlugin(VertexEditorPlugin.class);
		v.registerPlugin(PlanarDelaunayExplorer.class);
		v.startup();
	}

	private void updateCirclesComponent() {
		circlesComponent.removeAllChildren();
		
		if(faceComponentMap == null) { return;}
		
		VHDS hds = hif.get(new VHDS());
		AdapterSet as = hif.getActiveAdapters();
		IndexedLineSet circle = IndexedLineSetUtility.circle(50);
		for(VFace f : hds.getFaces()) {
			List<VVertex> boundaryVertices = HalfEdgeUtils.boundaryVertices(f);
			if(boundaryVertices.size() == 3) {
				double[][] coords = new double[3][];
				int i = 0;
				for(VVertex v : boundaryVertices) {
					coords[i++] = as.getD(Position3d.class,v);
				}
				double[] cc = GeometryUtility.circumCircle(coords[0], coords[1], coords[2]);
				SceneGraphComponent sgc = new SceneGraphComponent(f.toString());
				faceComponentMap.put(f,sgc);
				sgc.setGeometry(circle);
				sgc.setVisible(false);
				MatrixBuilder mb = MatrixBuilder.euclidean();
				mb.translate(cc[0], cc[1], cc[2]);
//				mb.rotateFromTo(new double[] {0,0,1}, N);
				mb.scale(cc[3]);
				mb.assignTo(sgc);
				circlesComponent.addChild(sgc);
			}
					
		}
	}

	@Override
	public void selectionChanged(Selection s, HalfedgeInterface hif) {
		Set<Face<?,?,?>> selectedFaces = s.getFaces();
		if(circlesBox.isSelected()) {
			for(Face<?,?,?> f : hif.get().getFaces()) {
				SceneGraphComponent faceComponent = faceComponentMap.get(f);
				if(faceComponent != null) {
					faceComponent.setVisible(selectedFaces.contains(f));
				}
			}		
		}
	}

	@Override
	public void dataChanged(HalfedgeLayer layer) {
		layer.removeTemporaryGeometry(auxComponent);
		layer.addTemporaryGeometry(auxComponent);
		faceComponentMap.clear();
	}

	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
	}

	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
	}

	@Override
	public void layerCreated(HalfedgeLayer layer) {
	}

	@Override
	public void layerRemoved(HalfedgeLayer layer) {
	}
}
