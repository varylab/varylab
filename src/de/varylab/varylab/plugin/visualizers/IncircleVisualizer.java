package de.varylab.varylab.plugin.visualizers;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_SHADER;
import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryEdges;
import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryVertices;
import static de.varylab.varylab.plugin.ddg.ChristoffelTransform.NormalMethod.Face_Sphere;
import static java.lang.Math.PI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Length;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.VectorField;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.ddg.ChristoffelTransform;
import de.varylab.varylab.utilities.Disk;
import de.varylab.varylab.utilities.GeometryUtility;

public class IncircleVisualizer extends VisualizerPlugin implements ActionListener,ChangeListener {

	private JPanel
		panel = new JPanel();
	private JCheckBox
		useDisks = new JCheckBox("Use Disks");
	private SpinnerNumberModel
		diskHeightModel = new SpinnerNumberModel(0.3, 0.01, 100.0, 0.01),
		circleResModel = new SpinnerNumberModel(20, 3, 200, 1);
	private JSpinner
		diskHeightSpinner = new JSpinner(diskHeightModel),
		circleResSpinner = new JSpinner(circleResModel);
	private SceneGraphComponent
		root = new SceneGraphComponent();
	private Appearance
		app = new Appearance("Circle Appearance");
	
	public IncircleVisualizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();
		panel.add(useDisks, cr);
		panel.add(new JLabel("Resolution"), cl);
		panel.add(circleResSpinner, cr);
		panel.add(new JLabel("Disk Height"), cl);
		panel.add(diskHeightSpinner, cr);
		useDisks.addActionListener(this);
		circleResSpinner.addChangeListener(this);
		diskHeightSpinner.addChangeListener(this);
		
		app.setAttribute(VERTEX_SHADER + "." + SMOOTH_SHADING, true);
		app.setAttribute(VERTEX_DRAW, false);
		app.setAttribute(EDGE_DRAW, true);
		app.setAttribute(FACE_DRAW, true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		updateContent();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		updateContent();
	}
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		root = new SceneGraphComponent("Incircles");
		root.setAppearance(app);
		int circleRes = circleResModel.getNumber().intValue();
		Geometry geometry = null;
		if (useDisks.isSelected()) {
			app.setAttribute(EDGE_DRAW, false);
			double h = diskHeightModel.getNumber().doubleValue();
			geometry = new Disk(circleRes, h);
		} else {
			app.setAttribute(EDGE_DRAW, true);
			geometry = IndexedLineSetUtility.circle(circleRes);
		}
		for (F f : hds.getFaces()) {
			List<V> bd = boundaryVertices(f);
			List<E> ebd = boundaryEdges(f);
			if (bd.size() != 4) continue;
			double l1 = a.get(Length.class, ebd.get(0), Double.class);
			double l2 = a.get(Length.class, ebd.get(1), Double.class);
			double l3 = a.get(Length.class, ebd.get(2), Double.class);
			double l4 = a.get(Length.class, ebd.get(3), Double.class);
			double ie = l1 + l3 - l2 - l4;
			// do we have a meaningfull incircle
			if (ie * ie > l1 * 1E-2) continue;
			SceneGraphComponent comp = new SceneGraphComponent("circle " + f.getIndex());
			comp.setGeometry(geometry);
			double[] c = GeometryUtility.getIncircle(f, a);
			double[] N = a.getD(Normal.class, f);
			MatrixBuilder mb = MatrixBuilder.euclidean();
			mb.translate(c[0], c[1], c[2]);
			mb.rotateFromTo(new double[] {0,0,1}, N);
			mb.scale(c[3], c[3], useDisks.isSelected() ? 1 : c[3]);
			mb.assignTo(comp);
			root.addChild(comp);
		}
	}
	
	
	@VectorField
	private class EdgeNormalAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {
		
		public EdgeNormalAdapter() {
			super(null, VEdge.class, null, double[].class, true, false);
		}
		
		@Override
		public double[] getEdgeValue(VEdge e, AdapterSet a) {
			return ChristoffelTransform.getAssociatedNormal(e, a);
		}
		
		@Override
		public String toString() {
			return "Associated Edge Normals";
		}
		
	}
	
	@VectorField
	private class EdgeVectorAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {
		
		public EdgeVectorAdapter() {
			super(null, VEdge.class, null, double[].class, true, false);
		}
		
		@Override
		public double[] getEdgeValue(VEdge e, AdapterSet a) {
			return ChristoffelTransform.getAssociatedEdgeVector(e, PI/8, a, Face_Sphere);
		}
		
		@Override
		public String toString() {
			return "Associated Edge Vectors";
		}
		
	}
	
	@Override
	public AdapterSet getAdapters() {
		AdapterSet aSet = new AdapterSet();
		aSet.add(new EdgeNormalAdapter());
		aSet.add(new EdgeVectorAdapter());
		return aSet;
	}
	
	@Override
	public SceneGraphComponent getComponent() {
		return root;
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	@Override
	public String getName() {
		return "Incircles";
	}

}
