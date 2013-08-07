package de.varylab.varylab.plugin.nurbs.plugin;

import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.JFileChooser.FILES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.python.google.common.collect.Lists;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
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
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.plugin.io.NurbsIO;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.BoundaryLines;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;
import de.varylab.varylab.plugin.nurbs.NURBSSurfaceFactory;
import de.varylab.varylab.plugin.nurbs.NurbsUVCoordinate;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.HalfedgePoint;
import de.varylab.varylab.plugin.nurbs.data.IntObjects;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurves;
import de.varylab.varylab.plugin.nurbs.math.LineSegmentIntersection;
import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;

public class NurbsManagerPlugin extends ShrinkPanelPlugin implements ActionListener {
	

	private HalfedgeInterface 
		hif = null;

	private JFileChooser 
		chooser = new JFileChooser();
	
	private Action
		importAction = new ImportAction();
	
//	private ConjugateLinesPanel
//		conjugateLinesPanel = new ConjugateLinesPanel();
	
	private GeodesicPanel
		geodesicPanel = new GeodesicPanel();
	
	private CurvatureLinesPanel
		curvatureLinesPanel = new CurvatureLinesPanel();
	
	private PointDistancePanel
		pointDistancePanel = new PointDistancePanel();
	
	private JButton
		importButton = new JButton(importAction),
		updateButton = new JButton("update");

	private JTable
		surfacesTable= new JTable(new SurfaceTableModel());
	
	private JScrollPane
		layersScroller = new JScrollPane(surfacesTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
	
	private JToolBar
		surfaceToolbar = new JToolBar();
	
	private ArrayList<NURBSSurface>
		surfaces = new ArrayList<NURBSSurface>();

	private SpinnerNumberModel
		uSpinnerModel = new SpinnerNumberModel(10,0,100,2),
		vSpinnerModel = new SpinnerNumberModel(10,0,100,2);
	
	private JSpinner
		uSpinner = new JSpinner(uSpinnerModel),
		vSpinner = new JSpinner(vSpinnerModel);

	private JCheckBox
		vectorFieldBox = new JCheckBox("vf");
	
	private LinkedList<PolygonalLine>
		lines = new LinkedList<PolygonalLine>();
	
	private LinkedList<PolygonalLine>
	 	removedLines = new LinkedList<PolygonalLine>();
	
	private int 
		curveIndex = 1;
	
	private int 
		activeSurfaceIndex = 0;
	
	private List<double[]> umbilics = new LinkedList<double[]>();

	private PointSelectionPlugin pstool = null;

	
	public NurbsManagerPlugin() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
		
		configureFileChooser();
		importButton.addActionListener(this);
		importButton.setToolTipText("Load Nurbs surface");
		updateButton.addActionListener(this);
		
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new GridLayout());
		tablePanel.add(layersScroller);
		layersScroller.setMinimumSize(new Dimension(30, 150));
		surfacesTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		surfacesTable.setRowHeight(22);
		surfacesTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		
		surfaceToolbar.add(importAction);
		surfaceToolbar.add(new JToolBar.Separator());
		surfaceToolbar.add(vectorFieldBox);
		surfaceToolbar.add(new JToolBar.Separator());
		surfaceToolbar.add(uSpinner);
		surfaceToolbar.add(vSpinner);
		surfaceToolbar.add(new JToolBar.Separator());
		surfaceToolbar.add(updateButton);
		surfaceToolbar.setFloatable(false);
		
