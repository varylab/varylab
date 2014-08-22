package de.varylab.varylab.plugin.nurbs.plugin;

import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.ui.LayoutFactory;
import de.jreality.writer.WriterOBJ;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.data.VisualizationInterface;
import de.jtem.halfedgetools.plugin.misc.VertexEditorPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.interaction.DraggablePointComponent;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.BoundaryLines;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsWeightAdapter;
import de.varylab.varylab.plugin.nurbs.adapter.VectorFieldMapAdapter;
import de.varylab.varylab.plugin.nurbs.algorithm.ComputeVectorFields;
import de.varylab.varylab.plugin.nurbs.algorithm.ExtractControlMesh;
import de.varylab.varylab.plugin.nurbs.algorithm.LinearDeformation;
import de.varylab.varylab.plugin.nurbs.algorithm.NurbsSurfaceFromMesh;
import de.varylab.varylab.plugin.nurbs.algorithm.ProjectToNurbsSurface;
import de.varylab.varylab.plugin.nurbs.algorithm.SplitAtEdge;
import de.varylab.varylab.plugin.nurbs.algorithm.SplitInTheMiddle;
import de.varylab.varylab.plugin.nurbs.algorithm.StretchXYZ;
import de.varylab.varylab.plugin.nurbs.algorithm.UVUnroll;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.math.CurveType;
import de.varylab.varylab.plugin.nurbs.math.FaceSetGenerator;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurve;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurve.SymmetricDir;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurvesOriginal;
import de.varylab.varylab.plugin.nurbs.math.LineSegmentIntersection;
import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;
import de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility;
import de.varylab.varylab.plugin.nurbs.scene.ListSceneGraphComponent;
import de.varylab.varylab.plugin.nurbs.scene.PolygonalLineComponentProvider;
import de.varylab.varylab.plugin.nurbs.type.NurbsUVCoordinate;
import de.varylab.varylab.ui.ListSelectRemoveTable;
import de.varylab.varylab.ui.ListSelectRemoveTableModel;
import de.varylab.varylab.ui.PrettyPrinter;
//import com.thoughtworks.xstream.io.binary.Token.Attribute;

public class NurbsManagerPlugin extends ShrinkPanelPlugin {
	
	private static Logger logger = Logger.getLogger(NurbsManagerPlugin.class.getName());
	
	private HalfedgeInterface 
		hif = null;

	private GeodesicPanel
		geodesicPanel = new GeodesicPanel();
	
	private IntegralCurvesPanel
		curvatureLinesPanel = new IntegralCurvesPanel();
	
	private PointDistancePanel
		pointDistancePanel = new PointDistancePanel();

	private int 
		curveIndex = 5;
	
	private List<double[]> singularities = null;

	private PointSelectionPlugin pointSelectionPlugin = null;
	
	boolean firstVectorField = true;
	boolean secondVectorField = true;
	
	private NURBSSurface activeNurbsSurface;

