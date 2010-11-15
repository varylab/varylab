package de.varylab.varylab.plugin.remeshing;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import geom3d.Point;
import geom3d.Triangle;

import java.awt.GridBagConstraints;
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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import de.jreality.math.Matrix;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.experimental.ManagedContent;
import de.jreality.ui.AppearanceInspector;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.discreteconformal.heds.CoFace;
import de.varylab.discreteconformal.heds.CoHDS;
import de.varylab.discreteconformal.heds.CoVertex;
import de.varylab.discreteconformal.heds.adapter.CoPositionAdapter;
import de.varylab.discreteconformal.heds.adapter.CoTexturePositionAdapter;
import de.varylab.discreteconformal.heds.bsp.KdTree;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public class SurfaceRemeshingPlugin extends ShrinkPanelPlugin implements ActionListener {

	private enum Pattern {
		Triangles,
		TrianglesQuantized,
		Quads,
		QuadsQuantized,
		Hexagons;
	}
	
	private ContentAppearance
		contentAppearance = null;
	private JComboBox
		patternCombo = new JComboBox(Pattern.values());
	private SpinnerNumberModel
		lookUpHeuristicModel = new SpinnerNumberModel(3, 1, 100, 1);
	private JSpinner
		lookUpHeuristicSpinner = new JSpinner(lookUpHeuristicModel);
	
	// plug-in connection
	private ManagedContent
		managedContent = null;
	private HalfedgeInterface
		hcp = null;
	
	// ui components
	private GridBagConstraints
		gbc1 = new GridBagConstraints(),
		gbc2 = new GridBagConstraints();
	private JButton
		meshingButton = new JButton("Remesh"),
		liftingButton = new JButton("Lift/Flat");
	private JCheckBox
		newVerticesBox = new JCheckBox("Insert new Vertices"),
		forceOnLatticeBox = new JCheckBox("Force corners on Lattice");
	
	private CoHDS surface = new CoHDS();
	private VHDS remesh = new VHDS();
	
	private HashMap<VVertex, Point>
		flatCoordMap = new HashMap<VVertex,Point>();
	
	private boolean lifted = false;
	
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
		shrinkPanel.add(new JLabel("Lookup Heuristic"), gbc1);
		shrinkPanel.add(lookUpHeuristicSpinner, gbc2);
		
		shrinkPanel.add(meshingButton, gbc1);
		shrinkPanel.add(liftingButton, gbc2);

		shrinkPanel.add(newVerticesBox, gbc2);
		shrinkPanel.add(forceOnLatticeBox, gbc2);
		
		meshingButton.addActionListener(this);
		liftingButton.addActionListener(this);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource(); 
		if (s == meshingButton) {
			try {
				remeshSurface();
			} catch(RemeshingException re) {
				System.err.println(re.getMessage());
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
	
	
	private void flattenMesh() {
		for(VVertex v : remesh.getVertices()) {
			v.setPosition(flatCoordMap.get(v));
		}
		hcp.set(remesh);
	}


	private void liftMesh() {
		if(hcp.getAdapters().query(CoPositionAdapter.class) == null) {
			hcp.addGlobalAdapter(new CoPositionAdapter(),true);
		}
		if(hcp.getAdapters().query(CoTexturePositionAdapter.class) == null) {
			hcp.addGlobalAdapter(new CoTexturePositionAdapter(false),true);
		}
		remesh = hcp.get(remesh);
		RemeshingUtility.projectOntoBoundary(remesh, surface);
		mapInnerVertices(surface,remesh);
		hcp.set(remesh);
	}


	private void remeshSurface() throws RemeshingException {
		lifted = false;
		if(hcp.getAdapters().query(CoPositionAdapter.class) == null) {
			hcp.addGlobalAdapter(new CoPositionAdapter(),true);
		}
		if(hcp.getAdapters().query(CoTexturePositionAdapter.class) == null) {
			hcp.addGlobalAdapter(new CoTexturePositionAdapter(false),true);
		}
		remesh.clear();
		surface = hcp.get(surface);
		if (surface == null) {
			return;
		}
		Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
		if (surface.getVertex(0).getTextureCoord() == null) {
			JOptionPane.showMessageDialog(w, "Surface has no texture coordinates.", "Error", WARNING_MESSAGE);
			return;
		}
		HalfedgeSelection sel = hcp.getSelection();
		Set<CoVertex> featureSet = sel.getVertices(surface);
		featureSet.retainAll(HalfEdgeUtils.boundaryVertices(surface));
		
		AppearanceInspector ai = contentAppearance.getAppearanceInspector();
		Matrix texMatrix = ai.getTextureMatrix();

		for (CoVertex v : surface.getVertices()) {
			v.setTextureCoord(TextureUtility.transformCoord(v.getTextureCoord(), texMatrix));
		}
		
		Matrix texInvMatrix = texMatrix.getInverse();
		
		Rectangle2D bbox = RemeshingUtility.getTextureBoundingBox(surface);

		// create pattern
		LatticeRemesher remesher = new LatticeRemesher(newVerticesBox.isSelected(),forceOnLatticeBox.isSelected());
		switch (getPattern()) {
		case Triangles:
			TriangleRemeshingUtility.createRectangularTriangleMesh(remesh, bbox);
			break;
		case TrianglesQuantized:
			remesher.setLattice(new TriangleLattice(bbox));
			remesh = remesher.fitToBoundary(surface); 
			for (VVertex v : remesh.getVertices()) {
				double[] tex = v.texcoord;
				v.texcoord = TextureUtility.transformCoord(tex, texInvMatrix);
			}
			hcp.set(remesh);
			return;
		case Quads:
			QuadLattice qLattice = new QuadLattice(bbox);
			remesh = qLattice.getHDS();
			break;
		case QuadsQuantized:
			remesher.setLattice(new QuadLattice(bbox));
			remesh = remesher.fitToBoundary(surface); 
			for (VVertex v : remesh.getVertices()) {
				double[] tex = v.texcoord;
				v.texcoord = TextureUtility.transformCoord(tex, texInvMatrix);
			}
			hcp.set(remesh);
			return;
		case Hexagons:
			HexRemeshingUtility.createHexMesh(remesh,bbox);
			break;
		}
		
		// construct kd-tree
		KdTree<CoVertex> kdTree = new KdTree<CoVertex>(surface.getVertices(), 10, false);
		
		// map inner vertices
		int lookUp = lookUpHeuristicModel.getNumber().intValue();
		Map<VVertex, CoFace> texFaceMap = new HashMap<VVertex, CoFace>();
		Set<VVertex> cutSet = new HashSet<VVertex>(remesh.getVertices());
		for (VVertex v : remesh.getVertices()) {
			Point patternPoint = v.getPosition();
			CoFace f = RemeshingUtility.getContainingFace(v, surface, kdTree, lookUp);
			if (f == null) continue;
			List<CoVertex> b = HalfEdgeUtils.boundaryVertices(f);
			Triangle tTex = new Triangle(b.get(0).getTextureCoord(), b.get(1).getTextureCoord(), b.get(2).getTextureCoord());
			Triangle tPos = new Triangle(b.get(0).getPosition(), b.get(1).getPosition(), b.get(2).getPosition());
			Point bary = getBarycentric(patternPoint, tTex);
			Point newPos = getCoordinate(bary, tPos);
			v.setPosition(newPos);
			texFaceMap.put(v, f);
			cutSet.remove(v);
		}
		
		
		Set<VVertex> overlap = new HashSet<VVertex>();
		Set<VFace> faceOverlap = new HashSet<VFace>();
		for (VVertex v : remesh.getVertices()) {
			if (cutSet.contains(v)) {
				continue;
			}
			CoFace mapFace = texFaceMap.get(v);
			List<VEdge> star = HalfEdgeUtilsExtra.getEdgeStar(v);
			for (VEdge e : star) {
				VVertex incident = e.getStartVertex();
				if (!cutSet.contains(incident)) {
					continue;
				}
				VFace overlapFace = e.getLeftFace();
				if (overlapFace == null) {
					continue;
				}
				faceOverlap.add(overlapFace);
				List<VVertex> b = HalfEdgeUtils.boundaryVertices(overlapFace);
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
			double[] tex = v.texcoord;
			v.texcoord = TextureUtility.transformCoord(tex, texInvMatrix);
		}
		for (CoVertex v : surface.getVertices()) {
			Point tex = v.getTextureCoord();
			v.setTextureCoord(TextureUtility.transformCoord(tex, texInvMatrix));	
		}
		
		RemeshingUtility.cutTargetBoundary(faceOverlap, overlap, surface, featureSet);
		
		for (VVertex v : overlap) {
			TopologyAlgorithms.removeVertex(v);
		}
		hcp.set(remesh);
	}
	
	
	
	private void mapInnerVertices(CoHDS hds, VHDS r) {
		// map inner vertices
		KdTree<CoVertex> kdTree = new KdTree<CoVertex>(hds.getVertices(), 10, false);
		int lookUp = lookUpHeuristicModel.getNumber().intValue();
		for (VVertex v : r.getVertices()) {
			Point patternPoint = v.getPosition();
			CoFace f = RemeshingUtility.getContainingFace(v, hds, kdTree, lookUp);
			if (f == null) { 
				System.err.println("no face containing " + v + " found!");
				continue;
			}
			
			List<CoVertex> b = HalfEdgeUtils.boundaryVertices(f);
			Triangle tTex = new Triangle(b.get(0).getTextureCoord(), b.get(1).getTextureCoord(), b.get(2).getTextureCoord());
			Triangle tPos = new Triangle(b.get(0).getPosition(), b.get(1).getPosition(), b.get(2).getPosition());
			Point bary = getBarycentric(patternPoint, tTex);
			Point newPos = getCoordinate(bary, tPos);
			flatCoordMap.put(v, patternPoint);
			v.setPosition(newPos);
		}
	}

	/**
	 * Convert to barycentric
	 * @param p
	 * @param t
	 * @return
	 */
	protected static Point getBarycentric(Point p , Triangle t) {
		Point l = new Point();
		double x1 = t.getA().x();
		double y1 = t.getA().y();
		double x2 = t.getB().x();
		double y2 = t.getB().y();
		double x3 = t.getC().x();
		double y3 = t.getC().y();		
		double det = (x1 - x3)*(y2 - y3) - (y1 - y3)*(x2 - x3);
		l.setX(((y2 - y3)*(p.x() - x3) - (x2 - x3)*(p.y() - y3)) / det);
		l.setY(((x1 - x3)*(p.y() - y3) - (y1 - y3)*(p.x() - x3)) / det);
		l.setZ(1 - l.x() - l.y());
		return l;
	}
	
	protected static Point getCoordinate(Point b, Triangle t) {
		Point r = new Point();
		r.setX(b.x()*t.getA().x() + b.y()*t.getB().x() + b.z()*t.getC().x());
		r.setY(b.x()*t.getA().y() + b.y()*t.getB().y() + b.z()*t.getC().y());
		r.setZ(b.x()*t.getA().z() + b.y()*t.getB().z() + b.z()*t.getC().z());
		return r;
	}
	
	
	private Pattern getPattern() {
		return (Pattern)patternCombo.getSelectedItem();
	}
	
	
	@Override 
	public void install(Controller c) throws Exception {
		super.install(c);
		hcp = c.getPlugin(HalfedgeInterface.class);
		managedContent = c.getPlugin(ManagedContent.class);
		contentAppearance = c.getPlugin(ContentAppearance.class);
	}
	
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		managedContent.removeAll(getClass());
	} 
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "lookUpHeuristic", lookUpHeuristicModel.getNumber().intValue());
		c.storeProperty(getClass(), "pattern", patternCombo.getSelectedIndex());
		c.storeProperty(getClass(), "newVertices", newVerticesBox.isSelected());
		c.storeProperty(getClass(), "forceLattice", forceOnLatticeBox.isSelected());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		lookUpHeuristicModel.setValue(c.getProperty(getClass(), "lookUpHeuristic", lookUpHeuristicModel.getNumber().intValue()));
		patternCombo.setSelectedIndex(c.getProperty(getClass(), "pattern", 0));
		newVerticesBox.setSelected(c.getProperty(getClass(), "newVertices", true));
		forceOnLatticeBox.setSelected(c.getProperty(getClass(), "forceLattice", true));
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
