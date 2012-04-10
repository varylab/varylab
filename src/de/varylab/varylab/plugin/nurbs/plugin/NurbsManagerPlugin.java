package de.varylab.varylab.plugin.nurbs.plugin;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

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
import de.jreality.shader.DefaultPointShader;
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
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariables;
import de.jtem.numericalMethods.calculus.minimizing.Info;
import de.jtem.numericalMethods.calculus.minimizing.NelderMead;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.io.NurbsIO;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurfaceFactory;
import de.varylab.varylab.plugin.nurbs.NurbsUVCoordinate;
import de.varylab.varylab.plugin.nurbs.PointAndDistance;
import de.varylab.varylab.plugin.nurbs.VertexComparator;
import de.varylab.varylab.plugin.nurbs.adapter.FlatIndexFormAdapter;
import de.varylab.varylab.plugin.nurbs.adapter.VectorFieldMapAdapter;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.HalfedgePoint;
import de.varylab.varylab.plugin.nurbs.data.IntObjects;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurves;
import de.varylab.varylab.plugin.nurbs.math.LineSegmentIntersection;
import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;
import de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility;

public class NurbsManagerPlugin extends ShrinkPanelPlugin implements ActionListener{
	

	private HalfedgeInterface 
		hif = null;
	
	private VectorFieldMapAdapter 
		linefield = new VectorFieldMapAdapter();
	
	private FlatIndexFormAdapter 
		indexAdapter= new FlatIndexFormAdapter(); 
	
	private JFileChooser 
		chooser = new JFileChooser();
	
	private Action
		importAction = new ImportAction();
	
	private GeodesicPanel
		geodesicPanel = new GeodesicPanel();
	
	private CurvatureLinesPanel
		curvatureLinesPanel = new CurvatureLinesPanel();
	
	private PolygonalCurvePanel
		polygonalCurvePanel = new PolygonalCurvePanel();
	
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
		uSpinnerModel = new SpinnerNumberModel(30,0,100,2),
		vSpinnerModel = new SpinnerNumberModel(30,0,100,2);
	
	private JSpinner
		uSpinner = new JSpinner(uSpinnerModel),
		vSpinner = new JSpinner(vSpinnerModel);

	private JCheckBox
		vectorFieldBox = new JCheckBox("vf");
	
	private LinkedList<LineSegment> 
		segments = new LinkedList<LineSegment>();
	
	private int 
		curveIndex = 1;
	
	private int 
		activeSurfaceIndex = 0;
	
