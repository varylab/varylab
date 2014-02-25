package de.varylab.varylab.plugin.ddg;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_SHADER;
import static de.jtem.halfedgetools.util.GeometryUtility.circumCircle;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.basic.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.algorithm.subdivision.MedialGraphLinear;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class VertexSpheres extends ShrinkPanelPlugin implements ActionListener, ChangeListener, HalfedgeListener {

	private HalfedgeInterface
		hif = null;
	private JButton
		initializeButton = new JButton("Initialize");
	private JButton
		createCircularButton = new JButton("C-Net");
	private JButton
		dualizeButton = new JButton("Dualize");
	private JSlider
		radiusSlider = new JSlider(0, 100, 0);
	private SpinnerNumberModel
		radiusModel = new SpinnerNumberModel(0, 0, 1, 0.01);
	private JSpinner
		radiusSpinner = new JSpinner(radiusModel);
	private JLabel
		radiusLabel = new JLabel("Radius");
	private JCheckBox
		spheresBox = new JCheckBox("Spheres");
	private JCheckBox
		circlesBox = new JCheckBox("Circles");
	private JCheckBox
		orthoSpheresBox = new JCheckBox("Ortho spheres");
	private boolean 
		blockListeners = false;
	private AdapterSet
		aSet = null;
	private SceneGraphComponent 
		spheres = new SceneGraphComponent();
	private SceneGraphComponent 
		circles = new SceneGraphComponent();
	private SceneGraphComponent 
		orthoSpheres = new SceneGraphComponent();
	private RealVector 
		modVector;
	public static RealVector 
		radii;
	
	private VHDS surface;
	private double min;
	private double max;

	
	public VertexSpheres() {
		shrinkPanel.setTitle("Vertex spheres");
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		shrinkPanel.add(initializeButton, rc);
		shrinkPanel.add(radiusLabel, lc);
		shrinkPanel.add(radiusSpinner, rc);
		shrinkPanel.add(radiusSlider, rc);
		shrinkPanel.add(spheresBox,rc);
		shrinkPanel.add(circlesBox,rc);
		shrinkPanel.add(orthoSpheresBox,rc);
		shrinkPanel.add(createCircularButton,rc);
		shrinkPanel.add(dualizeButton,rc);
		
		spheresBox.setEnabled(false);
		circlesBox.setEnabled(false);
		orthoSpheresBox.setEnabled(false);
		radiusSlider.setEnabled(false);
		radiusLabel.setEnabled(false);
		radiusSpinner.setEnabled(false);
		
		initializeButton.addActionListener(this);
		createCircularButton.addActionListener(this);
		dualizeButton.addActionListener(this);
		radiusSlider.addChangeListener(this);
		radiusSpinner.addChangeListener(this);
		spheresBox.addChangeListener(this);
		circlesBox.addChangeListener(this);
		orthoSpheresBox.addChangeListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == createCircularButton) {
			createCircularNet();
		}
		if(e.getSource() == dualizeButton) {
			dualize(surface,aSet);
			hif.update();
		}
		surface = hif.get(new VHDS());
		aSet = hif.getAdapters();
		radii = calculateRadii(surface,aSet);
		initModVector();
		updateRadiusSlider();
		spheresBox.setEnabled(true);
		spheresBox.setSelected(false);
		circlesBox.setEnabled(true);
		circlesBox.setSelected(false);
		orthoSpheresBox.setEnabled(true);
		orthoSpheresBox.setSelected(false);
		createCircularButton.setEnabled(true);
		dualizeButton.setEnabled(true);
	}

	private void createCircularNet() {
		HalfedgeLayer newLayer = new HalfedgeLayer(hif);
		newLayer.setName("Circular net?");
		MedialGraphLinear mgl = new MedialGraphLinear();
		VHDS newHDS = hif.createEmpty(surface);
		
		Map<VVertex, VFace> vertexFaceMap = new HashMap<VVertex, VFace>();
		Map<VEdge, VVertex> edgeVertexMap = new HashMap<VEdge, VVertex>();
		Map<VFace, VFace> faceFaceMap = new HashMap<VFace, VFace>();
		Map<VEdge, VEdge> edgeEdgeMap1 = new HashMap<VEdge, VEdge>();
		
		Map<VEdge, double[]> edgePointMap = new HashMap<VEdge, double[]>();
		updateEdgePoints(edgePointMap);
		
		mgl.execute(surface, newHDS, vertexFaceMap, edgeVertexMap, faceFaceMap, edgeEdgeMap1, aSet);
		for(VEdge e : surface.getPositiveEdges()) {
			aSet.set(Position.class,edgeVertexMap.get(e),edgePointMap.get(e));
		}
		newLayer.set(newHDS);
		hif.addLayer(newLayer);
	}

	private void updateRadiusSlider() {
		double min1 = Double.POSITIVE_INFINITY;
		double min2 = Double.POSITIVE_INFINITY;
		for(int i = 0; i < modVector.getDimension(); ++i) {
			if((modVector.getEntry(i) == -1.0)&&(radii.getEntry(i)<min1)) {
				min1 = radii.getEntry(i);
			}
			if((modVector.getEntry(i) == 1.0)&&(radii.getEntry(i)<min2)) {
				min2 = radii.getEntry(i);
			}
		}
		min = -1.0*min2;
		radiusModel.setMinimum(2.0*min);
		max = min1;
		radiusModel.setMaximum(2.0*max);
		radiusModel.setStepSize((min1+min2)/500.0);
		radiusSlider.setEnabled(true);
		radiusLabel.setEnabled(true);
		radiusSpinner.setEnabled(true);
		radiusModel.setValue(0.0);
		radiusSlider.setValue((int) (-min/(max-min)*100));
	}

	private void initModVector() {
		VVertex vv = surface.getVertex(0);
		modVector = new ArrayRealVector(surface.numVertices());
		modVector.setEntry(vv.getIndex(), 1.0);
		LinkedList<VVertex> queue = new LinkedList<VVertex>();
		queue.add(vv);
		while(!queue.isEmpty()) {
			vv = queue.poll();
			List<VVertex> neighbors = HalfEdgeUtils.neighboringVertices(vv);
			for(VVertex v : neighbors) {
				if(modVector.getEntry(v.getIndex()) == 0.0) {
					modVector.setEntry(v.getIndex(), -1.0*modVector.getEntry(vv.getIndex()));
					queue.add(v);
				}
			}
		}
		for(VEdge e : surface.getPositiveEdges()) {
			if(modVector.getEntry(e.getStartVertex().getIndex())*modVector.getEntry(e.getTargetVertex().getIndex()) > 0) {
				System.err.println("Mistake in coloring."); return;
			}
		}
	}

	private void updateCircles() {
		hif.removeTemporaryGeometry(circles);
		circles = new SceneGraphComponent("Circles");
		Appearance app = new Appearance();
		app.setAttribute(VERTEX_SHADER + "." + SMOOTH_SHADING, true);
		app.setAttribute(VERTEX_DRAW, false);
		app.setAttribute(EDGE_DRAW, true);
		app.setAttribute(FACE_DRAW, true);
		circles.setAppearance(app);
		Geometry geometry = IndexedLineSetUtility.circle(40);
		Map<VEdge, double[]> edgePointMap = new HashMap<VEdge, double[]>();
		updateEdgePoints(edgePointMap);
		circles.addChild(touchingPoints(edgePointMap.values()));
		for(VFace ff : surface.getFaces()) {
			SceneGraphComponent comp = new SceneGraphComponent("circle " + ff.getIndex());
			comp.setGeometry(geometry);
			VEdge ee = ff.getBoundaryEdge();
			//TODO: average over 4 circles to improve quality!
			double[] p1 = edgePointMap.get(ee);
			ee = ee.getNextEdge();
			double[] p2 = edgePointMap.get(ee);
			ee = ee.getNextEdge();
			double[] p3 = edgePointMap.get(ee);
			
			double[] c = circumCircle(p1,p2,p3);
			
			double[] N = Rn.crossProduct(null, Rn.subtract(null, p2, p1), Rn.subtract(null, p3, p1));
			MatrixBuilder mb = MatrixBuilder.euclidean();
			mb.translate(c[0], c[1], c[2]);
			mb.rotateFromTo(new double[] {0,0,1}, N);
			mb.scale(c[3]);
			mb.assignTo(comp);
			circles.addChild(comp);
		}
	}
	
	private SceneGraphComponent touchingPoints(Collection<double[]> values) {
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(values.size());
		double[][] coords = new double[values.size()][3];
		int i = 0;
		for(double[] v : values) {
			System.arraycopy(v, 0, coords[i], 0, 3);
			++i;
		}
		psf.setVertexCoordinates(coords);
		psf.update();
		SceneGraphComponent points = new SceneGraphComponent("Touching points");
		Appearance app = new Appearance();
		app.setAttribute(VERTEX_DRAW, true);
		points.setAppearance(app);
		points.setGeometry(psf.getGeometry());
		return points;
	}

	private void updateEdgePoints(Map<VEdge, double[]> edgePointMap) {
		for(VEdge ee : surface.getPositiveEdges()) {
			int si = ee.getStartVertex().getIndex();
			int ti = ee.getTargetVertex().getIndex();
			double[] sp = aSet.getD(Position3d.class, ee.getStartVertex());
			double[] tp = aSet.getD(Position3d.class, ee.getTargetVertex());
			double rs = radii.getEntry(si)+radiusModel.getNumber().doubleValue()*modVector.getEntry(si);
			double rt = radii.getEntry(ti)+radiusModel.getNumber().doubleValue()*modVector.getEntry(ti);
			double[] mp = Rn.linearCombination(null, rt/(rs+rt), sp, rs/(rs+rt), tp);
			edgePointMap.put(ee, mp);
			edgePointMap.put(ee.getOppositeEdge(), mp);
		}
	}

	private void updateSpheres() {
		hif.removeTemporaryGeometry(spheres);
		spheres = new SceneGraphComponent("Spheres");
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute(CommonAttributes.EDGE_DRAW, false);
		app.setAttribute(CommonAttributes.FACE_DRAW, true);
		app.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		app.setAttribute(CommonAttributes.POLYGON_SHADER +"."+ CommonAttributes.SMOOTH_SHADING, true);
		app.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, new Color(0.9f, 0.5f, 0));
		app.setAttribute(CommonAttributes.POLYGON_SHADER +"."+ CommonAttributes.TRANSPARENCY, 0.8);
		spheres.setAppearance(app);
		SceneGraphComponent sphere = SphereUtility.tessellatedCubeSphere(5,true);
		for(VVertex vv : surface.getVertices()) {
			SceneGraphComponent child = new SceneGraphComponent("Vertex "+ vv.getIndex());
			child.addChild(sphere);
			double radius = getRadius(vv.getIndex());
			child.setTransformation(new Transformation(MatrixBuilder.euclidean().translate(aSet.getD(Position3d.class, vv)).scale(radius).getArray()));
			spheres.addChild(child);
		}
	}

	private double getRadius(int i) {
		double radius = radii.getEntry(i)+radiusModel.getNumber().doubleValue()*modVector.getEntry(i);
		return radius;
	}

	private void updateOrthoSpheres() {
		hif.removeTemporaryGeometry(orthoSpheres);
		orthoSpheres = new SceneGraphComponent("Orthogonal spheres");
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute(CommonAttributes.EDGE_DRAW, false);
		app.setAttribute(CommonAttributes.FACE_DRAW, true);
		app.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		app.setAttribute(CommonAttributes.POLYGON_SHADER +"."+ CommonAttributes.SMOOTH_SHADING, true);
		app.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, new Color(0.0f, 0.0f, 0.8f));
		app.setAttribute(CommonAttributes.POLYGON_SHADER +"."+ CommonAttributes.TRANSPARENCY, 0.8);
		orthoSpheres.setAppearance(app);
		SceneGraphComponent sphere = SphereUtility.tessellatedCubeSphere(5,true);
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(1);
		psf.setVertexCoordinates(new double[][]{{0.0,0.0,0.0}});
		psf.update();
		SceneGraphComponent centerComp = new SceneGraphComponent("center");
		centerComp.setGeometry(psf.getPointSet());
		Appearance centerApp = new Appearance();
		centerApp.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		centerComp.setAppearance(centerApp);
		for(VFace f : surface.getFaces()) {
			RealVector orthoSphere = calculateOrthoSphere(f);
			double[] center = new double[3];
			center[0] = orthoSphere.getEntry(0);
			center[1] = orthoSphere.getEntry(1);
			center[2] = orthoSphere.getEntry(2);
			SceneGraphComponent child = new SceneGraphComponent("face "+ f.getIndex());
			child.addChild(sphere);
			child.addChild(centerComp);
			MatrixBuilder mb = MatrixBuilder.euclidean();
			mb.translate(center);
			mb.scale(Math.sqrt(Rn.euclideanNormSquared(center)-orthoSphere.getEntry(3)));
			mb.assignTo(child);
			orthoSpheres.addChild(child);
		}
	}

	private RealVector calculateOrthoSphere(VFace f) {
		RealMatrix A = new Array2DRowRealMatrix(4, 4);
		RealVector b = new ArrayRealVector(4);
		int i = 0;
		for(VVertex v : HalfEdgeUtils.boundaryVertices(f)) {
			double[] coords = aSet.getD(Position3d.class, v);
			A.setEntry(i,0,2*coords[0]);
			A.setEntry(i,1,2*coords[1]);
			A.setEntry(i,2,2*coords[2]);
			A.setEntry(i, 3, -1.0);
			double actRadius = getRadius(v.getIndex());
			b.setEntry(i,Rn.euclideanNormSquared(coords) - actRadius*actRadius);
			++i;
		}
		SingularValueDecomposition dec = new SingularValueDecomposition(A);
		DecompositionSolver solver = dec.getSolver();
		return solver.solve(b);
	}

	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> RealVector calculateRadii(HDS surface, AdapterSet as) {
		RealMatrix A = new OpenMapRealMatrix(surface.numEdges()/2, surface.numVertices());
		RealVector b = new ArrayRealVector(surface.numEdges()/2);
		int i = 0;
		for(E ee : surface.getPositiveEdges()) {
			A.setEntry(i, ee.getStartVertex().getIndex(), 1.0);
			A.setEntry(i, ee.getTargetVertex().getIndex(),1.0);
			b.setEntry(i, Rn.euclideanNorm(as.getD(EdgeVector.class, ee)));
			++i;
		}
		SingularValueDecomposition dec = new SingularValueDecomposition(A);
//		QRDecomposition dec = new QRDecomposition(A);
		DecompositionSolver solver = dec.getSolver();
		return solver.solve(b);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (blockListeners) return;
		blockListeners = true;
		if (radiusSlider == e.getSource()) {
			double radius = radiusSlider.getValue();
			radiusModel.setValue(min + radius*(max-min)/100.0);
		}
		if (radiusSpinner == e.getSource()) {
			double val = radiusModel.getNumber().doubleValue();
			radiusSlider.setValue((int) ((val-min)/(max-min)*100));
		}
		updateSpheres();
		updateCircles();
		updateOrthoSpheres();
		hif.addTemporaryGeometry(spheres);
		hif.addTemporaryGeometry(circles);
		hif.addTemporaryGeometry(orthoSpheres);
		blockListeners = false;
//		if(spheres.getOwner() != null) {
//			hif.addTemporaryGeometry(spheres);
//		}
//		if(circles.getOwner() != null) {
//			hif.addTemporaryGeometry(spheres);
//		}
		


		spheres.setVisible(spheresBox.isSelected());
		circles.setVisible(circlesBox.isSelected());
		orthoSpheres.setVisible(orthoSpheresBox.isSelected());
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
//		hif.addHalfedgeListener(this);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Vetex spheres");
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public void dataChanged(HalfedgeLayer layer) {
		hif.removeTemporaryGeometry(circles);
		hif.removeTemporaryGeometry(spheres);
		hif.removeTemporaryGeometry(orthoSpheres);
		spheresBox.setEnabled(false);
		circlesBox.setEnabled(false);
		orthoSpheresBox.setEnabled(false);
		radiusSlider.setEnabled(false);
		radiusLabel.setEnabled(false);
		radiusSpinner.setEnabled(false);
	}

	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void dualize(HDS hds, AdapterSet a) {
		a.add(new EdgeSignAdapter());
		List<Map<V, double[]>> newCoordsMaps = new LinkedList<Map<V, double[]>>();
		Set<V> rootSet = getRadomInnerPoints(hds, 10);
		for (V v0 : rootSet) {
			Map<V, double[]> newCoordsMap = new HashMap<V, double[]>();
			newCoordsMaps.add(newCoordsMap);
			HashSet<V> readyVertices = new HashSet<V>();
			LinkedList<V> vertexQueue = new LinkedList<V>();
	//		V v0 = hds.getVertex(0);
	//		V v0 = guessRootVertex(hds, 100);
			vertexQueue.offer(v0);
			//vertex 0 in 0.0;
			double[] v0Pos = a.getD(Position3d.class, v0);
			newCoordsMap.put(v0, v0Pos.clone());
			while (!vertexQueue.isEmpty()){
				V v = vertexQueue.poll();
				double[] startCoord = newCoordsMap.get(v);
				List<E> star = HalfEdgeUtils.incomingEdges(v);
				for (E e : new HashSet<E>(star)) {
					V v2 = e.getStartVertex();
					if (readyVertices.contains(v2))
						continue;
					else {
						vertexQueue.offer(v2);
						readyVertices.add(v2);
					}
					double[] vec = a.getD(EdgeVector.class,e);
					double factor = getRadius(v.getIndex())*getRadius(v2.getIndex());						
					Boolean edgeSign = a.get(EdgeSign.class, e, Boolean.class);
					double scale = (edgeSign ? -1 : 1) / factor;
					vec[0] *= scale;
					vec[1] *= scale;
					vec[2] *= scale;
					Rn.add(vec, vec, startCoord);
					vec = Pn.homogenize(null, vec);
					newCoordsMap.put(v2, vec);
				}
			}
		}
		Map<V, double[]> newCoordsMap = new HashMap<V, double[]>();
		for (Map<V, double[]> map : newCoordsMaps) {
			for (V v : map.keySet()) {
				if (!newCoordsMap.containsKey(v)) {
					newCoordsMap.put(v, new double[4]);
				}
				double[] p = map.get(v);
				double[] pMean = newCoordsMap.get(v);
				Rn.add(pMean, pMean, p);
			}
		}
		for (V v : newCoordsMap.keySet()) {
			double[] p = newCoordsMap.get(v);
			Pn.dehomogenize(p, p);
		}
		
		for (V v : hds.getVertices()){
			double[] p = newCoordsMap.get(v);
			if (p != null) {
				a.set(Position.class, v, p);
			}
		}	
	}
	
	public  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<V> getRadomInnerPoints(HDS hds, int mcSamples) {
		Random rnd = new Random();
		rnd.setSeed(hds.numVertices());
		
		Set<V> result = new HashSet<V>();
		for (int i = 0; i < Math.min(mcSamples, hds.numVertices()); i++) {
			int sampleIndex = rnd.nextInt(hds.numVertices());
			V sampleVertex = hds.getVertex(sampleIndex);
			if (!HalfEdgeUtils.isBoundaryVertex(sampleVertex)) {
				result.add(sampleVertex);				
			}
		}
		
		if (result.isEmpty()) {
			result.add(hds.getVertex(rnd.nextInt(hds.numVertices())));
		}
		return result;
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
