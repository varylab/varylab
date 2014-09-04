package de.varylab.varylab.plugin.nurbs.plugin;

import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

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
import de.jreality.plugin.job.JobListener;
import de.jreality.plugin.job.JobQueuePlugin;
import de.jreality.plugin.job.ParallelJob;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.LayoutFactory;
import de.jreality.writer.WriterOBJ;
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
import de.varylab.varylab.plugin.nurbs.DraggableCurvesModel;
import de.varylab.varylab.plugin.nurbs.DraggableCurvesTable;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.adapter.VectorFieldMapAdapter;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.data.SignedUV;
import de.varylab.varylab.plugin.nurbs.math.CurveType;
import de.varylab.varylab.plugin.nurbs.math.FaceSetGenerator;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.SymmetricDir;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.VectorFields;
import de.varylab.varylab.plugin.nurbs.math.LineSegmentIntersection;
import de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility;
import de.varylab.varylab.plugin.nurbs.math.PrincipleCurvatureVectorFieldProvider;
import de.varylab.varylab.plugin.nurbs.math.SymmetricVectorFieldProvider;
import de.varylab.varylab.plugin.nurbs.scene.DefaultComponentProvider;
import de.varylab.varylab.plugin.nurbs.scene.DraggableIntegralCurveListener;
import de.varylab.varylab.plugin.nurbs.scene.DraggableIntegralNurbsCurves;
import de.varylab.varylab.plugin.nurbs.scene.ListSceneGraphComponent;
import de.varylab.varylab.plugin.nurbs.scene.NurbsSurfaceDirectionConstraint;
import de.varylab.varylab.ui.PrettyPrinter;