	private List<double[]> umbilics = new LinkedList<double[]>();

	
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
		shrinkPanel.add(curvatureLinesPanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(geodesicPanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(polygonalCurvePanel, c);
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
			putValue(NAME, "Import");
			putValue(SMALL_ICON, ImageHook.getIcon("folder.png"));
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
					surface.setName(file.getName());
					surfaces.add(surface);
					activeSurfaceIndex = surfaces.size()-1;
				} 
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(w, ex.getMessage(), ex.getClass().getSimpleName(), ERROR_MESSAGE);
			}
			updateStates();
		}
	}
	
	private class GeodesicPanel extends ShrinkPanel implements ActionListener{
		
		private static final long 
			serialVersionUID = 1L;
		private SpinnerNumberModel
			tolExpModel = new SpinnerNumberModel(-2, -30.0, 0, 1),
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
	
	private class CurvatureLinesPanel extends ShrinkPanel implements ActionListener{
		
		private static final long 
			serialVersionUID = 1L;
		private SpinnerNumberModel
			tolExpModel = new SpinnerNumberModel(-2, -30.0, 0, 1),
			epsExpModel = new SpinnerNumberModel(-2, -30.0, 0, 1),
			stepSizeModel = new SpinnerNumberModel(-3, -30.0, 0, 1),
			nearUmbilicModel = new SpinnerNumberModel(-2, -30.0, 0, 1);
			
		private JSpinner
			tolSpinner = new JSpinner(tolExpModel),
			epsSpinner = new JSpinner(epsExpModel),
			stepSizeSpinner = new JSpinner(stepSizeModel),
			nearUmbilicSpinner = new JSpinner(nearUmbilicModel);
		private JButton
			goButton = new JButton("Go");
		private JRadioButton
			maxButton = new JRadioButton("Max Curvature (red)"),
			minButton = new JRadioButton("Min Curvature (cyan)"),
			intersectionButton = new JRadioButton("Bentley Ottmann");
		
		 
		public CurvatureLinesPanel() {
			super("Curvature Lines");
			setShrinked(true);
			setLayout(new GridBagLayout());
			GridBagConstraints lc = LayoutFactory.createLeftConstraint();
			GridBagConstraints rc = LayoutFactory.createRightConstraint();
			add(new JLabel("Tolerance Exp"), lc);
			add(tolSpinner, rc);
			add(new JLabel("Eps Exp"), lc);
			add(epsSpinner, rc);
			add(new JLabel("Step Size Exp"), lc);
			add(stepSizeSpinner, rc);
			add(new JLabel("Near Umbilic Exp"), lc);
			add(nearUmbilicSpinner, rc);
			add(maxButton, rc);
			add(minButton, rc);
			add(intersectionButton, rc);
			add(goButton, rc);
			
			goButton.addActionListener(this);
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e){
			double tol = tolExpModel.getNumber().doubleValue();
			tol = Math.pow(10, tol);
			double eps = epsExpModel.getNumber().doubleValue();
			eps = Math.pow(10, eps);
			double stepSize = stepSizeModel.getNumber().doubleValue();
			stepSize = Math.pow(10, stepSize);
			double umbilicStop = nearUmbilicModel.getNumber().doubleValue();
			umbilicStop = Math.pow(10, umbilicStop);
			
			VHDS hds = hif.get(new VHDS());
			Set<VVertex> vSet = new TreeSet<VVertex>(new VertexComparator());
			vSet.addAll(hif.getSelection().getVertices(hds));
			AdapterSet as = hif.getAdapters();
			
			for (double[] umb : umbilics) {
				System.out.println();
				System.out.println("umbilic: " + Arrays.toString(umb));
				System.out.println();
			}
			

			int p = surfaces.get(surfacesTable.getSelectedRow()).getUDegree();
			int q = surfaces.get(surfacesTable.getSelectedRow()).getVDegree();
			double[] U = surfaces.get(surfacesTable.getSelectedRow()).getUKnotVector();
			double[] V = surfaces.get(surfacesTable.getSelectedRow()).getVKnotVector();
			double[][][]Pw = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
			boolean max = maxButton.isSelected();
			boolean min = minButton.isSelected();
			boolean inter = intersectionButton.isSelected();
			LinkedList<LineSegment> currentSegments = new LinkedList<LineSegment>();
			LinkedList<Integer> umbilicIndex = new LinkedList<Integer>();
			

			for(VVertex v : vSet) {
				double[] y0 = as.getD(NurbsUVCoordinate.class, v);
					if (max){
						curveIndex = curveLine(tol, eps, stepSize, umbilics, p, q,
								U, V, Pw, currentSegments, curveIndex, umbilicIndex, y0, true, umbilicStop);
					}
					if (min){
						curveIndex = curveLine(tol, eps, stepSize, umbilics, p, q,
								U, V, Pw, currentSegments, curveIndex, umbilicIndex, y0, false, umbilicStop);
					}
			}
			segments.addAll(currentSegments);
			hif.clearSelection();
			
			if(inter){
				// default patch
				LinkedList<LineSegment> allSegments = new LinkedList<LineSegment>(segments);
				LinkedList<LineSegment> boundarySegments = new LinkedList<LineSegment>();
//				double[][] seg1 = {{0,0},{1,0}};
				double[][] seg1 = new double[2][2];
				seg1[0][0] = U[0];
				seg1[0][1] = V[0];
				seg1[1][0] = U[U.length - 1];
				seg1[1][1] = V[0];
				LineSegment b1 = new LineSegment(seg1, 1, 1);
//				double[][] seg2 = {{1,0},{1,1}};
				double[][] seg2 = new double[2][2];
				seg2[0][0] = U[U.length - 1];
				seg2[0][1] = V[0];
				seg2[1][0] = U[U.length - 1];
				seg2[1][1] = V[V.length - 1];
				LineSegment b2 = new LineSegment(seg2, 1, 2);
//				double[][] seg3 = {{1,1},{0,1}};
				double[][] seg3 = new double[2][2];
				seg3[0][0] = U[U.length - 1];
				seg3[0][1] = V[V.length - 1];
				seg3[1][0] = U[0];
				seg3[1][1] = V[V.length - 1];
				LineSegment b3 = new LineSegment(seg3, 1, 3);
//				double[][] seg4 = {{0,1},{0,0}};
				double[][] seg4 = new double[2][2];
				seg4[0][0] = U[0];
				seg4[0][1] = V[V.length - 1];
				seg4[1][0] = U[0];
				seg4[1][1] = V[0];
				LineSegment b4 = new LineSegment(seg4, 1, 4);
				boundarySegments.add(b1);
				boundarySegments.add(b2);
				boundarySegments.add(b3);
				boundarySegments.add(b4);
				int shiftedIndex = allSegments.size();
				for (LineSegment bs : boundarySegments) {
					bs.setCurveIndex(bs.getCurveIndex() + shiftedIndex);
				}
				allSegments.addAll(boundarySegments);
//				String str = new String();
//				try {
//					System.out.println("PRINT OUT START");
//					FileWriter testOut = new FileWriter("testSegmentsNew.txt");
//					int ijk = 0;
//					for (LineSegment ls : allSegments) {
//						++ijk;
//						str = ls.getSegment()[0][0] + " " + ls.getSegment()[0][1] + " " + ls.getSegment()[1][0] + " " + ls.getSegment()[1][1] + "\n";
//						testOut.write(str);
//						if(ijk%10000 == 0) {
//							System.out.print(".");
//						}
//					}
//					testOut.close();
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//				double firstTimeDouble = System.currentTimeMillis();
//				LinkedList<IntersectionPoint> intersec = LineSegmentIntersection.findIntersections(allSegments);
//				double lastTimeDouble = System.currentTimeMillis();
//				System.out.println("Double Time: " + (lastTimeDouble - firstTimeDouble));
//				double firstTimeBentley = System.currentTimeMillis();
//				LinkedList<IntersectionPoint> intersections = LineSegmentIntersection.BentleyOttmannAlgoritm(allSegments);
//				double lastTimeBentley = System.currentTimeMillis();
//				System.out.println("Bentley Ottmann Time: " + (lastTimeBentley - firstTimeBentley));				
				System.out.println("mit preSelection");				
				allSegments = LineSegmentIntersection.preSelection(U, V, allSegments);
	
				for (LineSegment ls : allSegments) {
					System.out.println("linesegment");
					System.out.println("start "+Arrays.toString(ls.getSegment()[0])+ "end "+Arrays.toString(ls.getSegment()[1]));
				}
				System.out.println("U " + Arrays.toString(U));
				System.out.println("V " + Arrays.toString(V));
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
				FaceSet fS = LineSegmentIntersection.createFaceSet(H);
				for (int i = 0; i < fS.getVerts().length; i++) {
					double[] S = new double[4];
					NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, fS.getVerts()[i][0], fS.getVerts()[i][1], S);
					fS.getVerts()[i] = S;
				}
				HalfedgeLayer hel = new HalfedgeLayer(hif);
				hel.setName("Curvature Geometry");
				try {
					FileOutputStream fos = new FileOutputStream("test.obj");
					WriterOBJ.write(fS.getIndexedFaceSet(), fos);
					fos.close();
				} catch (Exception e2) {}
				hel.set(fS.getIndexedFaceSet());
				hif.addLayer(hel);
				hif.update();
				PointSetFactory psfi = new PointSetFactory();
				
				double[][] iu = new double[intersections.size()][];
				double[][] ipoints = new double[intersections.size()][];
				int c = 0;
				for (IntersectionPoint ip : intersections) {
					iu[c] = ip.getPoint();
					c++;
				}
				psfi.setVertexCount(iu.length);

				for (int i = 0; i < iu.length; i++) {
					double[] S = new double[4];
					NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, iu[i][0], iu[i][1], S);
					ipoints[i] = S;
				}
				
				
				if(intersections.size()>0){
					psfi.setVertexCoordinates(ipoints);
					psfi.update();
					SceneGraphComponent sgci = new SceneGraphComponent("intersection");
					SceneGraphComponent intersectionComp = new SceneGraphComponent("Intersection");
					sgci.addChild(intersectionComp);
					sgci.setGeometry(psfi.getGeometry());
					Appearance iAp = new Appearance();
					sgci.setAppearance(iAp);
					DefaultGeometryShader idgs = ShaderUtility.createDefaultGeometryShader(iAp, false);
					DefaultPointShader ipointShader = (DefaultPointShader)idgs.getPointShader();
					ipointShader.setDiffuseColor(Color.black);
					hif.getActiveLayer().addTemporaryGeometry(sgci);
				}
			}
		}

		private int curveLine(double tol, double eps, double stepSize,
				List<double[]> umbilics, int p, int q, double[] U,
				double[] V, double[][][] Pw,
				LinkedList<LineSegment> segments, int curveIndex,
				LinkedList<Integer> umbilicIndex, double[] y0, boolean maxMin, double umbilicStop) {
			IntObjects intObj;
			int noSegment;
			LinkedList<double[]> all = new LinkedList<double[]>();
			intObj = IntegralCurves.rungeKutta(surfaces.get(surfacesTable.getSelectedRow()), y0, tol,false, maxMin,eps,stepSize,umbilics, umbilicStop);
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
				intObj = IntegralCurves.rungeKutta(surfaces.get(surfacesTable.getSelectedRow()), y0, tol,true, maxMin,eps,stepSize, umbilics, umbilicStop);
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
					LineSegment ls = new  LineSegment();
					ls.setIndexOnCurve(index) ;
					ls.setSegment(seg);
					ls.setCurveIndex(curveIndex);
					ls.setCyclic(cyclic);
					segments.add(ls);
					firstcurvePoint = secondCurvePoint;
				}
			}
			
			curveIndex ++;
			PointSetFactory psf = new PointSetFactory();
			double[][] u = new double[all.size()][];
			double[][] points = new double[all.size()][];
			for (int i = 0; i < u.length; i++) {
				u[i] = all.get(i);
			}
			psf.setVertexCount(u.length);
			for (int i = 0; i < u.length; i++) {
				double[] S = new double[4];
				NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, u[i][0], u[i][1], S);
				points[i] = S;
			}
			psf.setVertexCoordinates(points);
			psf.update();
			SceneGraphComponent sgc = new SceneGraphComponent("Integral Curve");
			SceneGraphComponent maxCurveComp = new SceneGraphComponent("Max Curve");
			sgc.addChild(maxCurveComp);
			sgc.setGeometry(psf.getGeometry());
			Appearance labelAp = new Appearance();
			sgc.setAppearance(labelAp);
			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(labelAp, false);
			DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
			if(maxMin){
				pointShader.setDiffuseColor(Color.red);
			}else{
				pointShader.setDiffuseColor(Color.cyan);
			}
			hif.getActiveLayer().addTemporaryGeometry(sgc);
			return curveIndex;
		}
	}
	
	private class PolygonalCurvePanel extends ShrinkPanel implements ActionListener{
		
		private static final long 
		serialVersionUID = 1L;
		
		public PolygonalCurvePanel() {
			super("Polygonal Curves");
			setShrinked(true);
			setLayout(new GridBagLayout());
		}
		
		@Override
		public void actionPerformed(ActionEvent e){
			
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
		
		private double[] point = {1,3,1,1};
		
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
				System.out.println("point before dragging " + Arrays.toString(point));
				DragEventTool t = new DragEventTool();
				t.addPointDragListener(new PointDragListener() {
	
					public void pointDragStart(PointDragEvent e) {
					}
	//
					public void pointDragged(PointDragEvent e) {
						PointSet pointSet = e.getPointSet();
						
						double[][] points=new double[pointSet.getNumPoints()][];
				        pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
				        points[e.getIndex()]=e.getPosition(); 
				        point = e.getPosition().clone();
				        point[3] = 1.;
				        pointSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));
				        
					}
	//
					public void pointDragEnd(PointDragEvent e) {
					}
//					
				});
