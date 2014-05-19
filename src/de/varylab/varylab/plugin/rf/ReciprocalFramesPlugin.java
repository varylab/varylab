package de.varylab.varylab.plugin.rf;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.basic.View;
import de.jreality.ui.LayoutFactory;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.BaryCenter;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgePluginFactory;
import de.jtem.halfedgetools.plugin.algorithm.generator.PrimitivesGenerator;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.projgeom.PlueckerLineGeometry;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabMain;

public class ReciprocalFramesPlugin extends ShrinkPanelPlugin implements ChangeListener, ActionListener {

	private SpinnerNumberModel
		angleModel = new SpinnerNumberModel(0.0, -180, 180, 1.0),
		lengthModel = new SpinnerNumberModel(1.0, 0.0, 100, 0.1);
	
	private JSpinner
		angleSpinner = new JSpinner(angleModel),
		lengthSpinner = new JSpinner(lengthModel);
	
	private JPanel
		panel = new JPanel();
	
	private Map<VEdge, VEdge>
		rodEdgeMap = new HashMap<>(),
		edgeRodMap = new HashMap<>();
		
	private JButton
		initButton = new JButton("Initialize");

	private HalfedgeInterface 
		hif = null;
	
	private VHDS 
		baseHDS = null;
	
	private HalfedgeLayer
		rodLayer = null;
	
	public ReciprocalFramesPlugin() {
		angleSpinner.addChangeListener(this);
		lengthSpinner.addChangeListener(this);
		initButton.addActionListener(this);
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		panel.add(initButton, rc);
		panel.add(new JLabel("Turning angle"),lc);
		angleSpinner.setEnabled(false);
		panel.add(angleSpinner,rc);
		panel.add(new JLabel("Rod length"),lc);
		lengthSpinner.setEnabled(false);
		panel.add(lengthSpinner,rc);
		
		
		shrinkPanel.add(panel);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if(src == angleSpinner || src == lengthSpinner) {
			turnEdges(angleModel.getNumber().doubleValue(), lengthModel.getNumber().doubleValue());
		}
		
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		Object src = e.getSource();
		if(src == initButton) {
			initialize();
		}
	}
	
	private void initialize() {
		baseHDS = hif.get(new VHDS());
		rodLayer = new HalfedgeLayer(hif);
		rodLayer.setName("Rods for " + hif.getActiveLayer().getName());
		hif.addLayer(rodLayer);
		
		angleSpinner.setEnabled(true);
		angleModel.setValue(0.0);
		lengthSpinner.setEnabled(true);
		lengthModel.setValue(1.0);
		turnEdges(0.0,1.0);
	}

	private void turnEdges(double alpha, double scale) {
		AdapterSet as = hif.getAdapters();
		
		VHDS rodHDS = convertToIndividualRods(baseHDS, as);
		rotateRods(rodHDS, rodEdgeMap, as, alpha, scale);
		
		rodLayer.set(rodHDS);
		hif.update();
		rodLayer.addAdapter(new RodDistanceAdapter(), false);
		rodLayer.addAdapter(new RodConnectivityAdapter(getRodConnectivityMap()), false);
	}

	private Map<VEdge, VEdge> getRodConnectivityMap() {
		Map<VEdge, VEdge> rcm = new HashMap<VEdge, VEdge>();
		for(VEdge e : rodEdgeMap.keySet()) {
			rcm.put(e, edgeRodMap.get(rodEdgeMap.get(e).getNextEdge()));
		}
		return rcm;
	}

	private void rotateRods(VHDS rods, Map<VEdge, VEdge> rodMap, AdapterSet as, double alpha, double scale) {
		for(VEdge e : rods.getPositiveEdges()) {
			double[] ev = Rn.times(null, scale*0.5, as.getD(EdgeVector.class, e));
			double[] midpoint = as.getD(BaryCenter.class, e);
			double[] n = as.getD(Normal.class, rodMap.get(e));
			double[] rotatedEdge = rotateEdge(ev,n,alpha);
			
			double[] ps = Rn.subtract(null, midpoint, rotatedEdge);
			double[] pt = Rn.add(null, midpoint, rotatedEdge);
			as.set(Position.class, e.getStartVertex(), ps);
			as.set(Position.class, e.getTargetVertex(), pt);
		}
	}

	private VHDS convertToIndividualRods(VHDS original, AdapterSet as) {
		VHDS rods = new VHDS();
		for(VEdge e : original.getPositiveEdges()) {
			VVertex vs = rods.addNewVertex();
			VVertex vt = rods.addNewVertex();
			VEdge e1 = rods.addNewEdge();
			VEdge e2 = rods.addNewEdge();
			e1.setIsPositive(true);
			e1.linkOppositeEdge(e2);
			e1.linkNextEdge(e2);
			e2.linkNextEdge(e1);
			e1.setTargetVertex(vs);
			e2.setTargetVertex(vt);
			as.set(Position.class, vs, as.getD(Position4d.class,e.getStartVertex()));
			as.set(Position.class, vt, as.getD(Position4d.class,e.getTargetVertex()));
			rodEdgeMap.put(e1, e);
			edgeRodMap.put(e, e1);
			rodEdgeMap.put(e2, e.getOppositeEdge());
			edgeRodMap.put(e.getOppositeEdge(), e2);
		}
		return rods;
	}

	private double[] rotateEdge(double[] ev, double[] n, double alpha) {
		Matrix m = MatrixBuilder.euclidean().rotate(alpha/360*2*Math.PI, n).getMatrix();
		m.transformVector(ev);
		return ev;
	}

	@Override
	public void install(Controller c) throws Exception {
		hif = c.getPlugin(HalfedgeInterface.class);
		c.getPlugin(RodDistanceOptimizer.class);
		super.install(c);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	public static void main(String[] args) {
		NativePathUtility.set("native");
		JRHalfedgeViewer.initHalfedgeFronted();
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addContentUI();
		v.addContentSupport(ContentType.Raw);
		v.registerPlugin(VarylabMain.class);
		v.registerPlugin(ReciprocalFramesPlugin.class);
		v.registerPlugin(ConsolePlugin.class);
		v.registerPlugin(PrimitivesGenerator.class);
		v.registerPlugins(HalfedgePluginFactory.createDataVisualizationPlugins());
		v.startup();
	}
	
	private class RodDistanceAdapter extends AbstractAdapter<Double> {
		
		public RodDistanceAdapter() {
			super(Double.class, true, false);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Edge.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>
		> Double getE(E e, AdapterSet a) {
			VEdge hdsE = rodEdgeMap.get(e);
			double[] l1 = PlueckerLineGeometry.lineFromPoints(null, a.getD(Position4d.class, e.getTargetVertex()), a.getD(Position4d.class, e.getStartVertex()));
			VEdge nextRod = edgeRodMap.get(hdsE.getNextEdge());
			double[] l2 = PlueckerLineGeometry.lineFromPoints(null, a.getD(Position4d.class, nextRod.getTargetVertex()), a.getD(Position4d.class, nextRod.getStartVertex()));
			return PlueckerLineGeometry.distanceBetweenLines(l1, l2);
		}

	}
}
