package de.varylab.varylab.plugin.remeshing;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.job.AbstractJob;
import de.jreality.plugin.job.JobQueuePlugin;
import de.jreality.ui.AppearanceInspector;
import de.jreality.util.LoggingSystem;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.Position2d;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.bsp.KdTree;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.TriangulatePlugin;
import de.jtem.halfedgetools.plugin.misc.VertexEditorPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.discreteconformal.plugin.DiscreteConformalPlugin;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;

public class SurfaceRemeshingPlugin extends ShrinkPanelPlugin implements ActionListener {

	private Logger
		log = Logger.getLogger(SurfaceRemeshingPlugin.class.getName());
	
	private enum Pattern {
		Triangles,
		Quads,
		TrianglesQuantized,
		QuadsQuantized,
		QuadsSingularities;
		
		@Override
		public String toString() {
			switch (this) {
			case QuadsQuantized:
				return "Boundary Aligned Quads";
			case TrianglesQuantized:
				return "Boundary Aligned Triangles";
			case QuadsSingularities:
				return "Quads With Singularities";
			default:
				return super.toString();
			}
		};
	}
	
	private ContentAppearance
		contentAppearance = null;
	// plug-in connection
	private HalfedgeInterface
		hcp = null;
	private JobQueuePlugin
		jobQueuePlugin = null;
	
	// ui components
	private JComboBox<Pattern>
		patternCombo = new JComboBox<>();	
	private GridBagConstraints
		c1 = new GridBagConstraints(),
		c2 = new GridBagConstraints();
	private JButton
		meshingButton = new JButton("Remesh"),
		liftingButton = new JButton("Lift/Flat");
	private JPanel
		quantOptsPanel = new JPanel();
	private JCheckBox
		expertModeChecker = new JCheckBox("Expert Mode"),
		newVerticesBox = new JCheckBox("Insert new Vertices", true),
		forceOnLatticeBox = new JCheckBox("Force corners on Lattice", true),
		projectiveCoordsBox = new JCheckBox("Use projective texture", true),
		relaxInteriorBox = new JCheckBox("Relax interior", true);
	
	private VHDS 
		surface = new VHDS(),
		remesh = new VHDS();
	private KdTree<VVertex, VEdge, VFace>
		surfaceKD = null;
	
	private HashMap<Integer, double[]>
		remeshPosMap = new HashMap<Integer, double[]>();
	private boolean 
		lifted = false;
	private Map<VFace,VFace> 
		newOldFaceMap = new HashMap<VFace, VFace>();
	
	
	public SurfaceRemeshingPlugin() {
	}
	
	
	private void createLayout() {
		boolean expert = isExpertMode();
		shrinkPanel.removeAll();
		c1.insets = new Insets(2, 2, 2, 2);
		c1.gridwidth = GridBagConstraints.RELATIVE;
		c1.weightx = 0.0;
		c1.fill = GridBagConstraints.BOTH;
		c2.insets = new Insets(2, 2, 2, 2);
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 1.0;
		c2.fill = GridBagConstraints.BOTH;

		shrinkPanel.add(expertModeChecker, c2);
		shrinkPanel.add(new JLabel("Pattern"), c1);
		ComboBoxModel<Pattern> model = null;
		if (expert) {
			model = new DefaultComboBoxModel<>(Pattern.values());
		} else {
			Pattern[] patterns = {Pattern.Triangles, Pattern.Quads, Pattern.TrianglesQuantized, Pattern.QuadsQuantized};
			model = new DefaultComboBoxModel<>(patterns);
		}
		patternCombo.setModel(model);
		
		shrinkPanel.add(patternCombo, c2);
		shrinkPanel.add(meshingButton, c2);
		
		if (expert) {
			shrinkPanel.add(liftingButton, c2);
			quantOptsPanel.removeAll();
			quantOptsPanel.setLayout(new GridBagLayout());
			quantOptsPanel.setBorder(BorderFactory.createTitledBorder("Method Options"));
			quantOptsPanel.add(projectiveCoordsBox, c2);
			quantOptsPanel.add(newVerticesBox, c2);
			quantOptsPanel.add(forceOnLatticeBox, c2);
			quantOptsPanel.add(relaxInteriorBox, c2);
			shrinkPanel.add(quantOptsPanel, c2);
		}
		
		shrinkPanel.revalidate();
	}


	private boolean isExpertMode() {
		boolean expert = expertModeChecker.isSelected();
		return expert;
	}
	
	private void connectGUIListeners() {
		meshingButton.addActionListener(this);
		liftingButton.addActionListener(this);
		expertModeChecker.addActionListener(this);
	}
	
	
	private class RemeshingJob extends AbstractJob {

