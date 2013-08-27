package de.varylab.varylab.plugin.nurbs.plugin;

import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.JFileChooser.FILES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;

import org.python.google.common.collect.Lists;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jreality.scene.Appearance;
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
import de.jtem.halfedgetools.plugin.misc.VertexEditorPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.io.NurbsIO;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.BoundaryLines;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;
import de.varylab.varylab.plugin.nurbs.NurbsUVCoordinate;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.algorithm.ExtractControlMesh;
import de.varylab.varylab.plugin.nurbs.algorithm.NurbsSurfaceFromMesh;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.HalfedgePoint;
import de.varylab.varylab.plugin.nurbs.data.IntObjects;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurves;
import de.varylab.varylab.plugin.nurbs.math.LineSegmentIntersection;
import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;
import de.varylab.varylab.plugin.nurbs.math.NurbsSurfaceUtility;
import de.varylab.varylab.ui.ListSelectRemoveTable;
import de.varylab.varylab.ui.ListSelectRemoveTableModel;
import de.varylab.varylab.ui.PrettyPrinter;

public class NurbsManagerPlugin extends ShrinkPanelPlugin {
	
	private HalfedgeInterface 
		hif = null;

	private JFileChooser 
		chooser = new JFileChooser();
	
	private Action
		importAction = new ImportAction();
	
	private GeodesicPanel
		geodesicPanel = new GeodesicPanel();
	
	private CurvatureLinesPanel
		curvatureLinesPanel = new CurvatureLinesPanel();
	
	private PointDistancePanel
		pointDistancePanel = new PointDistancePanel();
	
	private JButton
		importButton = new JButton(importAction);
	
	private JToolBar
		surfaceToolbar = new JToolBar();
	
	private int 
		curveIndex = 1;
	
	private List<double[]> umbilics = new LinkedList<double[]>();

	private PointSelectionPlugin pstool = null;

	private NURBSSurface activeNurbsSurface;

	public NurbsManagerPlugin() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
		