	private VisualizationInterface vif;

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
		shrinkPanel.add(curvatureLinesPanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(geodesicPanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(pointDistancePanel, c);
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
	

	
	private class IntegralCurvesPanel extends ShrinkPanel implements ActionListener, PointSelectionListener, HalfedgeListener, ListSelectionListener, TableModelListener, ItemListener, ChangeListener {
		
		private static final long 
			serialVersionUID = 1L;
		
		private SpinnerNumberModel
			tolExpModel = new SpinnerNumberModel(-4, -30.0, 0, 1),
			nearUmbilicModel = new SpinnerNumberModel(-2, -30.0, 0, 1),
			vecFieldModel = new SpinnerNumberModel(10, 0, 180, 1),
			beginLineModel = new SpinnerNumberModel(0, 0, 4000, 1),
			endLineModel = new SpinnerNumberModel(0, 0, 4000, 1);
		
		
		private JSpinner
			tolSpinner = new JSpinner(tolExpModel),
			nearUmbilicSpinner = new JSpinner(nearUmbilicModel),
			vecFieldSpinner = new JSpinner(vecFieldModel),
			beginLineSpinner = new JSpinner(beginLineModel),
			endLineSpinner = new JSpinner(endLineModel);
			
		
		
		private JButton 
		    intersectionsButton = new JButton("Discretize!"),
		    deleteButton = new JButton("Delete All Curves"),
			goButton = new JButton("Go"),
			cutLineButton = new JButton("Cut Line");
			
		
		private JCheckBox
			showVectorFieldBox = new JCheckBox("show"),	
			immediateCalculationBox = new JCheckBox("Immediate"),
			maxCurvatureBox = new JCheckBox("Max (cyan)"),
			minCurvatureBox = new JCheckBox("Min (red)"),
			vecFieldBox = new JCheckBox("Vec. Field (red)"),
			conjFieldBox = new JCheckBox("Conj. Field (cyan)"),
			symConjBox = new JCheckBox(),
			symConjCurvatureBox = new JCheckBox();
		
		
		private JComboBox<CurveType>
			curveCombo = new JComboBox<CurveType>();
		
		private ListSelectRemoveTableModel<PolygonalLine>
			curvesModel = new ListSelectRemoveTableModel<PolygonalLine>("Curves", new PolygonalLinePrinter());
		
		private ListSelectRemoveTable<PolygonalLine> 
			curvesTable = new ListSelectRemoveTable<PolygonalLine>(curvesModel);
		
		private Map<HalfedgeLayer, ListSelectRemoveTableModel<PolygonalLine>> 
			layers2models = new HashMap<HalfedgeLayer, ListSelectRemoveTableModel<PolygonalLine>>();
		
		private JScrollPane 
			curveScrollPanel = new JScrollPane(curvesTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
		private JPanel
			curveLengthPanel = new JPanel();

		private PolygonalLineComponentProvider
			polylineComponentProvider = new PolygonalLineComponentProvider();
		
		private ListSceneGraphComponent<PolygonalLine, PolygonalLineComponentProvider>
			integralCurvesRoot = new ListSceneGraphComponent<>("Integral curves root", polylineComponentProvider);

		private PolygonalLine activeCurve;

		private VectorFieldMapAdapter vfmax;

		private VectorFieldMapAdapter vfmin;
		
		private IntegralCurve ic;
		
		private LinkedList<DraggableCurves> currentCurves = new LinkedList<>();
		private LinkedList<LinkedList<double[]>> commonPoints = new LinkedList<>();
		private boolean interactiveDragging;

		private double[] getVecField(){
			if(vecFieldSpinner.isEnabled()){
				double grad = vecFieldModel.getNumber().doubleValue();
				double phi = grad * Math.PI / 180.0;
				double[] vec = {Math.cos(phi), Math.sin(phi)};
				return vec;
			}
			return null;
		}
		
		public IntegralCurvesPanel() {
			
			super("Integral Curves");
			setShrinked(true);
			
			setLayout(new GridBagLayout());
			GridBagConstraints lc = LayoutFactory.createLeftConstraint();
			GridBagConstraints rc = LayoutFactory.createRightConstraint();
			
			curveCombo.addItem(CurveType.CONJUGATE);
			curveCombo.addItem(CurveType.CURVATURE);
			curveCombo.addItem(CurveType.ASYMPTOTIC);
			curveCombo.addItemListener(this);
			
			lc.gridwidth = 2;
			add(new JLabel("Curve Type"),lc);
			lc.gridwidth = 1;
			add(curveCombo,rc);
			lc.gridwidth = 1;
			add(new JLabel("Vector Field (angle)"),lc);
			lc.gridwidth = 1;
			add(vecFieldSpinner, lc);
			lc.gridwidth = 1;
			add(showVectorFieldBox, rc);
			showVectorFieldBox.addActionListener(this);
			vecFieldSpinner.setEnabled(true);
			vecFieldSpinner.addChangeListener(this);
			lc.gridwidth = 2;
			add(new JLabel("Runge-Kutta Tolerance Exp"), lc);
			lc.gridwidth = 1;
			add(tolSpinner, rc);
			lc.gridwidth = 2;
			add(new JLabel("Singularity Neighbourhood Exp"), lc);
			lc.gridwidth = 1;
			add(nearUmbilicSpinner, rc);
			add(new JLabel("Curvature Lines:"), lc);
			add(minCurvatureBox, lc);
			minCurvatureBox.setEnabled(false);
			add(maxCurvatureBox, rc);
			maxCurvatureBox.setEnabled(false);
			add(new JLabel("Conjugate Lines:"), lc);
			add(vecFieldBox, lc);
			vecFieldBox.setEnabled(true);
			add(conjFieldBox, rc);
			conjFieldBox.setEnabled(true);
			lc.gridwidth = 2;
			add(new JLabel("Sym. Conj. w.r.t Vector Field:"), lc);
			lc.gridwidth = 1;
			add(symConjBox, rc);
			symConjBox.addActionListener(this);
			symConjBox.setEnabled(true);
			lc.gridwidth = 2;
			add(new JLabel("Sym. Conj. w.r.t Curvature Direction:"), lc);
			lc.gridwidth = 1;
			add(symConjCurvatureBox, rc);
			symConjCurvatureBox.setEnabled(true);
			symConjCurvatureBox.addActionListener(this);
			
			add(immediateCalculationBox,lc);
			add(goButton, rc);
			goButton.addActionListener(this);
			
			curvesTable.getSelectionModel().addListSelectionListener(this);
			curvesTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			
			curvesModel = curvesTable.getListModel();
			
			curvesModel.addTableModelListener(this);
			curveScrollPanel.setMinimumSize(new Dimension(100, 150));
			curveLengthPanel.setMinimumSize(new Dimension(100, 50));
			curveLengthPanel.add(new JLabel("Start Vertex"), lc);
			curveLengthPanel.add(beginLineSpinner, lc);
			beginLineSpinner.setEnabled(false);
			curveLengthPanel.add(new JLabel("End Vertex"), lc);
			curveLengthPanel.add(endLineSpinner, rc);
			endLineSpinner.setEnabled(false);
			curveLengthPanel.add(cutLineButton, rc);
			cutLineButton.addActionListener(this);
			cutLineButton.setEnabled(false);
			
			
			add(curveScrollPanel, rc);
			add(curveLengthPanel, rc);

			Appearance app = new Appearance();
			app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
			app.setAttribute(CommonAttributes.EDGE_DRAW, true);
			integralCurvesRoot.setAppearance(app);
			
			deleteButton.addActionListener(this);
			add(deleteButton, rc);
			intersectionsButton.addActionListener(this);
			add(intersectionsButton,rc);
		}
		
		private class DraggableCurves implements PointDragListener {
			
			private LinkedList<PolygonalLine> polygonalLines;			
			private DraggablePointComponent draggablePoint;
			private double[] p;
			private boolean uDir = pointSelectionPlugin.getUDir();
			private boolean vDir = pointSelectionPlugin.getVDir();
			double[] startUV = null;
			LinkedList<DraggableCurves> commonCurves = null;
			LinkedList<Integer> indexList;


			

			public DraggableCurves(double[] uv, double[] point, LinkedList<PolygonalLine> lines) {
				startUV = uv;
				draggablePoint = createDraggablePoint(point);
				polygonalLines = lines;
				indexList = new LinkedList<>();
				for (PolygonalLine pl : lines) {
					indexList.add(pl.getCurveIndex());
				}
				
			}
			
			public double[] getStartUV(){
				return startUV;
			}
			
			public DraggablePointComponent getDraggablePoint(){
				return draggablePoint;
			}
			
			public LinkedList<PolygonalLine> getPolygonalLines(){
				return polygonalLines;
			}
			
			
			
			public DraggablePointComponent createDraggablePoint(double[] point){
				DraggablePointComponent dpc = new DraggablePointComponent(point);
				dpc.setUseDefaultDraggListener(false);
				dpc.addPointDragListener(this);
				Appearance Ap = new Appearance();
				Ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
				dpc.setAppearance(Ap);
				DefaultGeometryShader idgs = ShaderUtility.createDefaultGeometryShader(Ap, false);
				DefaultPointShader ipointShader = (DefaultPointShader)idgs.getPointShader();
				ipointShader.setDiffuseColor(Color.orange);
				hif.getActiveLayer().addTemporaryGeometry(dpc);
				return dpc;
			}

			
			@Override
			public void pointDragStart(PointDragEvent e) {
//				LinkedList<DraggableCurves>  otherCurves = getCommonCurves(startUV);
//				for (DraggableCurves dc : otherCurves) {
//					for (PolygonalLine pl : dc.polygonalLines) {
//						curvesModel.remove(pl);
//					}
//				}
//				for (PolygonalLine pl : polygonalLines) {
//					curvesModel.remove(pl);
//				}
//				curvesModel.fireTableDataChanged();
			}

			@Override
			public void pointDragged(PointDragEvent e) {
				LinkedList<DraggableCurves>  otherCurves = getCommonCurves(startUV);
				for (DraggableCurves dc : otherCurves) {
					for (PolygonalLine pl : dc.polygonalLines) {
						curvesModel.remove(pl);
					}
				}
				for (PolygonalLine pl : polygonalLines) {
					curvesModel.remove(pl);
				}
				p = new double[]{e.getX(), e.getY(), e.getZ(), 1.0};
				double[] uv = activeNurbsSurface.getClosestPointDomainDir(p, startUV, uDir, vDir);	
				draggablePoint.updateCoords(activeNurbsSurface.getSurfacePoint(uv[0], uv[1]));
				if(interactiveDragging){
					for (PolygonalLine pl : polygonalLines) {
						curvesModel.remove(pl);
					}
					recomputeCurves(uv);
					curvesModel.addAll(polygonalLines);
				}
				curvesModel.fireTableDataChanged();
			}

			@Override
			public void pointDragEnd(PointDragEvent e) {
				double[] uv = activeNurbsSurface.getClosestPointDomainDir(p, startUV, uDir, vDir);	
				if(!interactiveDragging){
					recomputeCurves(uv);
					curvesModel.addAll(polygonalLines);
				}
				LinkedList<DraggableCurves>  otherCurves = getCommonCurves(startUV);
				for (DraggableCurves dc : otherCurves) {
					DraggablePointComponent dpc = dc.getDraggablePoint();
					double[] translation = Rn.subtract(null, uv, startUV);
					double[] otherStartUV = dc.getStartUV();
					double[] newCoords = Rn.add(null, otherStartUV, translation);
					dpc.updateCoords(activeNurbsSurface.getSurfacePoint(newCoords[0], newCoords[1]));
					dc.recomputeCurves(newCoords);
					curvesModel.addAll(dc.getPolygonalLines());				
				}
				curvesModel.fireTableDataChanged();
			}	
			
			public void recomputeCurves(double[] uv) {
				polygonalLines = ic.computeIntegralLine(firstVectorField, secondVectorField, 0.01, singularities, uv);
			}
		
			public LinkedList<DraggableCurves> getCommonCurves(double[] p){
				if(commonCurves == null){
					commonCurves = new LinkedList<>();
					LinkedList<double[]> commonPoints = getCommonPoints(p);
					for (DraggableCurves dc : currentCurves) {
						if(commonPoints.contains(dc.getStartUV())){
							commonCurves.add(dc);
						}		
		
					}
				}
				return commonCurves;
			}
			
			public LinkedList<double[]> getCommonPoints(double[] p){
				LinkedList<double[]> others = new LinkedList<>();
				for (LinkedList<double[]> list : commonPoints) {
					if(list.contains(p)){
						for (double[] point : list) {
							if(point != p){
								others.add(point);
							}
						}
					}
				}
				return others;
			}
			
		}
		
		
		
		@Override
		public void actionPerformed(ActionEvent e){
			interactiveDragging = pointSelectionPlugin.getInteractiveDragging();
			Object source = e.getSource();
			if(source == goButton) {
				List<double[]> startingPointsUV = pointSelectionPlugin.getSelectedPoints();
				if(startingPointsUV.size() == 0) {
					return;
				}
				logger.info("ALL STARTING POINTS");
				for (double[] sp : startingPointsUV) {
					logger.info(Arrays.toString(sp));
				}
				double tol = tolExpModel.getNumber().doubleValue();
				tol = Math.pow(10, tol);
				double umbilicStop = nearUmbilicModel.getNumber().doubleValue();
				umbilicStop = Math.pow(10, umbilicStop);
				

				CurveType vfc = (CurveType)curveCombo.getSelectedItem();
			
				if(vfc == CurveType.CURVATURE){
					firstVectorField = maxCurvatureBox.isSelected();
					secondVectorField = minCurvatureBox.isSelected();
				}
				else if(vfc == CurveType.CONJUGATE){
					logger.info("conj vec");
					firstVectorField = vecFieldBox.isSelected();
					secondVectorField = conjFieldBox.isSelected();
				}
				
				SymmetricDir symDir = SymmetricDir.NO_SYMMETRIE;
				if(symConjBox.isSelected()){
					symDir = SymmetricDir.DIRECTION;
				}
				else if(symConjCurvatureBox.isSelected()){
					symDir = SymmetricDir.CURVATURE;
				}	
				ic = new IntegralCurve(activeNurbsSurface, vfc, tol, symDir, getVecField(), curveIndex);
				if(singularities == null) {
//					computeUmbilicalPoints();
				}
				
				
				commonPoints = pointSelectionPlugin.getCommonPointList();
				for (double[] sp : startingPointsUV) {
					double[] surfacePoint = activeNurbsSurface.getSurfacePoint(sp[0], sp[1]);
					LinkedList<PolygonalLine> lines = ic.computeIntegralLine(firstVectorField, secondVectorField, umbilicStop, singularities, sp);
					DraggableCurves dc = new DraggableCurves(sp, surfacePoint, lines);
					currentCurves.add(dc);
				}
				
				curveIndex = ic.getCurveIndex() + 1;
				

				for (DraggableCurves dc : currentCurves) {
					curvesModel.addAll(dc.polygonalLines);
				}
				System.out.println("All lines");
				for (PolygonalLine pl : curvesModel.getList()) {
					System.out.println(pl.toString());
				}
				
//				LinkedList<PolygonalLine> currentLines = ic.computeIntegralLines(firstVectorField, secondVectorField, umbilicStop, singularities, startingPointsUV);
//				curveIndex = ic.getCurveIndex() + 1;
//				curvesModel.addAll(currentLines);
				
				
				hif.clearSelection();
				curvesModel.fireTableDataChanged();
			} else if(source == deleteButton) {
				curvesModel.clear();
				hif.clearSelection();
				curvesModel.fireTableDataChanged();
			} else if(source == intersectionsButton) {
				// default patch
				LinkedList<LineSegment> allSegments = new LinkedList<LineSegment>();
				System.out.println("All curves from the curveModel");
				for(PolygonalLine pl : curvesModel.getChecked()){
					System.out.println(pl.toString());
					allSegments.addAll(pl.getpLine());
				}
				List<LineSegment> completeDomainBoundarySegments = activeNurbsSurface.getCompleteDomainBoundarySegments();
				
				double[] U = activeNurbsSurface.getUKnotVector();
				double[] V = activeNurbsSurface.getVKnotVector();
				
				int boundaryIndex = 1;
				System.out.println("All boundary curves ");
				for (LineSegment bs : completeDomainBoundarySegments) {
					logger.info("boundary segment ");
					bs.setCurveIndex(boundaryIndex);
					System.out.println("boundary saegment "+ bs.getCurveIndex());
					boundaryIndex++;
					logger.info(""+bs.getCurveIndex());
				}
				
				allSegments.addAll(completeDomainBoundarySegments);			
				allSegments = LineSegmentIntersection.preSelection(U, V, allSegments);
				double firstTimeDouble = System.currentTimeMillis();
				double lastTimeDouble = System.currentTimeMillis();
				logger.info("Double Time: " + (lastTimeDouble - firstTimeDouble));
				double firstTimeBentley = System.currentTimeMillis();
				LinkedList<IntersectionPoint> intersections = LineSegmentIntersection.BentleyOttmannAlgoritm(U, V, allSegments);
				logger.info("\n");
				logger.info("NURBS manager plugin all intersections");
				for (IntersectionPoint ip : intersections) {
					logger.info(ip.toString());
				}
				FaceSetGenerator fsg = new FaceSetGenerator(activeNurbsSurface, intersections);
				double lastTimeBentley = System.currentTimeMillis();
				logger.info("Bentley Ottmann Time: " + (lastTimeBentley - firstTimeBentley));
				allSegments.clear();
				FaceSet fs = fsg.createFaceSet();
				for (int i = 0; i < fs.getVerts().length; i++) {
					double[] S = new double[4];
					activeNurbsSurface.getSurfacePoint(fs.getVerts()[i][0], fs.getVerts()[i][1], S);
					fs.getVerts()[i] = S;
				}

				HalfedgeLayer hel = new HalfedgeLayer(hif);
				hel.setName("Curvature Geometry");
//				writeToFile(fS);
			
				hif.addLayer(hel); //add and activate
				hel.set(fs.getIndexedFaceSet());
				hif.update();
			} else if(source == symConjBox || source == symConjCurvatureBox) {
				if(symConjBox.isSelected()) {
					symConjCurvatureBox.setEnabled(false);
				} else if(symConjCurvatureBox.isSelected()) {
					symConjBox.setEnabled(false);
				} else {
					symConjCurvatureBox.setEnabled(true);
					symConjBox.setEnabled(true);
				}
			} else if(source == cutLineButton){
				activeCurve.setBegin(beginLineModel.getNumber().intValue());
				activeCurve.setEnd(endLineModel.getNumber().intValue());
				updateIntegralCurvesRoot();
			} else if(source == showVectorFieldBox) {
				if(showVectorFieldBox.isSelected()) {
					updateVectorfields();
				} else {
					removeVectorFields();
				}
			}
			
		}

		private void removeVectorFields() {
			hif.removeAdapter(vfmin);
			hif.removeAdapter(vfmax);
			vfmin = null;
			vfmax = null;
		}

		private void updateVectorfields() {
			if(vfmax == null) {
				vfmax = new VectorFieldMapAdapter();
				vfmax.setName("Symmetric Conjugate 1");
			}
			if(vfmin == null) {
				vfmin = new VectorFieldMapAdapter();
				vfmin.setName("Symmetric Conjugate 2");
			}
			NurbsUVAdapter nurbsAdapter = hif.getAdapters().query(NurbsUVAdapter.class);
			if(nurbsAdapter != null) {
				for(VVertex v : hif.get(new VHDS()).getVertices()) {
					double[][] principleDirections = getDirections(nurbsAdapter.getSurface(), nurbsAdapter.getV(v, null), getVecField());
					vfmax.setV(v,principleDirections[0],null);
					vfmin.setV(v,principleDirections[1],null);
					
				}
			} else {
				throw new RuntimeException("No nurbs surface on active layer.");
			}
			hif.addLayerAdapter(vfmax, false);
			hif.addLayerAdapter(vfmin, false);
			vif.updateActiveVisualizations();
		}


		@SuppressWarnings("unused")
		private void writeToFile(FaceSet fS) {
			try {
				FileOutputStream fos = new FileOutputStream("test.obj");
				WriterOBJ.write(fS.getIndexedFaceSet(), fos);
				fos.close();
			} catch (Exception e2) {}
		}
		

		@Override
		public void pointSelected(double[] uv) {
			List<double[]> startingPointsUV = pointSelectionPlugin.getSelectedPoints();
			double tol = tolExpModel.getNumber().doubleValue();
			tol = Math.pow(10, tol);
			double umbilicStop = nearUmbilicModel.getNumber().doubleValue();
			umbilicStop = Math.pow(10, umbilicStop);
			boolean firstVectorField = maxCurvatureBox.isSelected();
			boolean secondVectorField = minCurvatureBox.isSelected();
			if(immediateCalculationBox.isSelected()) {
				SymmetricDir symDir = SymmetricDir.NO_SYMMETRIE;
				if(symConjBox.isSelected()){
					symDir = SymmetricDir.DIRECTION;
				}
				else if(symConjCurvatureBox.isSelected()){
					symDir = SymmetricDir.CURVATURE;
				}	

				IntegralCurve ic = new IntegralCurve(activeNurbsSurface, (CurveType) curveCombo.getSelectedItem(), tol, symDir, getVecField(), curveIndex);
				
				if(singularities == null) {
					computeUmbilicalPoints();
				}
				
				List<PolygonalLine> currentLines = ic.computeIntegralLines(firstVectorField, secondVectorField, umbilicStop, singularities, startingPointsUV);
//				LinkedList<PolygonalLine> curvatureLines = computeCurvatureLines(Lists.newArrayList(uv));
				for(PolygonalLine pl : currentLines) {
					if(!curvesModel.contains(pl)) {
						curvesModel.add(pl);
//						integralCurvesRoot.addChild(createLineComponent(pl));
					}
				}
				curvesModel.fireTableDataChanged();
			}
		}


		@Override
		public void dataChanged(HalfedgeLayer layer) {
			updateActiveNurbsSurface(layer);
			layer.addTemporaryGeometry(integralCurvesRoot.getComponent());
//			if(curvesModel == null) {
				if(layers2models.containsKey(layer)) {
					curvesModel = layers2models.get(layer);
					curvesModel.clear();
				} 
//				else {
//					curvesModel = new ListSelectRemoveTableModel<PolygonalLine>("Initial Point", new PolygonalLinePrinter());
//					curvesModel.addTableModelListener(this);
//					layers2models.put(layer,curvesModel);
//				}
//			} else {
//				curvesModel.clear();
//			}
			curvesModel.fireTableDataChanged();
		}


		@Override
		public void adaptersChanged(HalfedgeLayer layer) {
			updateActiveNurbsSurface(layer);
			if(activeNurbsSurface == null) {
				curvesModel.clear();
				curvesModel.fireTableDataChanged();
			}
		}


		@Override
		public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
			if(old == active) {
				return;
			}
			old.removeTemporaryGeometry(integralCurvesRoot.getComponent());
			active.addTemporaryGeometry(integralCurvesRoot.getComponent());
			
			layers2models.put(old,curvesModel);
			if(layers2models.containsKey(active)) {
				curvesModel = layers2models.get(active);
			} else {
				System.err.println("this should not happen!");
			}
			curvesTable.setModel(curvesModel);
			updateActiveNurbsSurface(active);
			curvesModel.fireTableDataChanged();
		}

		@Override
		public void layerCreated(HalfedgeLayer layer) {
			if(!layers2models.containsKey(layer)) {
				ListSelectRemoveTableModel<PolygonalLine> newModel = new ListSelectRemoveTableModel<PolygonalLine>("Polygonal line", new PolygonalLinePrinter());
				newModel.addTableModelListener(this);
				layers2models.put(layer,newModel);
			}
		}

		@Override
		public void layerRemoved(HalfedgeLayer layer) {
			layers2models.remove(layer);
		}


		@Override
		public void valueChanged(ListSelectionEvent e) {
			EffectiveAppearance ea = hif.getEffectiveAppearance(integralCurvesRoot.getComponent());
			if(ea == null) {
				return;
			}
			DefaultGeometryShader dgs2 = ShaderUtility.createDefaultGeometryShader(ea);
			DefaultLineShader dls = (DefaultLineShader) dgs2.getLineShader();
			
			for(int i = 0; i < curvesTable.getRowCount(); ++i) {
				int mi = curvesTable.convertRowIndexToModel(i);
				SceneGraphComponent lineComp = integralCurvesRoot.getComponent().getChildComponent(mi);
				boolean isVisible = lineComp.isVisible();
				boolean beginEnd = true;
				Appearance app = lineComp.getAppearance();
				app.setAttribute(LINE_SHADER + "." + RADII_WORLD_COORDINATES, dls.getRadiiWorldCoordinates());
				if(curvesTable.isRowSelected(i)) {
					if(beginEnd == true){
						activeCurve = curvesModel.getList().get(mi);
						int	max = activeCurve.getpLine().size()+1;
						beginLineModel.setMaximum(max);
						endLineModel.setMaximum(max);
						beginLineSpinner.setEnabled(true);
						beginLineModel.setValue(activeCurve.getBegin());
						endLineSpinner.setEnabled(true);
						endLineModel.setValue(activeCurve.getEnd());
						cutLineButton.setEnabled(true);
					}
					beginEnd = false;
					app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls.getTubeRadius()*1.8);
				} else {
					app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls.getTubeRadius());					
				}
				lineComp.setVisible(isVisible);
			}
		}


		@Override
		public void tableChanged(TableModelEvent e) {
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					updateIntegralCurvesRoot();
					curvesTable.adjustColumnSizes();
				}
			};
			EventQueue.invokeLater(runnable);
		}

		private class PolygonalLinePrinter implements PrettyPrinter<PolygonalLine> {

			@Override
			public String toString(PolygonalLine t) {
				return t.getDescription();
			}
			
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			updateIntegralCurvesPanelUI();
		}


		private void updateIntegralCurvesPanelUI() {
			if(CurveType.CONJUGATE == curveCombo.getSelectedItem()){
				vecFieldSpinner.setEnabled(true);
				vecFieldBox.setEnabled(true);
				conjFieldBox.setEnabled(true);
				symConjBox.setEnabled(true);
				symConjCurvatureBox.setEnabled(true);
			}
			else{
				vecFieldSpinner.setEnabled(false);
				vecFieldBox.setEnabled(false);
				conjFieldBox.setEnabled(false);
				symConjBox.setEnabled(false);
				symConjCurvatureBox.setEnabled(false);
			}
			if(CurveType.CURVATURE == curveCombo.getSelectedItem()){
				minCurvatureBox.setEnabled(true);
				maxCurvatureBox.setEnabled(true);
			}
			else{
				minCurvatureBox.setEnabled(false);
				maxCurvatureBox.setEnabled(false);
			}
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			if(e.getSource() == vecFieldSpinner) {
				updateVectorfields();
			}
		}

		private void updateIntegralCurvesRoot() {
			polylineComponentProvider.setSurface(activeNurbsSurface);
			List<PolygonalLine> list = curvesModel.getList();
			integralCurvesRoot.retain(list);
			for(PolygonalLine pl : list) {
				integralCurvesRoot.setVisible(pl, curvesModel.isChecked(pl));
			}
		}
	}
	
	private class PointDistancePanel extends ShrinkPanel implements ActionListener{
		
		private static final long 
			serialVersionUID = 1L;
		private JButton
			showControlMeshButton = new JButton("Show Control Mesh"),
			goButton = new JButton("Go"),
			pointButton = new JButton("create point");
		
		boolean pointCreated = false;
		
		public double[] point = {1, 1, 1, 1.0};
		
		public PointDistancePanel() {
			super("Point Distance");
			setShrinked(true);
			setLayout(new GridBagLayout());
			GridBagConstraints rc = LayoutFactory.createRightConstraint();
			add(pointButton, rc);
			add(goButton, rc);
			add(showControlMeshButton, rc);
			goButton.addActionListener(this);
			showControlMeshButton.addActionListener(this);
			pointButton.addActionListener(this);
			
		}
	
		@Override
		public void actionPerformed(ActionEvent e){
			
			if(pointButton == e.getSource()){
				pointCreated = true;
				HalfedgeLayer helPoint = new HalfedgeLayer(hif);
				helPoint.setName("Point Distance");
				hif.addLayer(helPoint);
				PointSetFactory psfi = new PointSetFactory();
				logger.info("create point");
				psfi.setVertexCount(1);
				psfi.setVertexCoordinates(point);
				psfi.update();
				SceneGraphComponent sgci = new SceneGraphComponent("point distance");
				SceneGraphComponent distancePointComp = new SceneGraphComponent("Intersection");
				sgci.addChild(distancePointComp);
				sgci.setGeometry(psfi.getGeometry());
				Appearance iAp = new Appearance();
				sgci.setAppearance(iAp);
				DefaultGeometryShader idgs = ShaderUtility.createDefaultGeometryShader(iAp, false);
				DefaultPointShader ipointShader = (DefaultPointShader)idgs.getPointShader();
				ipointShader.setDiffuseColor(Color.orange);
				hif.getActiveLayer().addTemporaryGeometry(sgci);
				logger.info("point before dragging " + Arrays.toString(point));
				DragEventTool t = new DragEventTool();
				t.addPointDragListener(new PointDragListener() {
	
					@Override
					public void pointDragStart(PointDragEvent e) {
					}
	
					@Override
					public void pointDragged(PointDragEvent e) {
						PointSet pointSet = e.getPointSet();
						double[][] points=new double[pointSet.getNumPoints()][];
				        pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
				        points[e.getIndex()]=e.getPosition(); 
				        point = e.getPosition().clone();
				    
//				        Pn.dehomogenize(point, point);
				        point[3] = 1.;
//				        logger.info(Arrays.toString(point));
				    
				        pointSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));
					}
					
					@Override
					public void pointDragEnd(PointDragEvent e) {
					}
					
				});
				
				sgci.addTool(t);
				psfi.setVertexCoordinates(point);
				
			}
			if (showControlMeshButton == e.getSource()) {
				NURBSSurface ns = getSelectedSurface();
				logger.info("original surface " + ns.toString());
				NURBSSurface decomposed = ns.decomposeSurface();				
				double[][][] cm = decomposed.getControlMesh();
				QuadMeshFactory qmf = new QuadMeshFactory();
				qmf.setULineCount(cm[0].length);
				qmf.setVLineCount(cm.length);
				qmf.setVertexCoordinates(cm);
				qmf.setGenerateEdgesFromFaces(true);
				qmf.update();
				IndexedFaceSet ifs = qmf.getIndexedFaceSet();
				SceneGraphComponent cmc = new SceneGraphComponent("Control Mesh");
				cmc.setGeometry(ifs);
				Appearance app = new Appearance();
				cmc.setAppearance(app);
				app.setAttribute(CommonAttributes.FACE_DRAW, true);
				app.setAttribute(CommonAttributes.EDGE_DRAW, true);
				app.setAttribute(CommonAttributes.VERTEX_DRAW, true);
				hif.getActiveLayer().addTemporaryGeometry(cmc);
			}
			
			if (pointCreated && goButton == e.getSource()) {
				point = calculateClosestPoint(point);
			}
		}
		
	

		private double[] calculateClosestPoint(double[] point) {
//			double [] domainPoint = activeNurbsSurface.getClosestPointDomain(point);
//			double[] surfacePoint = activeNurbsSurface.getSurfacePoint(domainPoint[0], domainPoint[1]);

//			System.out.println("the point = " + Arrays.toString(point));

//			
			double[] surfacePoint = activeNurbsSurface.getClosestPoint(point);
			HalfedgeLayer surfPoint = new HalfedgeLayer(hif);
			surfPoint.setName("Point ");			
			hif.addLayer(surfPoint);
			hif.update();
//			PointSetFactory psfPoint = new PointSetFactory();
			DraggablePointComponent dpc = new DraggablePointComponent(surfacePoint);
			
			
//			psfPoint.setVertexCount(1);
			
//			psfPoint.setVertexCoordinates(surfacePoint);
//			psfPoint.update();
			SceneGraphComponent sgcPoint = new SceneGraphComponent("closest point");
			SceneGraphComponent PointComp = new SceneGraphComponent("PointComponent");
			sgcPoint.addChild(dpc);
			sgcPoint.addChild(PointComp);
			sgcPoint.setGeometry(dpc.getGeometry());
			Appearance iAPoint = new Appearance();
			sgcPoint.setAppearance(iAPoint);
			DefaultGeometryShader idgsPoint = ShaderUtility.createDefaultGeometryShader(iAPoint, false);
			DefaultPointShader ipointShaderPoint = (DefaultPointShader)idgsPoint.getPointShader();
			ipointShaderPoint.setDiffuseColor(Color.red);
			hif.getActiveLayer().addTemporaryGeometry(sgcPoint);
			return point;
		}
	}	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(curvatureLinesPanel);
		hif.addAdapter(new NurbsWeightAdapter(), true);
		pointSelectionPlugin  = c.getPlugin(PointSelectionPlugin.class);
		pointSelectionPlugin.addPointSelectionListener(curvatureLinesPanel);
		vif = c.getPlugin(VisualizationInterface.class);
		c.getPlugin(NurbsIOPlugin.class);
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
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "calculateMaxCurve", curvatureLinesPanel.maxCurvatureBox.isSelected());
		c.storeProperty(getClass(), "calculateMinCurve", curvatureLinesPanel.minCurvatureBox.isSelected());
		c.storeProperty(getClass(), "immediateCurveCalculation", curvatureLinesPanel.immediateCalculationBox.isSelected());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		curvatureLinesPanel.maxCurvatureBox.setSelected(c.getProperty(getClass(), "calculateMaxCurve",curvatureLinesPanel.maxCurvatureBox.isSelected()));
		curvatureLinesPanel.minCurvatureBox.setSelected(c.getProperty(getClass(), "calculateMinCurve",curvatureLinesPanel.minCurvatureBox.isSelected()));
		curvatureLinesPanel.immediateCalculationBox.setSelected(c.getProperty(getClass(),"immediateCurveCalculation",curvatureLinesPanel.immediateCalculationBox.isSelected()));
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
		singularities = null;
	}
	
	private double[][] computeUmbilicalPoints() {
		AdapterSet as = hif.getAdapters();
		as.addAll(hif.getActiveAdapters());
		VHDS hds = hif.get(new VHDS());
		singularities = activeNurbsSurface.findUmbilics(hds, as);
		double[][] uu = new double[singularities.size()][];
		double[][] upoints = new double[singularities.size()][];
		for (int i = 0; i < uu.length; i++) {
			uu[i] = singularities.get(i);
		}
		
		for (int i = 0; i < uu.length; i++) {
			double[] S = new double[4];
			activeNurbsSurface.getSurfacePoint(uu[i][0], uu[i][1], S);
			upoints[i] = S;
		}
		
		return upoints;
	}
	


	public NURBSSurface getSelectedSurface() {
		LinkedList<BoundaryLines> boundList = new LinkedList<NURBSSurface.BoundaryLines>();
		boundList.add(BoundaryLines.u0);
		boundList.add(BoundaryLines.um);
		boundList.add(BoundaryLines.v0);
		boundList.add(BoundaryLines.vn);
		activeNurbsSurface.setBoundLines(boundList);
		return activeNurbsSurface;
	}
	