public class IntegralCurvesPlugin 
		extends ShrinkPanelPlugin 
		implements ActionListener, PointSelectionListener, HalfedgeListener, ListSelectionListener, TableModelListener, ItemListener, ChangeListener, JobListener {
	
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
		immediateCalculationBox = new JCheckBox("Immediate");
	
	private JComboBox<CurveType>
		curveCombo = new JComboBox<CurveType>(CurveType.values());
	private JComboBox<IntegralCurveFactory.VectorFields>
		vectorFieldCombo = new JComboBox<>(IntegralCurveFactory.VectorFields.values());
	private JComboBox<IntegralCurveFactory.VectorFields>
		vfCombo = new JComboBox<>();
	private JComboBox<IntegralCurveFactory.SymmetricDir>
		symmetryCombo = new JComboBox<IntegralCurveFactory.SymmetricDir>(IntegralCurveFactory.SymmetricDir.values());
	
	public static volatile int 
		curveIndex = 5;
	
	private DraggableCurvesModel
		curvesModel = new DraggableCurvesModel("Curves", new DCPrinter());
	
	private DraggableCurvesTable 
		curvesTable = new DraggableCurvesTable(curvesModel);

	private DefaultComponentProvider<DraggableIntegralNurbsCurves>
		defaultComponentProvider = new DefaultComponentProvider<>();
	
	private ListSceneGraphComponent<DraggableIntegralNurbsCurves, DefaultComponentProvider<DraggableIntegralNurbsCurves>>
		integralCurvesRoot = new ListSceneGraphComponent<>("Integral curves root", defaultComponentProvider);
	
	private Map<HalfedgeLayer, DraggableCurvesModel> 
		layers2models = new HashMap<>();
	
	private JScrollPane 
		curveScrollPanel = new JScrollPane(curvesTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
	private JPanel
		curveLengthPanel = new JPanel();

	private ShrinkPanel
		integratorParametersPanel = new ShrinkPanel("Integrator parameters"); 
	
	private DraggableIntegralNurbsCurves activeCurve;

	private VectorFieldMapAdapter vfmax;

	private VectorFieldMapAdapter vfmin;
	
	private IntegralCurveFactory ic;
	
	private List<DraggableIntegralNurbsCurves> currentCurves = Collections.synchronizedList(new LinkedList<DraggableIntegralNurbsCurves>());
	
	private LinkedList<LinkedList<SignedUV>> commonPoints = new LinkedList<>();

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
	
	private boolean startup = true;
	
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
		curveCombo.setSelectedItem(CurveType.CONJUGATE);
		
		shrinkPanel.add(new JLabel("Curve Type"),lc);
		shrinkPanel.add(curveCombo,rc);
		
		
		shrinkPanel.add(new JLabel("Curves:"), lc);
		vectorFieldCombo.setSelectedItem(VectorFields.BOTH);
		shrinkPanel.add(vectorFieldCombo, rc);
		
		shrinkPanel.add(new JLabel("Symmetry"), lc);
		symmetryCombo.setSelectedItem(SymmetricDir.CURVATURE);
		shrinkPanel.add(symmetryCombo,rc);
		
		shrinkPanel.add(immediateCalculationBox,lc);
		shrinkPanel.add(goButton, rc);
		goButton.addActionListener(this);
		
		curvesTable.getSelectionModel().addListSelectionListener(this);
		curvesTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		curvesModel = (DraggableCurvesModel) curvesTable.getListModel();
		
		curvesModel.addTableModelListener(this);
		curveScrollPanel.setPreferredSize(new Dimension(100, 150));
		
		curveLengthPanel.setLayout(new GridBagLayout());
		curveLengthPanel.setPreferredSize(new Dimension(100, 50));
		
		curveLengthPanel.add(new JLabel("Start"), lc);
		curveLengthPanel.add(beginLineSpinner, lc);
		curveLengthPanel.add(vfCombo,rc);
		vfCombo.addItemListener(this);
		beginLineSpinner.setEnabled(false);
		curveLengthPanel.add(new JLabel("End"), lc);
		curveLengthPanel.add(endLineSpinner, lc);
		endLineSpinner.setEnabled(false);
		curveLengthPanel.add(cutLineButton, rc);
		cutLineButton.addActionListener(this);
		cutLineButton.setEnabled(false);
		
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

	private List<DraggableIntegralNurbsCurves> setCommonCurves(DraggableIntegralNurbsCurves curve){
		if(curve.getCommonCurves() == null){
			List<DraggableIntegralNurbsCurves> commonCurves = new LinkedList<>();
			List<SignedUV> commonPoints = getCommonPoints(curve);
			for (DraggableIntegralNurbsCurves dc : currentCurves) {
				dc.setSign(dc.getInitialUV().getSign());
				if(commonPoints.contains(dc.getInitialUV())){
					commonCurves.add(dc);
				}		
			}
			curve.setCommonCurves(commonCurves);
		}
		return curve.getCommonCurves();
	}
	
	private List<SignedUV> getCommonPoints(DraggableIntegralNurbsCurves dc){
		SignedUV p = dc.getInitialUV();
		LinkedList<SignedUV> others = new LinkedList<>();
		if(commonPoints == null) {
			return others;
		}
		for (LinkedList<SignedUV> list : commonPoints) {
			if(list.contains(p)){
				for (SignedUV signedPoint : list) {
					if(signedPoint != p){
						others.add(signedPoint);
					}
				}
			}
		}
		return others;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		Object source = e.getSource();
		final NURBSSurface surface = nManager.getActiveNurbsSurface();
		if(source == goButton) {
			currentCurves.clear();
			initializeIntegralCurvesFactory(surface);
			
			if(singularities == null) {
				//							computeUmbilicalPoints();
			}
			
			commonPoints = pointSelectionPlugin.getCommonPointList();
			
			hif.clearSelection();
			
			final List<SignedUV> startingPointsUV = pointSelectionPlugin.getSelectedSignedPoints();
			
			computeIntegralCurves(surface, startingPointsUV);
			
		} else if(source == deleteButton) {
			curvesModel.clear();
			hif.clearSelection();
			curvesModel.fireTableDataChanged();
		} else if(source == intersectionsButton) {
			// default patch
			LinkedList<LineSegment> allSegments = new LinkedList<LineSegment>();
			logger.info("All curves from the curveModel");
			for(PolygonalLine pl : curvesModel.getCheckedPolygonalLines()){
				logger.info(pl.toString());
				allSegments.addAll(pl.getpLine());
			}
			
			List<LineSegment> completeDomainBoundarySegments = surface.getDomain().getCompleteDomainBoundarySegments();
			
			double[] U = surface.getUKnotVector();
			double[] V = surface.getVKnotVector();
			
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
			FaceSetGenerator fsg = new FaceSetGenerator(surface, intersections);
			double lastTimeBentley = System.currentTimeMillis();
			logger.info("Bentley Ottmann Time: " + (lastTimeBentley - firstTimeBentley));
			allSegments.clear();
			FaceSet fs = fsg.createFaceSet();
			for (int i = 0; i < fs.getVerts().length; i++) {
				double[] S = new double[4];
				surface.getSurfacePoint(fs.getVerts()[i][0], fs.getVerts()[i][1], S);
				fs.getVerts()[i] = S;
			}

			HalfedgeLayer hel = new HalfedgeLayer(hif);
			hel.setName("Curvature Geometry");
		
			hif.addLayer(hel); //add and activate
			hel.set(fs.getIndexedFaceSet());
			hif.update();
		} else if(source == cutLineButton){
			activeCurve.getPolygonalLine((VectorFields) vfCombo.getSelectedItem()).setBegin(beginLineModel.getNumber().intValue());
			activeCurve.getPolygonalLine((VectorFields) vfCombo.getSelectedItem()).setEnd(endLineModel.getNumber().intValue());
			updateIntegralCurvesRoot();
		} else if(source == showVectorFieldBox) {
			if(showVectorFieldBox.isSelected()) {
				updateVectorfields();
			} else {
				removeVectorFields();
			}
		}
		
	}

	private void computeIntegralCurves(final NURBSSurface surface, final List<SignedUV> startingPointsUV) {
		Collection<AbstractJob> jobs = new LinkedHashSet<AbstractJob>();
		if(startingPointsUV.size() == 0) {
			return;
		}
		logger.info("ALL STARTING POINTS");
		for (SignedUV sp : startingPointsUV) {
			logger.info(Arrays.toString(sp.getPoint()));
		}
		final List<DraggableIntegralNurbsCurves> curves = Collections.synchronizedList(new LinkedList<DraggableIntegralNurbsCurves>());
		for (final SignedUV sp : startingPointsUV) {
			AbstractJob j = new AbstractJob() {
				@Override
				public String getJobName() {
					return Arrays.toString(sp.getPoint());
				}
				
				@Override
				protected void executeJob() throws Exception {
					DraggableIntegralNurbsCurves dc = new DraggableIntegralNurbsCurves(surface, ic, sp);
					dc.setConstraint(new NurbsSurfaceDirectionConstraint(surface, sp.getPoint(), pointSelectionPlugin.getParameter()));
					DraggableIntegralCurveListener listener = new DraggableIntegralCurveListener(surface,dc,jobQueuePlugin);
					dc.addPointDragListener(listener);
					curves.add(dc);
					Collection<PolygonalLine> lines = dc.getPolygonalLines();
					setCurveIndices(lines);
					currentCurves.add(dc);
				}
			};
			j.addJobListener(this);
			jobs.add(j);
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
				for(DraggableIntegralNurbsCurves dc : curves) {
					setCommonCurves(dc);
				}
				updateIntegralCurvesRoot(null, curves);
			}

		};
		updateJob.addJobListener(this);
		jobQueuePlugin.queueJob(updateJob);
	}

	private void initializeIntegralCurvesFactory(final NURBSSurface surface) {
		
		ic = new IntegralCurveFactory(surface.getDomain());
		
		double tol = tolExpModel.getNumber().doubleValue();
		tol = Math.pow(10, tol);
		ic.setTol(tol);

		
		ic.setVectorFields((VectorFields)vectorFieldCombo.getSelectedItem());
		
		double umbilicStop = Math.pow(10, nearUmbilicModel.getNumber().doubleValue());
		ic.setUmbillicStop(umbilicStop);

		CurveType vfc = (CurveType)curveCombo.getSelectedItem();

		SymmetricDir symDir = (SymmetricDir)symmetryCombo.getSelectedItem();
		
		switch (vfc) {
		case CURVATURE:
			ic.setVectorFieldProvider(new PrincipleCurvatureVectorFieldProvider(surface));
			break;
		case CONJUGATE:
			ic.setVectorFieldProvider(new SymmetricVectorFieldProvider(surface, symDir, getVecField()));
			break;
		case ASYMPTOTIC:
			logger.warning("Asymptotic line computation not yet implemented.");
			return;
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
				double[][] vf = getDirections(nurbsAdapter.getSurface(), nurbsAdapter.getV(v, null), getVecField());
				vfmax.setV(v,vf[0],null);
				vfmin.setV(v,vf[1],null);
				
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
		computeIntegralCurves(nManager.getActiveNurbsSurface(), Collections.singletonList(new SignedUV(uv,1.0)));
	}


	@Override
	public void dataChanged(HalfedgeLayer layer) {
		if(startup) {
			layer.addTemporaryGeometry(integralCurvesRoot.getComponent());
			logger.severe("adding integral curves root to temporary geometry");
		}
		startup = false;
//		if(curvesModel == null) {
			if(layers2models.containsKey(layer)) {
				curvesModel = layers2models.get(layer);
				curvesModel.clear();
			} else {
				curvesModel = new DraggableCurvesModel("Curves", new DCPrinter());
				curvesModel.addTableModelListener(this);
				layers2models.put(layer,curvesModel);
			}
//		} else {
//			curvesModel.clear();
//		}
		curvesTable.setModel(curvesModel);
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
			DraggableCurvesModel newModel = new DraggableCurvesModel("Curves", new DCPrinter());
			curvesTable.setModel(newModel);
			newModel.addTableModelListener(this);
			layers2models.put(layer,newModel);
			newModel.fireTableDataChanged();
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
			final DraggableIntegralNurbsCurves lineComp = curvesModel.getList().get(mi);
			boolean isVisible = lineComp.isVisible();
			boolean beginEnd = true;
			Appearance app = lineComp.getAppearance();
			app.setAttribute(LINE_SHADER + "." + RADII_WORLD_COORDINATES, dls.getRadiiWorldCoordinates());
			if(curvesTable.isRowSelected(i)) {
				if(beginEnd == true){
					activeCurve = curvesModel.getList().get(mi);
					vfCombo.removeAllItems();
					switch (lineComp.getVectorFields()) {
					case FIRST:
						vfCombo.addItem(VectorFields.FIRST);
						break;
					case SECOND:
						vfCombo.addItem(VectorFields.SECOND);
						break;
					case BOTH:
						vfCombo.addItem(VectorFields.FIRST);
						vfCombo.addItem(VectorFields.SECOND);
					}

					cutLineButton.setEnabled(true);
					vfCombo.setEnabled(true);
					updateMinMaxSpinners();
				}
				beginEnd = false;
				app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls.getTubeRadius()*1.8);
			} else {
				app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls.getTubeRadius());					
			}
			lineComp.setVisible(isVisible);
		}
	}

	private void updateMinMaxSpinners() {
		PolygonalLine pl = activeCurve.getPolygonalLine((VectorFields) vfCombo.getSelectedItem());
		int	max = pl.getMax();
		beginLineModel.setMaximum(max);
		endLineModel.setMaximum(max);
		beginLineSpinner.setEnabled(true);
		beginLineModel.setValue(pl.getBegin());
		endLineSpinner.setEnabled(true);
		endLineModel.setValue(pl.getEnd());
	}


	@Override
	public void tableChanged(TableModelEvent e) {
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				integralCurvesRoot.retain(curvesModel.getList());
				curvesTable.adjustColumnSizes();
			}
		};
		EventQueue.invokeLater(runnable);
	}

	private class DCPrinter implements PrettyPrinter<DraggableIntegralNurbsCurves> {

		@Override
		public String toString(DraggableIntegralNurbsCurves t) {
			return t.getName();
		}
		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == vfCombo) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				if(vfCombo.isEnabled()) {
					updateMinMaxSpinners();
				}
			}
		} else if(e.getSource() == curveCombo) {
			updateIntegralCurvesPanelUI();
		}
	}

	private void updateIntegralCurvesPanelUI() {
		if(CurveType.CONJUGATE == curveCombo.getSelectedItem()){
			vecFieldSpinner.setEnabled(true);
			symmetryCombo.setEnabled(true);
		}
		else{
			vecFieldSpinner.setEnabled(false);
			symmetryCombo.setEnabled(false);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == vecFieldSpinner) {
			updateVectorfields();
		}
	}

	private void updateIntegralCurvesRoot(final List<DraggableIntegralNurbsCurves> toRemove, final List<DraggableIntegralNurbsCurves> toAdd) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if(toRemove != null) {
					for(DraggableIntegralNurbsCurves dc : toRemove) {
						curvesModel.remove(dc);
					}
				}
				if(toAdd != null) {
					for(DraggableIntegralNurbsCurves dc : toAdd) {
						curvesModel.add(dc);
						dc.updateComponent();
					}
				}
				List<DraggableIntegralNurbsCurves> list = curvesModel.getList();
				integralCurvesRoot.retain(list);
				curvesModel.fireTableDataChanged();
				for(DraggableIntegralNurbsCurves dc : list) {
					dc.setVisible(curvesModel.isChecked(dc));
				}
			}
		};
		EventQueue.invokeLater(r);
	}
	
	private void updateIntegralCurvesRoot() {
		updateIntegralCurvesRoot(null, null);
	}
	
	private void setCurveIndices(Collection<PolygonalLine> lines) {
		logger.warning("CurveIndex:" + curveIndex);
		for(PolygonalLine l : lines) {
			l.setCurveIndex(curveIndex++);
		}
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
		c.storeProperty(getClass(), "curves", curveCombo.getSelectedItem());
		c.storeProperty(getClass(), "symmetry", symmetryCombo.getSelectedItem());
		c.storeProperty(getClass(), "immediateCurveCalculation", immediateCalculationBox.isSelected());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		curveCombo.setSelectedItem(c.getProperty(getClass(), "curves", curveCombo.getSelectedItem()));
		symmetryCombo.setSelectedItem(c.getProperty(getClass(), "symmetry", symmetryCombo.getSelectedItem()));
		immediateCalculationBox.setSelected(c.getProperty(getClass(),"immediateCurveCalculation",immediateCalculationBox.isSelected()));
	}

	@Override
	public void jobStarted(Job job) {
	}

	@Override
	public void jobProgress(Job job, double progress) {
	}

	@Override
	public void jobFinished(Job job) {
	}

	@Override
	public void jobFailed(Job job, Exception e) {
		e.printStackTrace();
	}

	@Override
	public void jobCancelled(Job job) {
	}
}

