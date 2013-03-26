package de.varylab.varylab.plugin.remeshing;

import static javax.swing.JOptionPane.WARNING_MESSAGE;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.math.Matrix;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.job.AbstractJob;
import de.jreality.plugin.job.JobQueuePlugin;
import de.jreality.ui.AppearanceInspector;
import de.jreality.util.LoggingSystem;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.bsp.KdTree;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class SurfaceRemeshingPlugin extends ShrinkPanelPlugin implements ActionListener {

	private enum Pattern {
		Triangles,
		TrianglesQuantized,
		Quads,
		QuadsQuantized,
		QuadsSingularities;
	}
	
	private ContentAppearance
		contentAppearance = null;
	// plug-in connection
	private HalfedgeInterface
		hcp = null;
	private JobQueuePlugin
		jobQueuePlugin = null;
	
	// ui components
	private JComboBox
		patternCombo = new JComboBox(Pattern.values());	
	private GridBagConstraints
		gbc1 = new GridBagConstraints(),
		gbc2 = new GridBagConstraints();
	private JButton
		meshingButton = new JButton("Remesh"),
		liftingButton = new JButton("Lift/Flat");
	private JPanel
		quantOptsPanel = new JPanel();
	private JCheckBox
		newVerticesBox = new JCheckBox("Insert new Vertices"),
		forceOnLatticeBox = new JCheckBox("Force corners on Lattice"),
		projectiveCoordsBox = new JCheckBox("Use projective texture");
	
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
		gbc1.insets = new Insets(2, 2, 2, 2);
		gbc1.gridwidth = GridBagConstraints.RELATIVE;
		gbc1.weightx = 0.0;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc2.insets = new Insets(2, 2, 2, 2);
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.weightx = 1.0;
		gbc2.fill = GridBagConstraints.BOTH;

		shrinkPanel.add(new JLabel("Pattern"), gbc1);
		shrinkPanel.add(patternCombo, gbc2);
		shrinkPanel.add(meshingButton, gbc2);

		quantOptsPanel.setLayout(new GridLayout(4, 1));
		quantOptsPanel.add(projectiveCoordsBox);
		projectiveCoordsBox.setSelected(true);
		quantOptsPanel.add(newVerticesBox);
		quantOptsPanel.add(forceOnLatticeBox);
		quantOptsPanel.add(liftingButton);
		
		shrinkPanel.add(quantOptsPanel, gbc2);
		
		meshingButton.addActionListener(this);
		liftingButton.addActionListener(this);
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
		public void execute() throws Exception {
			Object s = actionSource; 
			Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			if (s == meshingButton) {
				try {
					remeshSurface();
				} catch(RemeshingException re) {
					JOptionPane.showMessageDialog(w, re.getMessage(), "Error", WARNING_MESSAGE);
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
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		RemeshingJob job = new RemeshingJob(e.getSource());
		jobQueuePlugin.queueJob(job);
	}
	
	
	private void flattenMesh() {
		if (remeshPosMap.size() != remesh.numVertices()) {
			Logger log = LoggingSystem.getLogger(SurfaceRemeshingPlugin.class);
			log.log(Level.WARNING, "Surface out of sync with the plugin");
			return;
		}
		for(VVertex v : remesh.getVertices()) {
			v.P = remeshPosMap.get(v.getIndex());
		}
		hcp.set(remesh);
	}


	private void liftMesh() {
		remesh = hcp.get(new VHDS());
		HalfedgeSelection selection = hcp.getSelection();
		RemeshingUtility.alignRemeshBoundary(remesh, surface, hcp.getAdapters());
		if(newOldFaceMap.isEmpty()) {
			RemeshingUtility.mapInnerVertices(surface, surfaceKD, remesh, hcp.getAdapters());
		} else {
			RemeshingUtility.mapInnerVertices(surface, newOldFaceMap, remesh, hcp.getAdapters());
		}
		hcp.set(remesh);
		hcp.setSelection(selection);
	}


	private void remeshSurface() throws RemeshingException {
		lifted = false;
		remeshPosMap.clear();
		newOldFaceMap.clear();
		surface = hcp.get(surface);
		AdapterSet a = hcp.getAdapters();
		if (surface.getVertex(0).T == null) {
			throw new RemeshingException("Surface has no texture coordinates.");
		}
		surfaceKD = new KdTree<VVertex, VEdge, VFace>(surface, a, 10, false);
		HalfedgeSelection sel = hcp.getSelection();
		Set<VVertex> featureSet = sel.getVertices(surface);
		featureSet.retainAll(HalfEdgeUtils.boundaryVertices(surface));
		
		AppearanceInspector ai = contentAppearance.getAppearanceInspector();
		Matrix texMatrix = ai.getTextureMatrix();

		for (VVertex v : surface.getVertices()) {
			texMatrix.transformVector(v.T);
		}
		
		Matrix texInvMatrix = texMatrix.getInverse();
		Rectangle2D bbox = RemeshingUtility.getTextureBoundingBox(surface, a);

		// create pattern
		LatticeRemesher<VVertex, VEdge, VFace, VHDS> remesher = 
			new LatticeRemesher<VVertex, VEdge, VFace, VHDS>(newVerticesBox.isSelected(),
															 forceOnLatticeBox.isSelected()
		);
		remesh = new VHDS();
		switch (getPattern()) {
		case Triangles:
			TriangleRemeshingUtility.createRectangularTriangleMesh(remesh, bbox);
			break;
		case TrianglesQuantized:
			remesher.setLattice(new TriangleLattice<VVertex, VEdge, VFace, VHDS>(remesh, a, bbox));
			remesh = remesher.remesh(surface, a); 
			remeshPosMap.clear();
			for (VVertex v : remesh.getVertices()) {
				texInvMatrix.transformVector(v.P);
				texInvMatrix.transformVector(v.T);
				remeshPosMap.put(v.getIndex(), v.P);
			}
			for (VVertex v : surface.getVertices()) {
				texInvMatrix.transformVector(v.T);
			}
			hcp.set(remesh);
			return;
		case Quads:
			QuadLattice<VVertex, VEdge, VFace, VHDS> qLattice = new QuadLattice<VVertex, VEdge, VFace, VHDS>(remesh, a, bbox);
			remesh = qLattice.getHDS();
			break;
		case QuadsQuantized:
			remesher.setLattice(new QuadLattice<VVertex, VEdge, VFace, VHDS>(remesh, a, bbox));
			remesh = remesher.remesh(surface, a); 
			for (VVertex v : remesh.getVertices()) {
				texInvMatrix.transformVector(v.P);
				texInvMatrix.transformVector(v.T);
				remeshPosMap.put(v.getIndex(), v.P);
			}
			for (VVertex v : surface.getVertices()) {
				texInvMatrix.transformVector(v.T);
			}
			hcp.set(remesh);
			return;
		case QuadsSingularities:
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
				texInvMatrix.transformVector(v.T);
			}

			hcp.set(remesh);
			HalfedgeSelection selection = hcp.getSelection();
			for(VVertex v : remesh.getVertices()) {
				if(localQuadRemesher.isTextureVertex(v) || HalfEdgeUtils.isBoundaryVertex(v)) {
					selection.add(v);
				}
			}
			hcp.setSelection(selection);
			
			return;
		}
		
		// map inner vertices
		Map<VVertex, VFace> texFaceMap = new HashMap<VVertex, VFace>();
		Set<VVertex> cutSet = new HashSet<VVertex>(remesh.getVertices());
		for (VVertex v : remesh.getVertices()) {
			double[] patternPoint = a.getD(Position3d.class, v);
			VFace f = RemeshingUtility.getContainingFace(v, surface, a, surfaceKD);
			if (f == null) continue;
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
			texInvMatrix.transformVector(v.T);
		}
		for (VVertex v : surface.getVertices()) {
			texInvMatrix.transformVector(v.T);
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
		contentAppearance = c.getPlugin(ContentAppearance.class);
		jobQueuePlugin = c.getPlugin(JobQueuePlugin.class);
	}
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "pattern", patternCombo.getSelectedIndex());
		c.storeProperty(getClass(), "newVertices", newVerticesBox.isSelected());
		c.storeProperty(getClass(), "forceLattice", forceOnLatticeBox.isSelected());
		c.storeProperty(getClass(), "useProjectiveCoords", projectiveCoordsBox.isSelected());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		patternCombo.setSelectedIndex(c.getProperty(getClass(), "pattern", 0));
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

}