		c.weighty = 0.0;
		shrinkPanel.add(surfaceToolbar, c);
		c.weighty = 1.0;
		shrinkPanel.add(tablePanel, c);
		c.weighty = 0.0;
//		shrinkPanel.add(conjugateLinesPanel, c);
//		c.weighty = 0.0;
		shrinkPanel.add(curvatureLinesPanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(geodesicPanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(pointDistancePanel, c);
	}

	private void configureFileChooser() {
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
			}

			@Override
			public String getDescription() {
				return "Wavefront OBJ (*.obj)";
			}
			
			@Override
			public String toString() {
				return getDescription();
			}
		});

		chooser.addChoosableFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
			}

			@Override
			public String getDescription() {
				return "Halfedge Geomtry (*.obj)";
			}
			
			@Override
			public String toString() {
				return getDescription();
			}
		});
	}	
	
	private class ImportAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;

		public ImportAction() {
			
			putValue(SMALL_ICON, ImageHook.getIcon("folder.png"));
			putValue(NAME, "Import");
			putValue(SHORT_DESCRIPTION, "Import");
			
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			chooser.setDialogTitle("Import Into Layer");
			int result = chooser.showOpenDialog(w);
			if (result != JFileChooser.APPROVE_OPTION) return;
			File file = chooser.getSelectedFile();
			try {
				if (file.getName().toLowerCase().endsWith(".obj")) {
					NURBSSurface surface = NurbsIO.readNURBS(new FileReader(file));
					double[] U = surface.getUKnotVector();
					System.out.println("u0 = " + U[0] + "um = " + U[U.length - 1]);
					double[] V = surface.getVKnotVector();
					System.out.println("v0 = " + V[0] + "vn = " + V[V.length - 1]);
					if(surface.getClosingDir() == ClosingDir.uClosed){
						System.out.println("surface.isClosedUDir()");
					}
					if(surface.getClosingDir() == ClosingDir.vClosed){
						System.out.println("surface.isClosedVDir()");
					}
					surface.setName(file.getName());
					surfaces.add(surface);
					activeSurfaceIndex = surfaces.size()-1;
				} 
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(w, ex.getMessage(), ex.getClass().getSimpleName(), ERROR_MESSAGE);
			}
			updateStates();
		}
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
				System.out.println("Select only two vertices!");
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
				LinkedList<double[]> points = IntegralCurves.geodesicSegmentBetweenTwoPoints(surfaces.get(surfacesTable.getSelectedRow()), a, b, eps, tol,nearby);
				PointSetFactory psf = new PointSetFactory();
				int p = surfaces.get(surfacesTable.getSelectedRow()).getUDegree();
				int q = surfaces.get(surfacesTable.getSelectedRow()).getVDegree();
				double[] U = surfaces.get(surfacesTable.getSelectedRow()).getUKnotVector();
				double[] V = surfaces.get(surfacesTable.getSelectedRow()).getVKnotVector();
				double[][][]Pw = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
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
				System.out.println("Geodesic segment length: " + length);
			}else{
				LinkedList<double[]> points = IntegralCurves.geodesicExponentialGivenByTwoPoints(surfaces.get(surfacesTable.getSelectedRow()), a, b, eps, tol,nearby);
				points.addAll(IntegralCurves.geodesicExponentialGivenByTwoPoints(surfaces.get(surfacesTable.getSelectedRow()), b, a, eps, tol, nearby));
				PointSetFactory psf = new PointSetFactory();
				int p = surfaces.get(surfacesTable.getSelectedRow()).getUDegree();
				int q = surfaces.get(surfacesTable.getSelectedRow()).getVDegree();
				double[] U = surfaces.get(surfacesTable.getSelectedRow()).getUKnotVector();
				double[] V = surfaces.get(surfacesTable.getSelectedRow()).getVKnotVector();
				double[][][]Pw = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
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
	
//	private class ConjugateLinesPanel extends ShrinkPanel implements ActionListener{
//		
//		private static final long 
//		serialVersionUID = 1L;
//		
//		private SpinnerNumberModel
//		tolExpModel = new SpinnerNumberModel(-3, -30.0, 0, 1);
//		private JSpinner
//		tolSpinner = new JSpinner(tolExpModel);
//		
//		private JButton
//		goButton = new JButton("Go");
//		
//		private JRadioButton
//		directionButton = new JRadioButton("direction"),
//		conjugateDirectionButton = new JRadioButton("conjugate direction"),
//		intersectionButton = new JRadioButton("Bentley Ottmann");
//		
//		private CurveTableModel
//		curveTableModel = new CurveTableModel();
//		private JTable 
//		curveTable = new JTable(curveTableModel);
//		private JScrollPane 
//		curveScrollPanel = new JScrollPane(curveTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
//		
//		public ConjugateLinesPanel() {
//			super("Conjugate Lines");
//			setShrinked(true);
//			
//			setLayout(new GridBagLayout());
//			GridBagConstraints lc = LayoutFactory.createLeftConstraint();
//			GridBagConstraints rc = LayoutFactory.createRightConstraint();
//			add(new JLabel("Tolerance Exp"), lc);
//			add(tolSpinner, rc);
//			add(directionButton, rc);
//			add(conjugateDirectionButton, rc);
//			add(intersectionButton, rc);
//			add(goButton, rc);
//			goButton.addActionListener(this);
//			curveTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
//			curveTable.setRowHeight(22);
//			curveTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
//			curveScrollPanel.setMinimumSize(new Dimension(100, 150));
//			add(curveScrollPanel, rc);
//			curveTable.getDefaultEditor(Boolean.class).addCellEditorListener(new CurveVisibilityListener());		
//		}
//		
//		public void actionPerformed(ActionEvent e){
//			VHDS hds = hif.get(new VHDS());
//			Set<VVertex> vSet = new TreeSet<VVertex>(new VertexComparator());
//			vSet.addAll(hif.getSelection().getVertices(hds));
//			AdapterSet as = hif.getAdapters();
//			double tol = tolExpModel.getNumber().doubleValue();
//			tol = Math.pow(10, tol);
//			
//			int p = surfaces.get(surfacesTable.getSelectedRow()).getUDegree();
//			int q = surfaces.get(surfacesTable.getSelectedRow()).getVDegree();
//			double[] U = surfaces.get(surfacesTable.getSelectedRow()).getUKnotVector();
//			double[] V = surfaces.get(surfacesTable.getSelectedRow()).getVKnotVector();
//			double[][][]Pw = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
//	
//			boolean max = maxButton.isSelected();
//			boolean min = minButton.isSelected();
//			boolean inter = intersectionButton.isSelected();
//			LinkedList<PolygonalLine> currentLines = new LinkedList<PolygonalLine>();
////			LinkedList<LinkedList<LineSegment>> currentSegments = new LinkedList<LinkedList<LineSegment>>();
//			LinkedList<Integer> umbilicIndex = new LinkedList<Integer>();
//			LinkedList<double[]> boundaryVerts = new LinkedList<double[]>();
//			double[] boundVert1 = new double[2];
//			boundVert1[0] = U[0];
//			boundVert1[1] = V[0];
//			double[] boundVert2 = new double[2];
//			boundVert2[0] = U[U.length - 1];
//			boundVert2[1] = V[0];
//			double[] boundVert3 = new double[2];
//			boundVert3[0] = U[U.length - 1];
//			boundVert3[1] = V[V.length - 1];
//			double[] boundVert4 = new double[2];
//			boundVert4[0] = U[0];
//			boundVert4[1] = V[V.length - 1];
//			boundaryVerts.add(boundVert1);
//			boundaryVerts.add(boundVert2);
//			boundaryVerts.add(boundVert3);
//			boundaryVerts.add(boundVert4);
//			LinkedList<LineSegment> boundarySegments = new LinkedList<LineSegment>();
//
//			double[][] seg1 = new double[2][2];
//			seg1[0] = boundVert1;
//			seg1[1] = boundVert2;
//			LineSegment b1 = new LineSegment(seg1, 1, 1);
//			double[][] seg2 = new double[2][2];
//			seg2[0] = boundVert2;
//			seg2[1] = boundVert3;
//			LineSegment b2 = new LineSegment(seg2, 1, 2);
//			double[][] seg3 = new double[2][2];
//			seg3[0] = boundVert3;
//			seg3[1] = boundVert4;
//			LineSegment b3 = new LineSegment(seg3, 1, 3);
//			double[][] seg4 = new double[2][2];
//			seg4[0] = boundVert4;
//			seg4[1] = boundVert1;
//			LineSegment b4 = new LineSegment(seg4, 1, 4);
//			boundarySegments.add(b1);
//			boundarySegments.add(b2);
//			boundarySegments.add(b3);
//			boundarySegments.add(b4);
//			
//		}
//	}
	
	private class CurvatureLinesPanel extends ShrinkPanel implements ActionListener, PointSelectionListener, HalfedgeListener, ListSelectionListener {
		
		private static final long 
			serialVersionUID = 1L;
		private SpinnerNumberModel
			tolExpModel = new SpinnerNumberModel(-3, -30.0, 0, 1),
			nearUmbilicModel = new SpinnerNumberModel(-2, -30.0, 0, 1);
			
		private JSpinner
			tolSpinner = new JSpinner(tolExpModel),
			nearUmbilicSpinner = new JSpinner(nearUmbilicModel);
		private JButton
			goButton = new JButton("Go");
		private JCheckBox
			immediateCalculationBox = new JCheckBox("Immediate calculation"),
			maxCurvatureBox = new JCheckBox("Max (red)"),
			minCurvatureBox = new JCheckBox("Min (cyan)"),
			intersectionBox = new JCheckBox("Bentley Ottmann");
		private CurveTableModel
			curveTableModel = new CurveTableModel();
		private JTable 
			curveTable = new JTable(curveTableModel);
		private JScrollPane 
			curveScrollPanel = new JScrollPane(curveTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);

		private SceneGraphComponent
			integralCurvesRoot = new SceneGraphComponent("Integral curves root");
		
		private HashMap<PolygonalLine, SceneGraphComponent>
			polygonalLineToSceneGraphComponent = new HashMap<PolygonalLine, SceneGraphComponent>();
	
		public CurvatureLinesPanel() {
			super("Curvature Lines");
			setShrinked(true);
			
			setLayout(new GridBagLayout());
			GridBagConstraints lc = LayoutFactory.createLeftConstraint();
			GridBagConstraints rc = LayoutFactory.createRightConstraint();
			add(new JLabel("Tolerance Exp"), lc);
			add(tolSpinner, rc);
			add(new JLabel("Near Umbilic Exp"), lc);
			add(nearUmbilicSpinner, rc);
//			add(selectionButton, rc);
			add(new JLabel("Curvature lines:"), lc);
			add(minCurvatureBox, lc);
			add(maxCurvatureBox, rc);
			
			add(intersectionBox, rc);
			add(immediateCalculationBox,lc);
			add(goButton, rc);
			goButton.addActionListener(this);
			curveTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
			curveTable.setRowHeight(22);
			curveTable.getSelectionModel().addListSelectionListener(this);
			curveTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			curveScrollPanel.setMinimumSize(new Dimension(100, 150));
			add(curveScrollPanel, rc);
			curveTable.getDefaultEditor(Boolean.class).addCellEditorListener(new CurveVisibilityListener());
			curveTable.getColumnModel().getColumn(0).setPreferredWidth(22);
			curveTable.getColumnModel().getColumn(0).setMaxWidth(22);
			
			Appearance app = new Appearance();
			app.setAttribute(CommonAttributes.EDGE_DRAW, true);
			integralCurvesRoot.setAppearance(app);
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e){
					
			List<double[]> startingPointsUV = pstool.getSelectedPoints();
			
			List<PolygonalLine> currentLines = computeCurvatureLines(startingPointsUV);
			
			lines.addAll(currentLines);
			hif.clearSelection();
	
			if(intersectionBox.isSelected()){
				NURBSSurface ns = surfaces.get(surfacesTable.getSelectedRow());
				lines.removeAll(removedLines);
				// default patch
				LinkedList<LineSegment> allSegments = new LinkedList<LineSegment>();
				for(PolygonalLine pl : lines){
					allSegments.addAll(pl.getpLine());
				}
				int shiftedIndex = allSegments.size();
				List<LineSegment> boundarySegments = ns.getBoundarySegments();
				
				for (LineSegment bs : boundarySegments) {
					bs.setCurveIndex(bs.getCurveIndex() + shiftedIndex);
				}
				allSegments.addAll(boundarySegments);			
				double[] U = ns.getUKnotVector();
				double[] V = ns.getVKnotVector();
		
				allSegments = LineSegmentIntersection.preSelection(U, V, allSegments);
				double firstTimeDouble = System.currentTimeMillis();
//				LinkedList<IntersectionPoint> intersec = LineSegmentIntersection.findIntersections(allSegments);
				double lastTimeDouble = System.currentTimeMillis();
				System.out.println("Double Time: " + (lastTimeDouble - firstTimeDouble));
				double firstTimeBentley = System.currentTimeMillis();
				LinkedList<IntersectionPoint> intersections = LineSegmentIntersection.BentleyOttmannAlgoritm(U, V, allSegments);
				double lastTimeBentley = System.currentTimeMillis();
				System.out.println("Bentley Ottmann Time: " + (lastTimeBentley - firstTimeBentley));
				allSegments.clear();
				LinkedList<HalfedgePoint> hp = LineSegmentIntersection.findAllNbrs(intersections);
				LinkedList<HalfedgePoint> H = LineSegmentIntersection.orientedNbrs(hp);
				FaceSet fS = LineSegmentIntersection.createFaceSet(H,ns.getBoundaryVerticesUV());
				for (int i = 0; i < fS.getVerts().length; i++) {
					double[] S = new double[4];
					ns.getSurfacePoint(fS.getVerts()[i][0], fS.getVerts()[i][1], S);
					fS.getVerts()[i] = S;
				}
				HalfedgeLayer hel = new HalfedgeLayer(hif);
				hel.setName("Curvature Geometry");
//				writeToFile(fS);
				hel.set(fS.getIndexedFaceSet());
				hif.addLayer(hel);
				hif.update();
			}
			
			curveTableModel.fireTableDataChanged();
		}


		private LinkedList<PolygonalLine> computeCurvatureLines(List<double[]> startingPointsUV) {
			NURBSSurface ns = surfaces.get(surfacesTable.getSelectedRow());
			
			double tol = tolExpModel.getNumber().doubleValue();
			tol = Math.pow(10, tol);
			
			double umbilicStop = nearUmbilicModel.getNumber().doubleValue();
			umbilicStop = Math.pow(10, umbilicStop);
			
			LinkedList<PolygonalLine> currentLines = new LinkedList<PolygonalLine>();
			LinkedList<Integer> umbilicIndex = new LinkedList<Integer>();
			
			for(double[] y0 : startingPointsUV) {
					if (maxCurvatureBox.isSelected()){
						curveIndex = curveLine(ns, tol, umbilics, currentLines,
								curveIndex, umbilicIndex, y0, true, umbilicStop);
					}
					if (minCurvatureBox.isSelected()){
						curveIndex = curveLine(ns, tol, umbilics, currentLines,
								curveIndex, umbilicIndex, y0, false, umbilicStop);
					}
			}
			return currentLines;
		}

		@SuppressWarnings("unused")
		private void writeToFile(FaceSet fS) {
			try {
				FileOutputStream fos = new FileOutputStream("test.obj");
				WriterOBJ.write(fS.getIndexedFaceSet(), fos);
				fos.close();
			} catch (Exception e2) {}
		}
		
		private boolean isValidSegment(double[][] seg, double u0, double um, double v0, double vn){
			if((seg[0][0] == u0 && seg[1][0] == um) || (seg[0][0] == um && seg[1][0] == u0)){
				return false;
			}
			else if((seg[0][1] == v0 && seg[1][1] == vn) || (seg[0][1] == vn && seg[1][1] == v0)){
				return false;
			}else{
				return true;
			}
		}

		private int curveLine(NURBSSurface nsurface, double tol, List<double[]> umbilics, List<PolygonalLine> segments, int curveIndex,
				List<Integer> umbilicIndex, double[] y0,
				boolean maxMin,
				double umbilicStop) {
			
			double[] U = nsurface.getUKnotVector();
			double[] V = nsurface.getVKnotVector();
			double u0 = U[0];
			double um = U[U.length - 1];
			double v0 = V[0];
			double vn = V[V.length - 1];
			LinkedList<LineSegment> currentSegments = new LinkedList<LineSegment>();
			IntObjects intObj;
			int noSegment;
			LinkedList<double[]> all = new LinkedList<double[]>();
			List<LineSegment> boundary = nsurface.getBoundarySegments();
			intObj = IntegralCurves.rungeKuttaCurvatureLine(nsurface, y0, tol,false, maxMin, umbilics, umbilicStop, boundary );
			if(intObj.getUmbilicIndex() != 0){
				umbilicIndex.add(intObj.getUmbilicIndex());
			}
			Collections.reverse(intObj.getPoints());
			all.addAll(intObj.getPoints());
			noSegment = all.size();
			System.out.println("first size" + noSegment);
			boolean cyclic = false;
			if(!intObj.isNearby()){
				all.pollLast();
				intObj = IntegralCurves.rungeKuttaCurvatureLine(nsurface, y0, tol,true, maxMin,  umbilics, umbilicStop, boundary);
				if(intObj.getUmbilicIndex() != 0){
					umbilicIndex.add(intObj.getUmbilicIndex());
				}
				all.addAll(intObj.getPoints());
			}else{
				//add the first element of a closed curve
				cyclic = true;
				System.out.println("add first");
				double[] first = new double [2];
				first[0] = all.getFirst()[0];
				first[1] = all.getFirst()[1];
				all.add(first);
				noSegment = all.size();
			}
			int index = 0;
			double[] firstcurvePoint = all.getFirst();
			for (double[] secondCurvePoint : all) {
				index ++;
				if(index != 1){
					double[][]seg = new double[2][];
					seg[0] = firstcurvePoint;
					seg[1] = secondCurvePoint;
					if(isValidSegment(seg, u0, um, v0, vn)){
						LineSegment ls = new  LineSegment();
						ls.setIndexOnCurve(index) ;
						ls.setSegment(seg);
						ls.setCurveIndex(curveIndex);
						ls.setCyclic(cyclic);
						currentSegments.add(ls);
						firstcurvePoint = secondCurvePoint;
					}
					else{
						index--;
					}
				}
			}
			
			PolygonalLine currentLine = new PolygonalLine(currentSegments);
			currentLine.setDescription("("+String.format("%.3f", y0[0]) +", "+String.format("%.3f", y0[1])+")");
			segments.add(currentLine);
			curveIndex ++;
			double[][] u = new double[all.size()][];
			double[][] points = new double[all.size()][];
			for (int i = 0; i < u.length; i++) {
				u[i] = all.get(i);
			}
			for (int i = 0; i < u.length; i++) {
				double[] S = new double[4];
				S = nsurface.getSurfacePoint(u[i][0], u[i][1]);
				points[i] = S;
			}
			IndexedLineSetFactory lsf = IndexedLineSetUtility.createCurveFactoryFromPoints(points, false);
			lsf.update();
			SceneGraphComponent sgc = new SceneGraphComponent("Integral Curve:" + (maxMin?"maximal":"minimal")+" curvature");
			polygonalLineToSceneGraphComponent.put(currentLine, sgc);
			sgc.setGeometry(lsf.getGeometry());
			Appearance labelAp = new Appearance();
			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(labelAp, false);
			DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
			DefaultLineShader lineShader = (DefaultLineShader)dgs.getLineShader();
			if(maxMin){
				pointShader.setDiffuseColor(Color.red);
				lineShader.setDiffuseColor(Color.red);
			}else{
				pointShader.setDiffuseColor(Color.cyan);
				lineShader.setDiffuseColor(Color.cyan);
			}
			sgc.setAppearance(labelAp);
			integralCurvesRoot.addChild(sgc);
			return curveIndex;
		}


		@Override
		public void pointSelected(double[] uv) {
			if(immediateCalculationBox.isSelected()) {
				lines.addAll(computeCurvatureLines(Lists.newArrayList(uv)));
				curveTableModel.fireTableDataChanged();
			}
		}


		@Override
		public void dataChanged(HalfedgeLayer layer) {
			layer.removeTemporaryGeometry(integralCurvesRoot);
			layer.addTemporaryGeometry(integralCurvesRoot);
			resetLines();
		}

		private void resetLines() {
			integralCurvesRoot.removeAllChildren();
			lines.clear();
			curveTableModel.fireTableDataChanged();
			polygonalLineToSceneGraphComponent.clear();
		}


		@Override
		public void adaptersChanged(HalfedgeLayer layer) {
		}


		@Override
		public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
			old.removeTemporaryGeometry(integralCurvesRoot);
			resetLines();
			active.addTemporaryGeometry(integralCurvesRoot);
		}


		@Override
		public void layerCreated(HalfedgeLayer layer) {
		}


		@Override
		public void layerRemoved(HalfedgeLayer layer) {
		}


		@Override
		public void valueChanged(ListSelectionEvent e) {
			EffectiveAppearance ea = hif.getEffectiveAppearance(integralCurvesRoot);
			if(ea == null) {
				return;
			}
			DefaultGeometryShader dgs2 = ShaderUtility.createDefaultGeometryShader(ea);
			DefaultLineShader dls = (DefaultLineShader) dgs2.getLineShader();
			
			for(int i = 0; i < curveTable.getRowCount(); ++i) {
				int mi = curveTable.convertRowIndexToModel(i);
				SceneGraphComponent lineComp = polygonalLineToSceneGraphComponent.get(lines.get(mi));
				boolean isVisible = lineComp.isVisible();
				Appearance app = lineComp.getAppearance();
				app.setAttribute(LINE_SHADER + "." + RADII_WORLD_COORDINATES, dls.getRadiiWorldCoordinates());
				if(curveTable.isRowSelected(i)) {
					app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls.getTubeRadius()*1.5);
				} else {
					app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls.getTubeRadius());					
				}
				lineComp.setVisible(isVisible);
			}
		}
		
		private class CurveTableModel extends DefaultTableModel {

			private static final long serialVersionUID = 1L;

			private String[] columnNames = {"Show", "Curve"};
			
			@Override
			public String getColumnName(int col) {
				return columnNames[col].toString();
		    }
			
		    @Override
			public int getRowCount() { 
		    	return (lines==null)?0:lines.size();
		    }
		    
		    @Override
			public int getColumnCount() { 
		    	return columnNames.length;
		    }
		    
		    @Override
			public Object getValueAt(int row, int col) {
		        if (col == 1) {
		        	String desc = lines.get(row).getDescription();
		        	if(desc == null) {
		        		return "???";
		        	}
		        	return desc;
		        }
		    	return polygonalLineToSceneGraphComponent.get(lines.get(row)).isVisible();
		    }
		    
		    @Override
			public boolean isCellEditable(int row, int col) {
		    	switch (col) {
				case 0:
					return true;
				default:
					return false;
				}
		    }
		    
		    @Override
			public void setValueAt(Object value, int row, int col) {
		    }	
		    
		    @Override
		    public Class<?> getColumnClass(int col) {
		    	if (col == 0)
		    		return Boolean.class;
		    	if (col == 1)
		    		return String.class;
		        return super.getColumnClass(col);
		    }
		}
		
		public class CurveVisibilityListener implements CellEditorListener {

			@Override
			public void editingCanceled(ChangeEvent e) {
			}

			@Override
			public void editingStopped(ChangeEvent e) {
				JTable table = curvatureLinesPanel.curveTable;
				int row = table.getSelectedRow();
				if (table.getRowSorter() != null) {
					row = table.getRowSorter().convertRowIndexToModel(row);
				}
				boolean isVisible = !polygonalLineToSceneGraphComponent.get(lines.get(row)).isVisible();
				polygonalLineToSceneGraphComponent.get(lines.get(row)).setVisible(isVisible);
				if(!isVisible){
					removedLines.add(lines.get(row));
				}
				else{
					removedLines.remove(lines.get(row));
				}
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
				System.out.println("create point");
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
//				System.out.println("point before dragging " + Arrays.toString(point));
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
//				        System.out.println(Arrays.toString(point));
				    
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
				System.out.println("original surface " + ns.toString());
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
				app.setAttribute(CommonAttributes.FACE_DRAW, false);
				app.setAttribute(CommonAttributes.EDGE_DRAW, true);
				app.setAttribute(CommonAttributes.VERTEX_DRAW, true);
				hif.getActiveLayer().addTemporaryGeometry(cmc);
			}
			
			if (pointCreated && goButton == e.getSource()) {
				point = calculateClosestPoint(point);
			}
		}
		
	

		private double[] calculateClosestPoint(double[] point) {
			NURBSSurface ns =  surfaces.get(surfacesTable.getSelectedRow());
			double[] surfacePoint = ns.getClosestPoint(point);
			HalfedgeLayer surfPoint = new HalfedgeLayer(hif);
			surfPoint.setName("Point ");
//			
			hif.addLayer(surfPoint);
			hif.update();
			PointSetFactory psfPoint = new PointSetFactory();
			
			psfPoint.setVertexCount(1);
			
			psfPoint.setVertexCoordinates(surfacePoint);
			psfPoint.update();
			SceneGraphComponent sgcPoint = new SceneGraphComponent("closest point");
			SceneGraphComponent PointComp = new SceneGraphComponent("PointComponent");
			sgcPoint.addChild(PointComp);
			sgcPoint.setGeometry(psfPoint.getGeometry());
			Appearance iAPoint = new Appearance();
			sgcPoint.setAppearance(iAPoint);
			DefaultGeometryShader idgsPoint = ShaderUtility.createDefaultGeometryShader(iAPoint, false);
			DefaultPointShader ipointShaderPoint = (DefaultPointShader)idgsPoint.getPointShader();
			ipointShaderPoint.setDiffuseColor(Color.red);
			hif.getActiveLayer().addTemporaryGeometry(sgcPoint);
			return point;
		}
		
		
		
	}	

	private class SurfaceTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = new String[]{"Name"};
		
		@Override
		public String getColumnName(int col) {
	        return columnNames[col].toString();
	    }
		
	    @Override
		public int getRowCount() { 
	    	return (surfaces==null)?0:surfaces.size();
	    }
	    
	    @Override
		public int getColumnCount() { 
	    	return 1; 
	    }
	    
	    @Override
		public Object getValueAt(int row, int col) {
	        return surfaces.get(row).getName();
	    }
	    
	    @Override
		public boolean isCellEditable(int row, int col) {
	    	return true; 
	    }
	    
	    @Override
		public void setValueAt(Object value, int row, int col) {
	        surfaces.get(row).setName((String)value);
	    }
	}
	
	private void updateStates() {
		surfacesTable.revalidate();
		surfacesTable.getSelectionModel().setSelectionInterval(activeSurfaceIndex,activeSurfaceIndex);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(curvatureLinesPanel);
		pstool  = c.getPlugin(PointSelectionPlugin.class);
		surfacesTable.setModel(new SurfaceTableModel());
		surfacesTable.setSelectionMode(SINGLE_SELECTION);
		surfacesTable.getSelectionModel().setSelectionInterval(activeSurfaceIndex,activeSurfaceIndex);
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
		String chooserDir = chooser.getCurrentDirectory().getAbsolutePath();
		c.storeProperty(getClass(), "importExportLocation", chooserDir);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		String chooserDir = System.getProperty("user.dir");
		chooserDir = c.getProperty(getClass(), "importExportLocation", chooserDir);
		File chooserDirFile = new File(chooserDir);
		if (chooserDirFile.exists()) {
			chooser.setCurrentDirectory(chooserDirFile);
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		Object src = ae.getSource();
		pstool.addPointSelectionListener(this.curvatureLinesPanel);
		if(src == updateButton) {
			NURBSSurfaceFactory qmf = new NURBSSurfaceFactory();
			qmf.setGenerateVertexNormals(true);
			qmf.setGenerateFaceNormals(true);
			qmf.setGenerateEdgesFromFaces(true);
			qmf.setULineCount(uSpinnerModel.getNumber().intValue());
			qmf.setVLineCount(vSpinnerModel.getNumber().intValue());
			NURBSSurface nurbsSurface = surfaces.get(surfacesTable.getSelectedRow());
			qmf.setSurface(nurbsSurface);
			qmf.update();
			hif.set(qmf.getGeometry());
			hif.update();
			hif.addLayerAdapter(qmf.getUVAdapter(), false);
			AdapterSet as = hif.getAdapters();
			VHDS hds = hif.get(new VHDS());
			umbilics = nurbsSurface.findUmbilics(hds, as);
			PointSetFactory psfu = new PointSetFactory();
			double[][] uu = new double[umbilics.size()][];
			double[][] upoints = new double[umbilics.size()][];
			for (int i = 0; i < uu.length; i++) {
				uu[i] = umbilics.get(i);
			}
			psfu.setVertexCount(uu.length);

			for (int i = 0; i < uu.length; i++) {
				double[] S = new double[4];
				nurbsSurface.getSurfacePoint(uu[i][0], uu[i][1], S);
				upoints[i] = S;
			}
			if(umbilics.size()>0){
				psfu.setVertexCoordinates(upoints);
				psfu.update();
				SceneGraphComponent sgcu = new SceneGraphComponent("umbilics");
				SceneGraphComponent umbilicComp = new SceneGraphComponent("Max Curve");
				sgcu.addChild(umbilicComp);
				sgcu.setGeometry(psfu.getGeometry());
				Appearance uAp = new Appearance();
				sgcu.setAppearance(uAp);
				DefaultGeometryShader udgs = ShaderUtility.createDefaultGeometryShader(uAp, false);
				DefaultPointShader upointShader = (DefaultPointShader)udgs.getPointShader();
				upointShader.setDiffuseColor(Color.black);
				hif.getActiveLayer().addTemporaryGeometry(sgcu);
			}
			if(vectorFieldBox.isSelected()) {
				hif.addLayerAdapter(qmf.getMinCurvatureVectorField(),false);
				hif.addLayerAdapter(qmf.getMaxCurvatureVectorField(),false);
			}
			lines.clear();
		} 
	}
	
	
	
//	
	public NURBSSurface getSelectedSurface() {
		NURBSSurface ns = surfaces.get(surfacesTable.getSelectedRow());
		LinkedList<BoundaryLines> boundList = new LinkedList<NURBSSurface.BoundaryLines>();
		boundList.add(BoundaryLines.u0);
		boundList.add(BoundaryLines.um);
		boundList.add(BoundaryLines.v0);
		boundList.add(BoundaryLines.vn);
		ns.setBoundLines(boundList);
		return ns;
	}
	
	@Override
	public void mainUIChanged(String uiClass) {
		super.mainUIChanged(uiClass);
		SwingUtilities.updateComponentTreeUI(chooser);
	}
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addContentUI();
		v.addBasicUI();
		v.registerPlugin(PointSelectionPlugin.class);
		v.registerPlugin(new NurbsManagerPlugin());
		v.startup();
	}
}