//				
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
//				LinkedList<NURBSSurface> fourPatches = decomposed.subdivideIntoFourNewPatches();
//				System.out.println("four new patches:");
//				for (NURBSSurface p : fourPatches) {
//					System.out.println(p.toString());
//					double[][][] cmP = p.getControlMesh();
//					QuadMeshFactory qmfP = new QuadMeshFactory();
//					qmfP.setULineCount(cmP.length);
//					qmfP.setVLineCount(cmP[0].length);
//					qmfP.setVertexCoordinates(cmP);
//					qmfP.setGenerateEdgesFromFaces(true);
//					qmfP.update();
//					IndexedFaceSet ifsP = qmfP.getIndexedFaceSet();
//					SceneGraphComponent cmcP = new SceneGraphComponent("Control Mesh");
//					cmcP.setGeometry(ifsP);
//					Appearance appP = new Appearance();
//					cmcP.setAppearance(appP);
//					appP.setAttribute(CommonAttributes.FACE_DRAW, false);
//					appP.setAttribute(CommonAttributes.EDGE_DRAW, true);
//					appP.setAttribute(CommonAttributes.VERTEX_DRAW, true);
//					hif.getActiveLayer().addTemporaryGeometry(cmcP);
//					
//				}
			}
			
			if (pointCreated && goButton == e.getSource()) {
				point = calculateClosestPoint(point);
			}
		}
		
	

		private double[] calculateClosestPoint(double[] point) {
			NURBSSurface ns =  surfaces.get(surfacesTable.getSelectedRow());
			System.out.println(ns.toString());
		
			
//			DragEventTool t = new DragEventTool();
//			t.addPointDragListener(new PointDragListener() {
//
//				public void pointDragStart(PointDragEvent e) {
//					System.out.println("drag start of vertex no "+e.getIndex());				
//				}
//
//				public void pointDragged(PointDragEvent e) {
//					PointSet pointSet = e.getPointSet();
//					
//					double[][] points=new double[pointSet.getNumPoints()][];
//			        pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
//			        System.out.println("point before dragging in tool: " + Arrays.toString(points[0]));
//			        points[e.getIndex()]=e.getPosition();  
//			        pointSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));
////			        psfi.setVertexAttribute(Attribute.COORDINATES, points);
////			        pointSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createWritableDataList(points));
////			        pointSet.setVertexAttributes(Attribute.COORDINATES,points);
////			        DataList
//			        System.out.println("point after dragging in tool: " + Arrays.toString(points[0]));
//			        
//				}
//
//				public void pointDragEnd(PointDragEvent e) {
//				}
//				
//			});
//			
//			sgci.addTool(t);
			
//			System.out.println("point before dragging: " + Arrays.toString(point));
//			PointSet pointSet = psfi.getPointSet();
//			double[][] points = new double[pointSet.getNumPoints()][];
//	        pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
//	        point = points[0];
//	        System.out.println("point after dragging: " + Arrays.toString(point));
			
			PointAndDistance pad = ns.getDistanceBetweenPointAndSurface(point);
			double[] surfacePoint = pad.getPoint();
			System.out.println("closest point: " + Arrays.toString(surfacePoint));
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
		hif.addAdapter(linefield, true);
		hif.addAdapter(indexAdapter, true);
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
		if(src == updateButton) {
			NURBSSurfaceFactory qmf = new NURBSSurfaceFactory();
			qmf.setGenerateVertexNormals(true);
			qmf.setGenerateFaceNormals(true);
			qmf.setGenerateEdgesFromFaces(true);
			qmf.setULineCount(uSpinnerModel.getNumber().intValue());
			qmf.setVLineCount(vSpinnerModel.getNumber().intValue());
			qmf.setSurface(surfaces.get(surfacesTable.getSelectedRow()));
			qmf.update();
			hif.set(qmf.getGeometry());
			hif.update();
			hif.addLayerAdapter(qmf.getUVAdapter(), false);
//			AdapterSet as = hif.getAdapters();
//			VHDS hds = hif.get(new VHDS());
//			umbilics = findUmbilics(hds, as);
//			PointSetFactory psfu = new PointSetFactory();
//			int p = surfaces.get(surfacesTable.getSelectedRow()).getUDegree();
//			int q = surfaces.get(surfacesTable.getSelectedRow()).getVDegree();
//			double[] U = surfaces.get(surfacesTable.getSelectedRow()).getUKnotVector();
//			double[] V = surfaces.get(surfacesTable.getSelectedRow()).getVKnotVector();
//			double[][][]Pw = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
//			double[][] uu = new double[umbilics.size()][];
//			double[][] upoints = new double[umbilics.size()][];
//			for (int i = 0; i < uu.length; i++) {
//				uu[i] = umbilics.get(i);
//			}
//			psfu.setVertexCount(uu.length);
//
//			for (int i = 0; i < uu.length; i++) {
//				double[] S = new double[4];
//				NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, uu[i][0], uu[i][1], S);
//				upoints[i] = S;
//			}
//			if(umbilics.size()>0){
//			psfu.setVertexCoordinates(upoints);
//			psfu.update();
//			SceneGraphComponent sgcu = new SceneGraphComponent("umbilics");
//			SceneGraphComponent umbilicComp = new SceneGraphComponent("Max Curve");
//			sgcu.addChild(umbilicComp);
//			sgcu.setGeometry(psfu.getGeometry());
//			Appearance uAp = new Appearance();
//			sgcu.setAppearance(uAp);
//			DefaultGeometryShader udgs = ShaderUtility.createDefaultGeometryShader(uAp, false);
//			DefaultPointShader upointShader = (DefaultPointShader)udgs.getPointShader();
//			upointShader.setDiffuseColor(Color.black);
//			hif.getActiveLayer().addTemporaryGeometry(sgcu);
//			}
//			if(vectorFieldBox.isSelected()) {
//				hif.addLayerAdapter(qmf.getMinCurvatureVectorField(),false);
//				hif.addLayerAdapter(qmf.getMaxCurvatureVectorField(),false);
//			}
			segments.clear();
		} 
	}
	
	