		configureFileChooser();
		importButton.setToolTipText("Load Nurbs surface");
		
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new GridLayout());
		
		surfaceToolbar.add(new JLabel("Import Nurbs Surface "));
		surfaceToolbar.add(importAction);
		surfaceToolbar.setFloatable(false);
		
		c.weighty = 0.0;
		shrinkPanel.add(surfaceToolbar, c);
		c.weighty = 1.0;
		shrinkPanel.add(tablePanel, c);
		c.weighty = 0.0;
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
					activeNurbsSurface = surface;
					Icon icon = getPluginInfo().icon != null ? getPluginInfo().icon : ImageHook.getIcon("folder.png");
					NurbsParameterPanel npp = new NurbsParameterPanel(surface);
					int dialogOk = JOptionPane.showConfirmDialog(
						w, npp, getPluginInfo().name, OK_CANCEL_OPTION,	PLAIN_MESSAGE, icon);
					if(dialogOk == JOptionPane.OK_OPTION) {
						NurbsSurfaceUtility.addNurbsMesh(surface, hif.getActiveLayer(),npp.getU(),npp.getV());
						double[][] umbilicalPoints = computeUmbilicalPoints();
						
						if(umbilicalPoints.length>0){
							addUmbilicalPoints(umbilicalPoints,hif.getActiveLayer());
						}
					}
				} 
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(w, ex.getMessage(), ex.getClass().getSimpleName(), ERROR_MESSAGE);
			}
		}
		
		private class NurbsParameterPanel extends JPanel {
			
			private static final long serialVersionUID = 1L;

			private SpinnerNumberModel
				uModel = new SpinnerNumberModel(10,0,100,2),
				vModel = new SpinnerNumberModel(10,0,100,2);
			
			private JSpinner
				uSpinner = new JSpinner(uModel),
				vSpinner = new JSpinner(vModel);
			
			private JPanel
				infoPanel = new JPanel(),
				paramPanel = new JPanel();
	
			public NurbsParameterPanel(NURBSSurface surf) {
				super(new GridBagLayout());
				GridBagConstraints rc = LayoutFactory.createRightConstraint();
				
				add(new JLabel("Surface info"),rc);
				
				infoPanel.setLayout(new GridLayout(4, 1));
				infoPanel.add(new JLabel("u-Degree: " + surf.getUDegree())); 
				infoPanel.add(new JLabel("u-Knots:  " + surf.getUKnotVector().length));
				infoPanel.add(new JLabel("v-Degree: " + surf.getVDegree())); 
				infoPanel.add(new JLabel("v-Knots:  " + surf.getVKnotVector().length));
				
				add(infoPanel,rc);
				
				add(new JSeparator(SwingConstants.HORIZONTAL),rc);
				
				add(new JLabel("Parameters:"),rc);
				
				paramPanel.setLayout(new GridLayout(2,2));
				paramPanel.add(new JLabel("u-Lines")); 
				paramPanel.add(uSpinner);
				paramPanel.add(new JLabel("v-Lines")); 
				paramPanel.add(vSpinner);
				
				add(paramPanel,rc);
			}
			
			public int getU() {
				return uModel.getNumber().intValue();
			}
			
			public int getV() {
				return vModel.getNumber().intValue();
			}
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
				LinkedList<double[]> points = IntegralCurves.geodesicSegmentBetweenTwoPoints(activeNurbsSurface, a, b, eps, tol,nearby);
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
				System.out.println("Geodesic segment length: " + length);
			}else{
				LinkedList<double[]> points = IntegralCurves.geodesicExponentialGivenByTwoPoints(activeNurbsSurface, a, b, eps, tol,nearby);
				points.addAll(IntegralCurves.geodesicExponentialGivenByTwoPoints(activeNurbsSurface, b, a, eps, tol, nearby));
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
	
	private class CurvatureLinesPanel extends ShrinkPanel 
		implements ActionListener, PointSelectionListener, HalfedgeListener, ListSelectionListener, TableModelListener {
		
		private static final long 
			serialVersionUID = 1L;
		
		private SpinnerNumberModel
			tolExpModel = new SpinnerNumberModel(-3, -30.0, 0, 1),
			nearUmbilicModel = new SpinnerNumberModel(-2, -30.0, 0, 1);
			
		private JSpinner
			tolSpinner = new JSpinner(tolExpModel),
			nearUmbilicSpinner = new JSpinner(nearUmbilicModel);
		
		private JButton 
		    intersectionsButton = new JButton("Discretize!"),
			goButton = new JButton("Go");
		
		private JCheckBox
			immediateCalculationBox = new JCheckBox("Immediate"),
			maxCurvatureBox = new JCheckBox("Max (red)"),
			minCurvatureBox = new JCheckBox("Min (cyan)");
		
		private ListSelectRemoveTableModel<PolygonalLine>
			activeModel = new ListSelectRemoveTableModel<PolygonalLine>("Curves", new PolygonalLinePrinter());
		
		private ListSelectRemoveTable<PolygonalLine> 
			curvesTable = new ListSelectRemoveTable<PolygonalLine>(activeModel);
		
		private Map<HalfedgeLayer, ListSelectRemoveTableModel<PolygonalLine>> 
			layers2models = new HashMap<HalfedgeLayer, ListSelectRemoveTableModel<PolygonalLine>>();
		
		private JScrollPane 
			curveScrollPanel = new JScrollPane(curvesTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);

		private SceneGraphComponent
			integralCurvesRoot = new SceneGraphComponent("Integral curves root");
		
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
			add(new JLabel("Curvature lines:"), lc);
			add(minCurvatureBox, lc);
			add(maxCurvatureBox, rc);
			
			add(immediateCalculationBox,lc);
			add(goButton, rc);
			goButton.addActionListener(this);
			
			curvesTable.getSelectionModel().addListSelectionListener(this);
			curvesTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			
			activeModel = curvesTable.getListModel();
			
			activeModel.addTableModelListener(this);
			curveScrollPanel.setMinimumSize(new Dimension(100, 150));
			
			add(curveScrollPanel, rc);

			Appearance app = new Appearance();
			app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
			app.setAttribute(CommonAttributes.EDGE_DRAW, true);
			integralCurvesRoot.setAppearance(app);
			
			intersectionsButton.addActionListener(this);
			add(intersectionsButton,rc);
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e){
					
			Object source = e.getSource();
			
			if(source == goButton) {
				List<double[]> startingPointsUV = pstool.getSelectedPoints();
			
				List<PolygonalLine> currentLines = computeCurvatureLines(startingPointsUV);
			
				activeModel.addAll(currentLines);
				hif.clearSelection();
				activeModel.fireTableDataChanged();
			} else if(source == intersectionsButton) {
				// default patch
				LinkedList<LineSegment> allSegments = new LinkedList<LineSegment>();
				for(PolygonalLine pl : activeModel.getChecked()){
					allSegments.addAll(pl.getpLine());
				}
				int shiftedIndex = allSegments.size();
				List<LineSegment> boundarySegments = activeNurbsSurface.getBoundarySegments();
				
				for (LineSegment bs : boundarySegments) {
					bs.setCurveIndex(bs.getCurveIndex() + shiftedIndex);
				}
				allSegments.addAll(boundarySegments);			
				double[] U = activeNurbsSurface.getUKnotVector();
				double[] V = activeNurbsSurface.getVKnotVector();
		
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
				FaceSet fS = LineSegmentIntersection.createFaceSet(H,activeNurbsSurface.getBoundaryVerticesUV());
				for (int i = 0; i < fS.getVerts().length; i++) {
					double[] S = new double[4];
					activeNurbsSurface.getSurfacePoint(fS.getVerts()[i][0], fS.getVerts()[i][1], S);
					fS.getVerts()[i] = S;
				}
				HalfedgeLayer hel = new HalfedgeLayer(hif);
				hel.setName("Curvature Geometry");
				hif.addLayer(hel);
				hel.set(fS.getIndexedFaceSet());
				hif.update();
			}
		}


		private LinkedList<PolygonalLine> computeCurvatureLines(List<double[]> startingPointsUV) {
			
			double tol = tolExpModel.getNumber().doubleValue();
			tol = Math.pow(10, tol);
			
			double umbilicStop = nearUmbilicModel.getNumber().doubleValue();
			umbilicStop = Math.pow(10, umbilicStop);
			
			List<double[]> boundaryVertices = activeNurbsSurface.getBoundaryVerticesUV();
			double scale = Rn.euclideanDistance(boundaryVertices.get(0), boundaryVertices.get(2));
			tol *= scale;
			umbilicStop *= scale;
			
			LinkedList<PolygonalLine> currentLines = new LinkedList<PolygonalLine>();
			LinkedList<Integer> umbilicIndex = new LinkedList<Integer>();
			
			for(double[] y0 : startingPointsUV) {
					if (maxCurvatureBox.isSelected()){
						curveIndex = curveLine(activeNurbsSurface, tol, umbilics, currentLines,
								curveIndex, umbilicIndex, y0, true, umbilicStop);
					}
					if (minCurvatureBox.isSelected()){
						curveIndex = curveLine(activeNurbsSurface, tol, umbilics, currentLines,
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
			currentLine.setDescription((maxMin?"max:":"min:") + "("+String.format("%.3f", y0[0]) +", "+String.format("%.3f", y0[1])+")");
			segments.add(currentLine);
			curveIndex ++;
			return curveIndex;
		}


		private SceneGraphComponent createLineComponent(PolygonalLine pl) {
			List<LineSegment> segments = pl.getpLine();
			double u,v;
			double[][] points = new double[segments.size()+1][];
			int i = 0;
			for(LineSegment segment : segments) {
				u = (segment.getSegment())[0][0];
				v = (segment.getSegment())[0][1];
				points[i] = activeNurbsSurface.getSurfacePoint(u, v);
				++i;
				if(i == segments.size()) {
					u = (segment.getSegment())[1][0];
					v = (segment.getSegment())[1][1];
					points[i] = activeNurbsSurface.getSurfacePoint(u, v);
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


		@Override
		public void pointSelected(double[] uv) {
			if(immediateCalculationBox.isSelected()) {
				LinkedList<PolygonalLine> curvatureLines = computeCurvatureLines(Lists.newArrayList(uv));
				for(PolygonalLine pl : curvatureLines) {
					if(!activeModel.contains(pl)) {
						activeModel.add(pl);
//						integralCurvesRoot.addChild(createLineComponent(pl));
					}
				}
				activeModel.fireTableDataChanged();
			}
		}


		@Override
		public void dataChanged(HalfedgeLayer layer) {
			updateActiveNurbsSurface(layer);
			layer.addTemporaryGeometry(integralCurvesRoot);
			if(activeModel == null) {
				if(layers2models.containsKey(layer)) {
					activeModel = layers2models.get(layer);
					activeModel.clear();
				} else {
					activeModel = new ListSelectRemoveTableModel<PolygonalLine>("Initial Point", new PolygonalLinePrinter());
					activeModel.addTableModelListener(this);
					layers2models.put(layer,activeModel);
				}
			}
			activeModel.fireTableDataChanged();
		}

//		private void resetLines() {
//			integralCurvesRoot.removeAllChildren();
//			activeModel.clear();
//			activeModel.fireTableDataChanged();
//		}


		@Override
		public void adaptersChanged(HalfedgeLayer layer) {
			updateActiveNurbsSurface(layer);
			if(activeNurbsSurface == null) {
				activeModel.clear();
				activeModel.fireTableDataChanged();
			}
		}


		@Override
		public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
			if(old == active) {
				return;
			}
			old.removeTemporaryGeometry(integralCurvesRoot);
			active.addTemporaryGeometry(integralCurvesRoot);
			
			layers2models.put(old,activeModel);
			if(layers2models.containsKey(active)) {
				activeModel = layers2models.get(active);
			} else {
				System.err.println("this should not happen!");
			}
			curvesTable.setModel(activeModel);
			updateActiveNurbsSurface(active);
			activeModel.fireTableDataChanged();
		}


		private void updateActiveNurbsSurface(HalfedgeLayer layer) {
			AdapterSet as = layer.getAdapters();
			as.addAll(layer.getActiveVolatileAdapters());
			NurbsUVAdapter nurbsUVAdapter = as.query(NurbsUVAdapter.class);
			if(nurbsUVAdapter == null) {
				nurbsUVAdapter = layer.getActiveAdapters().query(NurbsUVAdapter.class);
			}
			
			if(nurbsUVAdapter != null) {
				activeNurbsSurface = nurbsUVAdapter.getSurface(); 
			} else {
				activeNurbsSurface = null;
			}
		}


		@Override
		public void layerCreated(HalfedgeLayer layer) {
			ListSelectRemoveTableModel<PolygonalLine> newModel = new ListSelectRemoveTableModel<PolygonalLine>("Initial Point", new PolygonalLinePrinter());
			newModel.addTableModelListener(this);
			layers2models.put(layer,newModel);
		}


		@Override
		public void layerRemoved(HalfedgeLayer layer) {
			layers2models.remove(layer);
		}


		@Override
		public void valueChanged(ListSelectionEvent e) {
			EffectiveAppearance ea = hif.getEffectiveAppearance(integralCurvesRoot);
			if(ea == null) {
				return;
			}
			DefaultGeometryShader dgs2 = ShaderUtility.createDefaultGeometryShader(ea);
			DefaultLineShader dls = (DefaultLineShader) dgs2.getLineShader();
			
			for(int i = 0; i < curvesTable.getRowCount(); ++i) {
				int mi = curvesTable.convertRowIndexToModel(i);
				SceneGraphComponent lineComp = integralCurvesRoot.getChildComponent(mi);
				boolean isVisible = lineComp.isVisible();
				Appearance app = lineComp.getAppearance();
				app.setAttribute(LINE_SHADER + "." + RADII_WORLD_COORDINATES, dls.getRadiiWorldCoordinates());
				if(curvesTable.isRowSelected(i)) {
					app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls.getTubeRadius()*1.5);
				} else {
					app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls.getTubeRadius());					
				}
				lineComp.setVisible(isVisible);
			}
		}


		@Override
		public void tableChanged(TableModelEvent e) {
			resetIntegralCurvesComponent();
			curvesTable.adjustColumnSizes();
		}


		private void resetIntegralCurvesComponent() {
			integralCurvesRoot.removeAllChildren();
			for(PolygonalLine pl : activeModel.getList()) {
				SceneGraphComponent lineComponent = createLineComponent(pl);
				integralCurvesRoot.addChild(lineComponent);
				if(!activeModel.isChecked(pl)) {
					lineComponent.setVisible(false);
				}
			}
		}
		
		private class PolygonalLinePrinter implements PrettyPrinter<PolygonalLine> {

			@Override
			public String toString(PolygonalLine t) {
				return t.getDescription();
			}
			
		}
	}
	
	private class PointDistancePanel extends ShrinkPanel implements ActionListener{
		
		private static final long 
			serialVersionUID = 1L;
		private JButton
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
			goButton.addActionListener(this);
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
			
			if (pointCreated && goButton == e.getSource()) {
				point = calculateClosestPoint(point);
			}
		}
		
	

		private double[] calculateClosestPoint(double[] point) {
			double[] surfacePoint = activeNurbsSurface.getClosestPoint(point);
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
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(curvatureLinesPanel);
		pstool  = c.getPlugin(PointSelectionPlugin.class);
		pstool.addPointSelectionListener(curvatureLinesPanel);
		c.getPlugin(ExtractControlMesh.class);
		c.getPlugin(NurbsSurfaceFromMesh.class);
		c.getPlugin(VertexEditorPlugin.class);
		c.getPlugin(QuadMeshGenerator.class);
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
		c.storeProperty(getClass(), "calculateMaxCurve", curvatureLinesPanel.maxCurvatureBox.isSelected());
		c.storeProperty(getClass(), "calculateMinCurve", curvatureLinesPanel.minCurvatureBox.isSelected());
		c.storeProperty(getClass(), "immediateCurveCalculation", curvatureLinesPanel.immediateCalculationBox.isSelected());
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
		curvatureLinesPanel.maxCurvatureBox.setSelected(c.getProperty(getClass(), "calculateMaxCurve",curvatureLinesPanel.maxCurvatureBox.isSelected()));
		curvatureLinesPanel.minCurvatureBox.setSelected(c.getProperty(getClass(), "calculateMinCurve",curvatureLinesPanel.minCurvatureBox.isSelected()));
		curvatureLinesPanel.immediateCalculationBox.setSelected(c.getProperty(getClass(),"immediateCurveCalculation",curvatureLinesPanel.immediateCalculationBox.isSelected()));
	}

	private void addUmbilicalPoints(double[][] umbillicPoints, HalfedgeLayer layer) {
		PointSetFactory psfu = new PointSetFactory();
		psfu.setVertexCount(umbillicPoints.length);
		psfu.setVertexCoordinates(umbillicPoints);
		psfu.update();
		SceneGraphComponent sgcu = new SceneGraphComponent("Umbilics");
		sgcu.setGeometry(psfu.getGeometry());
		Appearance uAp = new Appearance();
		uAp.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		sgcu.setAppearance(uAp);
		DefaultGeometryShader udgs = ShaderUtility.createDefaultGeometryShader(uAp, false);
		DefaultPointShader upointShader = (DefaultPointShader)udgs.getPointShader();
		upointShader.setDiffuseColor(Color.pink);
		layer.addTemporaryGeometry(sgcu);
		
	}

	private double[][] computeUmbilicalPoints() {
		AdapterSet as = hif.getAdapters();
		as.addAll(hif.getActiveVolatileAdapters());
		VHDS hds = hif.get(new VHDS());
		umbilics = activeNurbsSurface.findUmbilics(hds, as);
		double[][] uu = new double[umbilics.size()][];
		double[][] upoints = new double[umbilics.size()][];
		for (int i = 0; i < uu.length; i++) {
			uu[i] = umbilics.get(i);
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
	
	@Override
	public void mainUIChanged(String uiClass) {
		super.mainUIChanged(uiClass);
		SwingUtilities.updateComponentTreeUI(chooser);
	}
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addContentUI();
		v.addBasicUI();
		v.registerPlugin(new NurbsManagerPlugin());
		v.startup();
	}
}
