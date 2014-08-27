package de.varylab.varylab.plugin.nurbs.plugin;

import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

import de.jreality.math.Rn;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.job.AbstractJob;
import de.jreality.plugin.job.Job;
import de.jreality.plugin.job.JobQueuePlugin;
import de.jreality.plugin.job.ParallelJob;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.ui.LayoutFactory;
import de.jreality.writer.WriterOBJ;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.data.VisualizationInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.interaction.DraggablePointComponent;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.adapter.VectorFieldMapAdapter;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.math.CurveType;
import de.varylab.varylab.plugin.nurbs.math.FaceSetGenerator;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurve;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurve.SymmetricDir;
import de.varylab.varylab.plugin.nurbs.math.LineSegmentIntersection;
import de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility;
import de.varylab.varylab.plugin.nurbs.scene.ListSceneGraphComponent;
import de.varylab.varylab.plugin.nurbs.scene.PolygonalLineComponentProvider;
import de.varylab.varylab.ui.ListSelectRemoveTable;
import de.varylab.varylab.ui.ListSelectRemoveTableModel;
import de.varylab.varylab.ui.PrettyPrinter;

public class IntegralCurvesPlugin 
		extends ShrinkPanelPlugin 
		implements ActionListener, PointSelectionListener, HalfedgeListener, ListSelectionListener, TableModelListener, ItemListener, ChangeListener {
	
	private static Logger logger = Logger.getLogger(IntegralCurvesPlugin.class.getSimpleName());
	
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
		showVectorFieldBox = new JCheckBox("vector field (angle):"),	
		immediateCalculationBox = new JCheckBox("Immediate"),
		maxCurvatureBox = new JCheckBox("Max (cyan)"),
		minCurvatureBox = new JCheckBox("Min (red)"),
		vecFieldBox = new JCheckBox("Vec. Field (red)"),
		conjFieldBox = new JCheckBox("Conj. Field (cyan)"),
		symConjBox = new JCheckBox("Conjugate directions"),
		symConjCurvatureBox = new JCheckBox("Principle directions"),
		interactiveBox = new JCheckBox("Interactive Curve Dragging");
	
	private JComboBox<CurveType>
		curveCombo = new JComboBox<CurveType>(CurveType.values());
	
	private boolean firstVectorField = true;
	private boolean secondVectorField = true;

	private volatile int 
		curveIndex = 5;
	
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

	private ShrinkPanel
		integratorParametersPanel = new ShrinkPanel("Integrator parameters"); 
	
	private PolygonalLine activeCurve;

	private VectorFieldMapAdapter vfmax;

	private VectorFieldMapAdapter vfmin;
	
	private IntegralCurve ic;
	
	private List<DraggableCurves> currentCurves = Collections.synchronizedList(new LinkedList<DraggableCurves>());
	
	private LinkedList<LinkedList<double[]>> commonPoints = new LinkedList<>();

	private List<double[]> singularities = null;

	private PointSelectionPlugin 
		pointSelectionPlugin = null;
	
	private VisualizationInterface 
		vif = null;

	private JobQueuePlugin 
		jobQueuePlugin = null;
	
	private NurbsManagerPlugin 
		nManager = null;

	private HalfedgeInterface 
		hif = null;
	
	private double[] getVecField(){
		if(vecFieldSpinner.isEnabled()){
			double grad = vecFieldModel.getNumber().doubleValue();
			double phi = grad * Math.PI / 180.0;
			double[] vec = {Math.cos(phi), Math.sin(phi)};
			return vec;
		}
		return null;
	}
	
	public IntegralCurvesPlugin() {
		
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		
		setupIntegratorParametersPanel();
		shrinkPanel.add(integratorParametersPanel,rc);
		
		curveCombo.addItemListener(this);
		
		shrinkPanel.add(new JLabel("Curve Type"),lc);
		shrinkPanel.add(curveCombo,rc);
		
		
		shrinkPanel.add(interactiveBox, rc);
		interactiveBox.setSelected(false);

		shrinkPanel.add(new JLabel("Curvature Lines:"), rc);
		shrinkPanel.add(minCurvatureBox, lc);
		minCurvatureBox.setEnabled(false);
		shrinkPanel.add(maxCurvatureBox, rc);
		maxCurvatureBox.setEnabled(false);
		
		shrinkPanel.add(new JLabel("Conjugate Lines:"), rc);
		shrinkPanel.add(vecFieldBox, lc);
		vecFieldBox.setEnabled(true);
		shrinkPanel.add(conjFieldBox, rc);
		conjFieldBox.setEnabled(true);
		
		
		shrinkPanel.add(new JLabel("Symmetry"), rc);
		shrinkPanel.add(symConjBox, rc);
		symConjBox.addActionListener(this);
		symConjBox.setEnabled(true);
		shrinkPanel.add(symConjCurvatureBox, rc);
		symConjCurvatureBox.setEnabled(true);
		symConjCurvatureBox.addActionListener(this);
		
		shrinkPanel.add(immediateCalculationBox,lc);
		shrinkPanel.add(goButton, rc);
		goButton.addActionListener(this);
		
		curvesTable.getSelectionModel().addListSelectionListener(this);
		curvesTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		curvesModel = curvesTable.getListModel();
		
		curvesModel.addTableModelListener(this);
		curveScrollPanel.setPreferredSize(new Dimension(100, 150));
		curveLengthPanel.setPreferredSize(new Dimension(100, 50));
		
		curveLengthPanel.add(new JLabel("Start"), lc);
		curveLengthPanel.add(beginLineSpinner, lc);
		beginLineSpinner.setEnabled(false);
		curveLengthPanel.add(new JLabel("End"), lc);
		curveLengthPanel.add(endLineSpinner, rc);
		endLineSpinner.setEnabled(false);
		curveLengthPanel.add(cutLineButton, rc);
		cutLineButton.addActionListener(this);
		cutLineButton.setEnabled(false);
		interactiveBox.addActionListener(this);
		
		
		shrinkPanel.add(curveScrollPanel, rc);
		shrinkPanel.add(curveLengthPanel, rc);

		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute(CommonAttributes.EDGE_DRAW, true);
		integralCurvesRoot.setAppearance(app);
		
		deleteButton.addActionListener(this);
		shrinkPanel.add(deleteButton, rc);
		intersectionsButton.addActionListener(this);
		shrinkPanel.add(intersectionsButton,rc);
	}
	
	private void setupIntegratorParametersPanel() {
		integratorParametersPanel.setLayout(new GridBagLayout());
		integratorParametersPanel.setShrinked(true);
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		lc.gridwidth = 1;
		integratorParametersPanel.add(showVectorFieldBox, lc);
		lc.gridwidth = 1;
		integratorParametersPanel.add(vecFieldSpinner, rc);
		lc.gridwidth = 1;
		showVectorFieldBox.addActionListener(this);
		vecFieldSpinner.setEnabled(true);
		vecFieldSpinner.addChangeListener(this);
		lc.gridwidth = 2;
		integratorParametersPanel.add(new JLabel("Runge-Kutta Tolerance Exp"), lc);
		lc.gridwidth = 1;
		integratorParametersPanel.add(tolSpinner, rc);
		lc.gridwidth = 2;
		integratorParametersPanel.add(new JLabel("Singularity Neighbourhood Exp"), lc);
		lc.gridwidth = 1;
		integratorParametersPanel.add(nearUmbilicSpinner, rc);
	}

	private class DraggableCurves implements PointDragListener {
		
		private List<PolygonalLine> polygonalLines;			
		private DraggablePointComponent draggablePoint;
		private double[] p;
		private boolean uDir = pointSelectionPlugin.getUDir();
		private boolean vDir = pointSelectionPlugin.getVDir();
		private double[] startUV = null;
		private List<DraggableCurves> commonCurves = null;
		private List<CurveJob> curveJobQueue = Collections.synchronizedList(new LinkedList<CurveJob>());
		
		LinkedList<Integer> indexList;

		public DraggableCurves(double[] uv, double[] point, List<PolygonalLine> lines) {
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
		
		public DraggablePointComponent createDraggablePoint(double[] point){
			DraggablePointComponent dpc = new DraggablePointComponent(point,this);
			Appearance Ap = new Appearance();
			Ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			dpc.setAppearance(Ap);
			DefaultGeometryShader idgs = ShaderUtility.createDefaultGeometryShader(Ap, false);
			DefaultPointShader ipointShader = (DefaultPointShader)idgs.getPointShader();
			ipointShader.setDiffuseColor(Color.orange);
			return dpc;
		}

		
		@Override
		public void pointDragStart(PointDragEvent e) {
			ic.setTol(1E-1);
		}

		@Override
		public void pointDragged(final PointDragEvent e) {

			p = new double[]{e.getX(), e.getY(), e.getZ(), 1.0};
			final double[] uv = nManager.getActiveNurbsSurface().getClosestPointDomainDir(p, startUV, uDir, vDir);
			updateAllCurves(uv);

		}

		@Override
		public void pointDragEnd(PointDragEvent e) {
			ic.setTol(Math.pow(10.0,tolExpModel.getNumber().doubleValue()));
			
			AbstractJob updateJob = new AbstractJob() {
				@Override
				public String getJobName() {
					return "Update curves display";
				}
				
				@Override
				protected void executeJob() throws Exception {
					synchronized(curveJobQueue) {
						curveJobQueue.remove(0);
						processCurveJobs();
					}
					curvesModel.fireTableDataChanged();
				}
			};
			addCurveJobs(new CurveJob(null, updateJob));
		}	
		
		private void updateAllCurves(final double[] uv) {
			Collection<AbstractJob> jobs = new LinkedList<AbstractJob>();
			final List<PolygonalLine> linesToRemove = Collections.synchronizedList(new LinkedList<PolygonalLine>());
			final List<PolygonalLine> linesToAdd = Collections.synchronizedList(new LinkedList<PolygonalLine>());
			
			draggablePoint.updateCoords(nManager.getActiveNurbsSurface().getSurfacePoint(uv[0], uv[1]));
			
			AbstractJob j = createCurveJob(uv, linesToRemove, linesToAdd);
			jobs.add(j);

			if(interactiveBox.isSelected()){
				jobs.addAll(createCommonCurvesJobs(uv, linesToRemove, linesToAdd));
			}
			ParallelJob parallelJob = new ParallelJob(jobs);
			
			AbstractJob updateJob = new AbstractJob() {
				@Override
				public String getJobName() {
					return "Update curves display";
				}
				
				@Override
				protected void executeJob() throws Exception {
					updateIntegralCurvesRoot(linesToRemove, linesToAdd);
					synchronized(curveJobQueue) {
						curveJobQueue.remove(0);
						processCurveJobs();
					}
				}
			};
			addCurveJobs(new CurveJob(parallelJob, updateJob));
		}

		private void addCurveJobs(CurveJob cj) {
			synchronized(curveJobQueue) {
				if(curveJobQueue.size() == 2) {
					curveJobQueue.set(1,cj);
				} else {
					curveJobQueue.add(cj);
				} 
				if(curveJobQueue.size() == 1) {
					processCurveJobs();
				}
			}
		}

		private void processCurveJobs() {
			synchronized(curveJobQueue) {
				if(!curveJobQueue.isEmpty()) {
					CurveJob cj = curveJobQueue.get(0);
					AbstractJob parallelJob = cj.getComputationJob();
					if(parallelJob != null) {
						jobQueuePlugin.queueJob(parallelJob);
					}
					jobQueuePlugin.queueJob(cj.getUpdateJob());
				}
			}
		}

		private AbstractJob createCurveJob(final double[] uv, final List<PolygonalLine> linesToRemove, final List<PolygonalLine> linesToAdd) {
			AbstractJob j = new AbstractJob() {
				
				@Override
				public String getJobName() {
					return "Recompute curve " + uv;
				}
				
				@Override
				protected void executeJob() throws Exception {
					linesToRemove.addAll(polygonalLines);
					recomputeCurves(uv);
					linesToAdd.addAll(polygonalLines);
				}
			};
			return j;
		}

		
		
		private Collection<AbstractJob> createCommonCurvesJobs(final double[] uv, final List<PolygonalLine> linesToRemove, final List<PolygonalLine> linesToAdd) {
			Collection<AbstractJob> jobs = new LinkedHashSet<AbstractJob>();
			final double[] translation = Rn.subtract(null, uv, startUV);
			for (final DraggableCurves dc : getCommonCurves(this)) {
				double[] otherStartUV = dc.getStartUV();
				double[] newCoords = Rn.add(null, otherStartUV, translation);
				jobs.add(dc.createCurveJob(newCoords, linesToRemove, linesToAdd));
			}
			return jobs;
		}
		
		public void recomputeCurves(double[] uv) {
			polygonalLines = ic.computeIntegralLine(firstVectorField, secondVectorField, 0.01, singularities, uv);
			setCurveIndices(polygonalLines);
			draggablePoint.updateCoords(nManager.getActiveNurbsSurface().getSurfacePoint(uv[0], uv[1]));
		}
		
		public void setCommonCurves(List<DraggableCurves> commonCurves) {
			this.commonCurves = commonCurves;
		}

	}
	
	private List<DraggableCurves> getCommonCurves(DraggableCurves curve){
		if(curve.commonCurves == null){
			List<DraggableCurves> commonCurves = new LinkedList<>();
			List<double[]> commonPoints = getCommonPoints(curve);
			for (DraggableCurves dc : currentCurves) {
				if(commonPoints.contains(dc.getStartUV())){
					commonCurves.add(dc);
				}		
			}
			curve.setCommonCurves(commonCurves);
		}
		return curve.commonCurves;
	}
	
	private List<double[]> getCommonPoints(DraggableCurves dc){
		double[] p = dc.getStartUV();
		LinkedList<double[]> others = new LinkedList<>();
		if(commonPoints == null) {
			return others;
		}
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
	
	@Override
	public void actionPerformed(ActionEvent e){
		Object source = e.getSource();
		if(source == goButton) {

			currentCurves.clear();
			double tol = tolExpModel.getNumber().doubleValue();
			tol = Math.pow(10, tol);

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

			SymmetricDir symDir = SymmetricDir.NO_SYMMETRY;
			if(symConjBox.isSelected()){
				symDir = SymmetricDir.DIRECTION;
			}
			else if(symConjCurvatureBox.isSelected()){
				symDir = SymmetricDir.CURVATURE;
			}	
			ic = new IntegralCurve(nManager.getActiveNurbsSurface(), vfc, tol, symDir, getVecField(), curveIndex);
			if(singularities == null) {
				//							computeUmbilicalPoints();
			}
			commonPoints = pointSelectionPlugin.getCommonPointList();
			
			hif.clearSelection();
			
			final double umbilicStop = Math.pow(10, nearUmbilicModel.getNumber().doubleValue());
			Collection<AbstractJob> jobs = new LinkedHashSet<AbstractJob>();
			final List<double[]> startingPointsUV = pointSelectionPlugin.getSelectedPoints();
			if(startingPointsUV.size() == 0) {
				return;
			}
			logger.info("ALL STARTING POINTS");
			for (double[] sp : startingPointsUV) {
				logger.info(Arrays.toString(sp));
			}
			for (final double[] sp : startingPointsUV) {
				jobs.add(new AbstractJob() {
					@Override
					public String getJobName() {
						return Arrays.toString(sp);
					}
					
					@Override
					protected void executeJob() throws Exception {
						LinkedList<PolygonalLine> lines = ic.computeIntegralLine(firstVectorField, secondVectorField, umbilicStop, singularities, sp);
						setCurveIndices(lines);
						curvesModel.addAll(lines);
						double[] surfacePoint = nManager.getActiveNurbsSurface().getSurfacePoint(sp[0], sp[1]);
						DraggableCurves dc = new DraggableCurves(sp, surfacePoint, lines);
						currentCurves.add(dc);
					}
				});
			}
			ParallelJob parallelJob = new ParallelJob(jobs);
			jobQueuePlugin.queueJob(parallelJob);

			Job updateJob = new AbstractJob() {
				@Override
				public String getJobName() {
					return "Update curves display";
				}
				
				@Override
				protected void executeJob() throws Exception {
					System.out.println("All lines");
					for (PolygonalLine pl : curvesModel.getList()) {
						System.out.println(pl.toString());
					}
					for (DraggableCurves dc : currentCurves) {
						hif.addTemporaryGeometry(dc.getDraggablePoint());
					}
					curvesModel.fireTableDataChanged();
				}
			};
			jobQueuePlugin.queueJob(updateJob);
		} else if(source == deleteButton) {
			curvesModel.clear();
			hif.clearSelection();
			curvesModel.fireTableDataChanged();
		} else if(source == intersectionsButton) {
			// default patch
			LinkedList<LineSegment> allSegments = new LinkedList<LineSegment>();
			logger.info("All curves from the curveModel");
			for(PolygonalLine pl : curvesModel.getChecked()){
				logger.info(pl.toString());
				allSegments.addAll(pl.getpLine());
			}
			
			List<LineSegment> completeDomainBoundarySegments = nManager.getActiveNurbsSurface().getCompleteDomainBoundarySegments();
			
			double[] U = nManager.getActiveNurbsSurface().getUKnotVector();
			double[] V = nManager.getActiveNurbsSurface().getVKnotVector();
			
			int boundaryIndex = 1;
			logger.info("All boundary curves ");
			for (LineSegment bs : completeDomainBoundarySegments) {
				logger.info("boundary segment ");
				bs.setCurveIndex(boundaryIndex);
				logger.info("boundary segment "+ bs.getCurveIndex());
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
			FaceSetGenerator fsg = new FaceSetGenerator(nManager.getActiveNurbsSurface(), intersections);
			double lastTimeBentley = System.currentTimeMillis();
			logger.info("Bentley Ottmann Time: " + (lastTimeBentley - firstTimeBentley));
			allSegments.clear();
			FaceSet fs = fsg.createFaceSet();
			for (int i = 0; i < fs.getVerts().length; i++) {
				double[] S = new double[4];
				nManager.getActiveNurbsSurface().getSurfacePoint(fs.getVerts()[i][0], fs.getVerts()[i][1], S);
				fs.getVerts()[i] = S;
			}

			HalfedgeLayer hel = new HalfedgeLayer(hif);
			hel.setName("Curvature Geometry");
		
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
			SymmetricDir symDir = SymmetricDir.NO_SYMMETRY;
			if(symConjBox.isSelected()){
				symDir = SymmetricDir.DIRECTION;
			}
			else if(symConjCurvatureBox.isSelected()){
				symDir = SymmetricDir.CURVATURE;
			}	

			IntegralCurve ic = new IntegralCurve(nManager.getActiveNurbsSurface(), (CurveType) curveCombo.getSelectedItem(), tol, symDir, getVecField(), curveIndex);
			
			if(singularities == null) {
				computeUmbilicalPoints();
			}
			
			List<PolygonalLine> currentLines = ic.computeIntegralLines(firstVectorField, secondVectorField, umbilicStop, singularities, startingPointsUV);
//			LinkedList<PolygonalLine> curvatureLines = computeCurvatureLines(Lists.newArrayList(uv));
			for(PolygonalLine pl : currentLines) {
				if(!curvesModel.contains(pl)) {
					curvesModel.add(pl);
//					integralCurvesRoot.addChild(createLineComponent(pl));
				}
			}
			curvesModel.fireTableDataChanged();
		}
	}


	@Override
	public void dataChanged(HalfedgeLayer layer) {
		layer.addTemporaryGeometry(integralCurvesRoot.getComponent());
//		if(curvesModel == null) {
			if(layers2models.containsKey(layer)) {
				curvesModel = layers2models.get(layer);
				curvesModel.clear();
			} 
//			else {
//				curvesModel = new ListSelectRemoveTableModel<PolygonalLine>("Initial Point", new PolygonalLinePrinter());
//				curvesModel.addTableModelListener(this);
//				layers2models.put(layer,curvesModel);
//			}
//		} else {
//			curvesModel.clear();
//		}
		curvesModel.fireTableDataChanged();
	}


	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
		if(nManager.getActiveNurbsSurface() == null) {
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
		updateIntegralCurvesRoot();
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
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

	private void updateIntegralCurvesRoot(final List<PolygonalLine> toRemove, final List<PolygonalLine> toAdd) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				curvesModel.removeAll(toRemove);
				curvesModel.addAll(toAdd);
				polylineComponentProvider.setSurface(nManager.getActiveNurbsSurface());
				List<PolygonalLine> list = curvesModel.getList();
				integralCurvesRoot.retain(list);
				for(PolygonalLine pl : list) {
					integralCurvesRoot.setVisible(pl, curvesModel.isChecked(pl));
				}
			}
		};
		EventQueue.invokeLater(r);
	}
	
	private void updateIntegralCurvesRoot() {
		updateIntegralCurvesRoot(Collections.<PolygonalLine>emptyList(), Collections.<PolygonalLine>emptyList());
	}
	
	private void setCurveIndices(List<PolygonalLine> lines) {
		for(PolygonalLine l : lines) {
			l.setCurveIndex(curveIndex++);
		}
	}

	private double[][] computeUmbilicalPoints() {
		AdapterSet as = hif.getAdapters();
		as.addAll(hif.getActiveAdapters());
		VHDS hds = hif.get(new VHDS());
		singularities = nManager.getActiveNurbsSurface().findUmbilics(hds, as);
		double[][] uu = new double[singularities.size()][];
		double[][] upoints = new double[singularities.size()][];
		for (int i = 0; i < uu.length; i++) {
			uu[i] = singularities.get(i);
		}
		
		for (int i = 0; i < uu.length; i++) {
			double[] S = new double[4];
			nManager.getActiveNurbsSurface().getSurfacePoint(uu[i][0], uu[i][1], S);
			upoints[i] = S;
		}
		
		return upoints;
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

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		nManager = c.getPlugin(NurbsManagerPlugin.class);
		pointSelectionPlugin = c.getPlugin(PointSelectionPlugin.class);
		vif = c.getPlugin(VisualizationInterface.class);
		jobQueuePlugin = c.getPlugin(JobQueuePlugin.class);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
		
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "calculateMaxCurve", maxCurvatureBox.isSelected());
		c.storeProperty(getClass(), "calculateMinCurve", minCurvatureBox.isSelected());
		c.storeProperty(getClass(), "immediateCurveCalculation", immediateCalculationBox.isSelected());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		maxCurvatureBox.setSelected(c.getProperty(getClass(), "calculateMaxCurve",maxCurvatureBox.isSelected()));
		minCurvatureBox.setSelected(c.getProperty(getClass(), "calculateMinCurve",minCurvatureBox.isSelected()));
		immediateCalculationBox.setSelected(c.getProperty(getClass(),"immediateCurveCalculation",immediateCalculationBox.isSelected()));
	}

}