		private Object 
			actionSource = null;
		
		public RemeshingJob(Object actionSource) {
			this.actionSource = actionSource;
		}
		
		@Override
		public String getJobName() {
			return "Surface Remeshing";
		}

		@Override
		public void executeJob() throws Exception {
			Object s = actionSource; 
			if (s == meshingButton) {
				try {
					remeshSurface();
					if (!isExpertMode() && 
						(getPattern() == Pattern.QuadsQuantized ||
						getPattern() == Pattern.TrianglesQuantized ||
						getPattern() == Pattern.QuadsSingularities)
					) {
						liftMesh();
					}
				} catch(Exception re) {
					log.warning("remeshing failed: " + re);
					showExceptionDialog(re);
					fireJobFailed(re);
					return;
				}
			}
			if(s == liftingButton) {
				if(!lifted) {
					liftMesh();
				} else {
					flattenMesh();
				}
				lifted = !lifted;
			}
		}
		
	}
	
	private void showExceptionDialog(final Exception e) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(shrinkPanel), e, "Remeshing error", JOptionPane.ERROR_MESSAGE);
			}
		};
		EventQueue.invokeLater(r);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == meshingButton || e.getSource() == liftingButton) {
			RemeshingJob job = new RemeshingJob(e.getSource());
			jobQueuePlugin.queueJob(job);
		}
		if (e.getSource() == expertModeChecker) {
			createLayout();
		}
	}
	
	
	private void flattenMesh() {
		if (remeshPosMap.size() != remesh.numVertices()) {
			Logger log = LoggingSystem.getLogger(SurfaceRemeshingPlugin.class);
			log.log(Level.WARNING, "Surface out of sync with the plugin");
			return;
		}
		for(VVertex v : remesh.getVertices()) {
			double[] flattened = remeshPosMap.get(v.getIndex());
			System.arraycopy(flattened, 0, v.getP(), 0, 4);
		}
		hcp.set(remesh);
	}


	private void liftMesh() {
		remesh = hcp.get(new VHDS());
		Selection selection = hcp.getSelection();
		AdapterSet adapters = new AdapterSet(hcp.getAdapters());
		RemeshingUtility.alignRemeshBoundary(remesh, surface, adapters);
		NurbsUVAdapter nurbsUVAdapter = hcp.getActiveAdapters().query(NurbsUVAdapter.class);
		if(nurbsUVAdapter!= null) {
			adapters.add(nurbsUVAdapter);
		}
		if(newOldFaceMap.isEmpty()) {
			RemeshingUtility.mapInnerVertices(surface, surfaceKD, remesh, adapters);
		} else {
			RemeshingUtility.mapInnerVertices(surface, newOldFaceMap, remesh, adapters);
		}
		hcp.set(remesh);
		hcp.setSelection(selection);
	}


	private void remeshSurface() throws RemeshingException {
		lifted = false;
		remeshPosMap.clear();
		newOldFaceMap.clear();
		surface = hcp.get(surface);
		log.fine("start remeshing surface: " + surface);
		AdapterSet a = hcp.getAdapters();
		if (surface.getVertex(0).getT() == null) {
			throw new RemeshingException("Surface has no texture coordinates.");
		}
		surfaceKD = new KdTree<VVertex, VEdge, VFace>(surface, new AdapterSet(new VTexturePositionPosition3dAdapter()), 10, false);
		Selection sel = hcp.getSelection();
		Set<VVertex> featureSet = new TreeSet<VVertex>(sel.getVertices(surface));
		featureSet.retainAll(HalfEdgeUtils.boundaryVertices(surface));
		
		AppearanceInspector ai = contentAppearance.getAppearanceInspector();
		Matrix texMatrix = ai.getTextureMatrix();

		for (VVertex v : surface.getVertices()) {
			texMatrix.transformVector(v.getT());
		}
		
		Matrix texInvMatrix = texMatrix.getInverse();
		Rectangle2D bbox = RemeshingUtility.getTextureBoundingBox(surface, a);

		// create pattern
		LatticeRemesher<VVertex, VEdge, VFace, VHDS> remesher = 
			new LatticeRemesher<VVertex, VEdge, VFace, VHDS>(newVerticesBox.isSelected(),
															 forceOnLatticeBox.isSelected(),
															 relaxInteriorBox.isSelected()
		);
		remesh = new VHDS();
		log.fine("using method " + getPattern());
		switch (getPattern()) {
		case Triangles:
			TriangleRemeshingUtility.createRectangularTriangleMesh(remesh, bbox);
			break;
		case TrianglesQuantized: {
			TriangleLattice<VVertex, VEdge, VFace, VHDS> lattice = new TriangleLattice<VVertex, VEdge, VFace, VHDS>(remesh, a, bbox);
			lattice.setTexInvTransform(Rn.times(null,texInvMatrix.getEntry(3,3),new double[]{texInvMatrix.getEntry(0, 0), texInvMatrix.getEntry(0, 1), texInvMatrix.getEntry(1, 0), texInvMatrix.getEntry(1, 1)}));
			remesher.setLattice(lattice);
			try {
				remesh = remesher.remesh(surface, a);
			} catch (Throwable e) {
				log.fine("error: " + e);
				log.fine("reverting texture transform");
				for (VVertex v : surface.getVertices()) {
					texInvMatrix.transformVector(v.getT());
				}
				throw e;
			}
			remeshPosMap.clear();
			for (VVertex v : remesh.getVertices()) {
				texInvMatrix.transformVector(v.getP());
				texInvMatrix.transformVector(v.getT());
				remeshPosMap.put(v.getIndex(), v.getP());
			}
			for (VVertex v : surface.getVertices()) {
				texInvMatrix.transformVector(v.getT());
			}
			if (!isExpertMode()) {
				hcp.setNoUndo(remesh);
			} else {
				hcp.set(remesh);
			}
			return;
		}
		case Quads: {
			QuadLattice<VVertex, VEdge, VFace, VHDS> qLattice = new QuadLattice<VVertex, VEdge, VFace, VHDS>(remesh, a, bbox);
			remesh = qLattice.getHDS();
			break;
		}
		case QuadsQuantized: {
			QuadLattice<VVertex, VEdge, VFace, VHDS> lattice = new QuadLattice<VVertex, VEdge, VFace, VHDS>(remesh, a, bbox);
			lattice.setTexInvTransform(Rn.times(null,texInvMatrix.getEntry(3,3),new double[]{texInvMatrix.getEntry(0, 0), texInvMatrix.getEntry(0, 1), texInvMatrix.getEntry(1, 0), texInvMatrix.getEntry(1, 1)}));			
			remesher.setLattice(lattice);
			try {
				remesh = remesher.remesh(surface, a);
			} catch (RemeshingException e) {
				for (VVertex v : surface.getVertices()) {
					texInvMatrix.transformVector(v.getT());
				}
				throw e;
			}
			for (VVertex v : remesh.getVertices()) {
				texInvMatrix.transformVector(v.getP());
				texInvMatrix.transformVector(v.getT());
				remeshPosMap.put(v.getIndex(), v.getP());
			}
			for (VVertex v : surface.getVertices()) {
				texInvMatrix.transformVector(v.getT());
			}
			if(!isExpertMode()) {
				hcp.setNoUndo(remesh);
			} else {
				hcp.set(remesh);
			}
			return;
		}
		case QuadsSingularities: {
			LocalQuadRemesher<VVertex, VEdge, VFace, VHDS> localQuadRemesher =
				new LocalQuadRemesher<VVertex, VEdge, VFace, VHDS>();
			newOldFaceMap = localQuadRemesher.remesh(surface,remesh,a, projectiveCoordsBox.isSelected());
			remeshPosMap.clear();
			for (VVertex v : remesh.getVertices()) {
				double[] coord = a.getD(Position4d.class, v);
				texInvMatrix.transformVector(coord);
				a.set(Position.class, v, coord.clone());
				a.set(TexturePosition.class, v, coord.clone());
				remeshPosMap.put(v.getIndex(), coord);
			}
			for (VVertex v : surface.getVertices()) {
				texInvMatrix.transformVector(v.getT());
			}

			if(!isExpertMode()) {
				hcp.setNoUndo(remesh);
			} else {
				hcp.set(remesh);
			}
			Selection selection = hcp.getSelection();
			for(VVertex v : remesh.getVertices()) {
				if(localQuadRemesher.isTextureVertex(v) || HalfEdgeUtils.isBoundaryVertex(v)) {
					selection.add(v);
				}
			}
			hcp.setSelection(selection);
			
			return;
		}
		}
		
		// map inner vertices
		Map<VVertex, VFace> texFaceMap = new HashMap<VVertex, VFace>();
		Set<VVertex> cutSet = new HashSet<VVertex>(remesh.getVertices());
		for (VVertex v : remesh.getVertices()) {
			double[] patternPoint = a.getD(Position2d.class, v);
			VFace f = RemeshingUtility.getContainingFace(v, surface, a, surfaceKD);
			if (f == null) {
				continue;
			} 
			double[] bary = RemeshingUtility.getBarycentricTexturePoint(patternPoint, f, a);
			double[] newPos = RemeshingUtility.getPointFromBarycentric(bary, f, a);
			a.set(Position.class, v, newPos);
			texFaceMap.put(v, f);
			cutSet.remove(v);
		}
		
		
		Set<VVertex> overlap = new HashSet<VVertex>();
		Set<VFace> faceOverlap = new HashSet<VFace>();
		for (VVertex v : remesh.getVertices()) {
			if (cutSet.contains(v)) {
				continue;
			}
			VFace mapFace = texFaceMap.get(v);
			List<VEdge> star = HalfEdgeUtilsExtra.getEdgeStar(v);
			for (VEdge e : star) {
				VVertex incident = e.getStartVertex();
				if (!cutSet.contains(incident)) {
					continue;
				}
				// left face overlap
				VFace overlapLeft = e.getLeftFace();
				if (overlapLeft == null) continue;
				faceOverlap.add(overlapLeft);
				List<VVertex> b = HalfEdgeUtils.boundaryVertices(overlapLeft);
				for (VVertex bv : b) {
					if (cutSet.contains(bv)) {
						overlap.add(bv);
						texFaceMap.put(bv, mapFace);
					}
				}
				// right face overlap
				VFace overlapRight = e.getRightFace();
				if (overlapRight == null) continue;
				faceOverlap.add(overlapRight);
				b = HalfEdgeUtils.boundaryVertices(overlapRight);
				for (VVertex bv : b) {
					if (cutSet.contains(bv)) {
						overlap.add(bv);
						texFaceMap.put(bv, mapFace);
					}
				}
			}
		}
		cutSet.removeAll(overlap);
		
		// remove extra faces
		for (VVertex v : cutSet) {
			TopologyAlgorithms.removeVertex(v);
		}
		
		// transform tex coordinates
		for (VVertex v : remesh.getVertices()) {
			texInvMatrix.transformVector(v.getT());
		}
		for (VVertex v : surface.getVertices()) {
			texInvMatrix.transformVector(v.getT());
		}
		
		RemeshingUtility.cutTargetBoundary(faceOverlap, overlap, surface, featureSet, a);
		hcp.set(remesh);
	}
	
	
	
	private Pattern getPattern() {
		return (Pattern)patternCombo.getSelectedItem();
	}
	
	
	@Override 
	public void install(Controller c) throws Exception {
		super.install(c);
		hcp = c.getPlugin(HalfedgeInterface.class);
		c.getPlugin(DomainLineCutPlugin.class);
		c.getPlugin(DomainSegmentCutPlugin.class);
		contentAppearance = c.getPlugin(ContentAppearance.class);
		jobQueuePlugin = c.getPlugin(JobQueuePlugin.class);
		createLayout();
		connectGUIListeners();
	}
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "expoertMode", expertModeChecker.isSelected());
		c.storeProperty(getClass(), "pattern", patternCombo.getSelectedIndex());
		c.storeProperty(getClass(), "newVertices", newVerticesBox.isSelected());
		c.storeProperty(getClass(), "forceLattice", forceOnLatticeBox.isSelected());
		c.storeProperty(getClass(), "useProjectiveCoords", projectiveCoordsBox.isSelected());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		expertModeChecker.setSelected(c.getProperty(getClass(), "expertMode", expertModeChecker.isSelected()));
		try {
			patternCombo.setSelectedIndex(c.getProperty(getClass(), "pattern", 0));
		} catch (Exception e) {}
		newVerticesBox.setSelected(c.getProperty(getClass(), "newVertices", true));
		forceOnLatticeBox.setSelected(c.getProperty(getClass(), "forceLattice", true));
		projectiveCoordsBox.setSelected(c.getProperty(getClass(), "useProjectiveCoords", true));
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Surface Remeshing", "Stefan Sechelmann");
		return info;
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
		v.registerPlugin(SurfaceRemeshingPlugin.class);
		v.registerPlugin(QuadMeshGenerator.class);
		v.registerPlugin(VertexEditorPlugin.class);
		v.registerPlugin(TriangulatePlugin.class);
		v.registerPlugin(DiscreteConformalPlugin.class);
		v.startup();
	}
	
	@Position3d
	private class VTexturePositionPosition3dAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

		public VTexturePositionPosition3dAdapter() {
			super(VVertex.class, null, null, double[].class, true, false);
		}
		
		@Override
		public double getPriority() {
			return 1;
		}
		
		@Override
		public double[] getVertexValue(VVertex v, AdapterSet a) {
			return new double[]{v.getT()[0]/v.getT()[3], v.getT()[1]/v.getT()[3], v.getT()[2]/v.getT()[3]};
		}
		
	}
}