//	private LinkedList<double[]> findUmbilics(VHDS hds, AdapterSet as) {
//		NURBSSurface ns = getSelectedSurface();
//		double u0 = ns.getUKnotVector()[0];
//		double u1 = ns.getUKnotVector()[ns.getUKnotVector().length - 1];
//		double v0 = ns.getVKnotVector()[0];
//		double v1 = ns.getVKnotVector()[ns.getVKnotVector().length - 1];
//		for (VVertex v : hds.getVertices()) {
//			double[] NurbsuvCoordinate = as.get(NurbsUVCoordinate.class, v, double[].class);
//			double uCoord = NurbsuvCoordinate[0];
//			double vCoord = NurbsuvCoordinate[1];
//			if(uCoord < u0 || uCoord > u1){
//				System.out.println("uCoord is out of domain " + uCoord);
//			}
//			if(vCoord < v0 || vCoord > v1){
//				System.out.println("uCoord is out of domain " + vCoord);
//			}
//			
//			CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(ns, uCoord, vCoord);
//			double[] vector = ci.getCurvatureDirectionsDomain()[0];
//			linefield.set(v, vector, as);
//		}
//		
//		
//		as.add(indexAdapter);
//		LinkedList<Double> umbFaces = new LinkedList<Double>();
//		LinkedList<VFace> umbilicFaces = new LinkedList<VFace>();
//		for (VFace f : hds.getFaces()){
//			double result = indexAdapter.get(f, as);
//			if(Math.abs(Math.abs(result) - 1) < 0.001){
//				umbFaces.add(result);
//				umbilicFaces.add(f);
//			}
//		}
//
//		RealFunctionOfSeveralVariables fun = new RealFunctionOfSeveralVariables() {
//			@Override
//			public int getNumberOfVariables() {
//				return 2;
//			}
//			@Override
//			public double eval(double[] p) {
//				NURBSSurface ns = getSelectedSurface();
//				double u0 = ns.getUKnotVector()[0];
//				double u1 = ns.getUKnotVector()[ns.getUKnotVector().length - 1];
//				double v0 = ns.getVKnotVector()[0];
//				double v1 = ns.getVKnotVector()[ns.getVKnotVector().length - 1];
//				if(p[0] < u0 || p[0] > u1){
//					System.out.println("Nelder-Mead out of domain");
//					System.out.println("p[0]: " + p[0]);
//					return 10000;
//				}
//				if(p[1] < v0 || p[1] > v1){
//					System.out.println("Nelder-Mead out of domain");
//					System.out.println("p[1]: " + p[1]);
//					return 10000;
//				}
//				CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(ns, p[0], p[1]);
//				double H = ci.getMeanCurvature();
//				double K = ci.getGaussCurvature();
//				return Math.abs(H*H - K);
//			}
//		};
//		
//		LinkedList<double[]> possibleUmbs = new LinkedList<double[]>();
//		for (VFace f : umbilicFaces){
//			System.out.println("Faceindex: " + f.getIndex());
//			VVertex v = f.getBoundaryEdge().getStartVertex();
//			double[] start = as.get(NurbsUVCoordinate.class, v, double[].class);
//			double[][] xi = computeXi(start);
//			double value = NelderMead.search(start, xi, 1E-12, fun,100,new Info());
//			System.out.println();
//			System.out.println("NM Value " + value);
//			System.out.println("NM Pos " + Arrays.toString(start));
//			possibleUmbs.add(start);
//		}
//		
//		
//		double epsilon = 0.001;
//		HashMap<double[], List<double[]>> near = new HashMap<double[], List<double[]>>();
//		for (double[] umb1 : possibleUmbs) {
//			List<double[]> nearUmb1 = new LinkedList<double[]>();
//			for (double[] umb2 : possibleUmbs) {
//				if(umb1 != umb2){
//					if(Rn.euclideanDistance(umb1, umb2) < epsilon){
//						nearUmb1.add(umb2);
//					}
//				}
//			}
//			near.put(umb1, nearUmb1);
//		}
//		List<double[]> allNearUmb = new LinkedList<double[]>();
//		List<double[]> allNearFirstUmb = new LinkedList<double[]>();
//		for (double[] umb : possibleUmbs) {
//			if(near.containsKey(umb)){
//				allNearUmb.add(umb);
//				if(!allNearFirstUmb.contains(umb)){
//					for(double[] u : near.get(umb)){
//						allNearFirstUmb.add(u);
//					}
//				}
//			}
//		}
//		possibleUmbs.removeAll(allNearFirstUmb);
//		return possibleUmbs;
//	}
//	
	public NURBSSurface getSelectedSurface() {
		return surfaces.get(surfacesTable.getSelectedRow());
	}
	
	
	public double[][] computeXi(double[] p) {
		double[][] xi = new double[2][];
		double[] x1 = new double[2];
		double[] x2 = new double[2];
		NURBSSurface ns = getSelectedSurface();
		double pu = p[0];
		double pv = p[1];
		double uend = ns.getUKnotVector()[ns.getUKnotVector().length - 1];
		double vend = ns.getVKnotVector()[ns.getVKnotVector().length - 1];
		if(pu + 0.05 < uend){
			x1[0] = 1.; 
		}
		else{
			x1[0] = -1.;
		}
		x1[1] = 0;
		if(pv + 0.05 < vend){
			x2[1] = 1.;
		}
		else{
			x2[1] = -1.;
		}
		x2[0] = 0;
		xi[0] = x1;
		xi[1] = x2;
		return xi;
	}
	
	
	@Override
	public void mainUIChanged(String uiClass) {
		super.mainUIChanged(uiClass);
		SwingUtilities.updateComponentTreeUI(chooser);
	}
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addContentUI();
		v.registerPlugin(new NurbsManagerPlugin());
		v.startup();
	}
}