//	@Override
//	public void mainUIChanged(String uiClass) {
//		super.mainUIChanged(uiClass);
//		SwingUtilities.updateComponentTreeUI(chooser);
//	}
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addContentUI();
		v.addBasicUI();
		v.registerPlugin(new NurbsManagerPlugin());
		v.startup();
	}
	
	private double[][] getDirections(NURBSSurface ns, double[] uv, double[] vecField) {
		double[] givenDir = vecField;
		CurvatureInfo ci =  NURBSCurvatureUtility.curvatureAndDirections(ns, uv);
		double K = ci.getGaussCurvature();
		double[][] dir = {{0,0,0},{0,0,0}};
		if(K > 0){
			double k1 = ci.getMinCurvature();
			double k2 = ci.getMaxCurvature();
			double[] e1 = ci.getCurvatureDirections()[0];
			double[] e2 = ci.getCurvatureDirections()[1];
			double[] v = Rn.normalize(null, Rn.add(null, Rn.times(null, givenDir[0], e1), Rn.times(null, givenDir[1], e2)));
			double delta = 0.;
			if(Rn.innerProduct(v, e1) > 1){
				delta = 0.;
			}
			else if(Rn.innerProduct(v, e1) < -1){
				delta = Math.PI;
			}
			else{
				delta = 2 * Math.acos(Rn.innerProduct(v, e1));
			}
			double theta;
			if(k2 == 0){
				theta = Math.PI / 2.;
			}
			else{
				double q = k1 / k2;
				double p = Math.tan(delta) * (1 + q) / 2;
				theta = Math.atan(p + Math.sqrt(p * p + q));
			}
			
			dir[0] = Rn.linearCombination(null, Math.cos(theta), e1, Math.sin(theta),e2);
			dir[1] = Rn.linearCombination(null, -k2*Math.sin(theta),e1, k1*Math.cos(theta),e2);
			Rn.normalize(dir[0], dir[0]);
			Rn.normalize(dir[1], dir[1]);
		} 
		return dir;
	}
}
