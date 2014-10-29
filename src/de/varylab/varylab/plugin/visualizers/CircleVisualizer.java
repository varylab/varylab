package de.varylab.varylab.plugin.visualizers;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_SHADER;

import java.awt.Color;
import java.awt.EventQueue;
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
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.varylab.varylab.utilities.Disk;

public class CircleVisualizer extends DataVisualizerPlugin implements ActionListener, ChangeListener {

	private JCheckBox
		useDisks = new JCheckBox("Use Disks"),
		showEdges = new JCheckBox("Edges");
	
	private SpinnerNumberModel
		diskHeightModel = new SpinnerNumberModel(0.2, 0.01, 100.0, 0.01),
		tubeRadiusModel = new SpinnerNumberModel(0.03, 0.01, 100.0, 0.01),
		circleResModel = new SpinnerNumberModel(20, 3, 200, 1);
	
	private JSpinner
		diskHeightSpinner = new JSpinner(diskHeightModel),
		circleResSpinner = new JSpinner(circleResModel),
		tubeRadiusSpinner = new JSpinner(tubeRadiusModel);
	
	private Appearance
		app = new Appearance("Circle Appearance");
	
	private JPanel 
		optionsPanel = new JPanel();

	private CircleVisualization 
		actVis = null;

	private boolean 
		listenersDisabled = false;
	
	public CircleVisualizer() {
		initOptionPanel();
	}
	
	private void initOptionPanel() {
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();
		optionsPanel.add(useDisks, cl);
		optionsPanel.add(showEdges, cr);
		
		optionsPanel.add(new JLabel("Resolution"), cl);
		optionsPanel.add(circleResSpinner, cr);
		optionsPanel.add(new JLabel("Disk Height"), cl);
		optionsPanel.add(diskHeightSpinner, cr);
		optionsPanel.add(new JLabel("Tube radius"), cl);
		optionsPanel.add(tubeRadiusSpinner, cr);
		
		useDisks.addActionListener(this);
		showEdges.addActionListener(this);
		circleResSpinner.addChangeListener(this);
		diskHeightSpinner.addChangeListener(this);
		tubeRadiusSpinner.addChangeListener(this);
		
		app.setAttribute(VERTEX_SHADER + "." + SMOOTH_SHADING, true);
		app.setAttribute(VERTEX_DRAW, false);
		app.setAttribute(EDGE_DRAW, true);
		app.setAttribute(FACE_DRAW, true);
	}
	
	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source) {
		CircleVisualization vis = new CircleVisualization(layer, source, this, type);
		vis.useDisks = useDisks.isSelected();
		vis.showDiskEdges = showEdges.isSelected();
		vis.circleRes = circleResModel.getNumber().intValue();
		vis.tubeRadius = tubeRadiusModel.getNumber().doubleValue();
		vis.diskHeight = diskHeightModel.getNumber().doubleValue();
		return vis;
	}

	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		boolean accept = false;
		accept |= a.checkType(Circle3d.class);
		accept |= a.checkType(double[].class);
		return accept;
	}

	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		actVis = (CircleVisualization) visualization;

		listenersDisabled = true;
		useDisks.setSelected(actVis.useDisks);
		showEdges.setSelected(actVis.showDiskEdges);
		diskHeightModel.setValue(actVis.diskHeight);
		circleResModel.setValue(actVis.circleRes);
		listenersDisabled = false;

		return optionsPanel;
	}
	
	public class CircleVisualization extends AbstractDataVisualization {

		private boolean
			useDisks = false,
			showDiskEdges = true;
		
		private int
			circleRes = 36;
		
		private double
			tubeRadius = 0.1,
			diskHeight = 0.2;

		private Color 
			color = Color.DARK_GRAY;
		
		private Geometry
			geometry = null;
		
		private SceneGraphComponent
			circlesComponent = new SceneGraphComponent("Circle Visualizer");
		
		public CircleVisualization(HalfedgeLayer layer, Adapter<?> source, DataVisualizer visualizer, NodeType type) {
			super(layer, source, visualizer, type);
			circlesComponent.setAppearance(app);
			getLayer().addTemporaryGeometry(circlesComponent);
		}

		@Override
		public void update() {
			getLayer().removeTemporaryGeometry(circlesComponent);
			if (!isActive()) {
				circlesComponent.setVisible(false);
				return;
			} else {
				circlesComponent.setVisible(true);
			}
			
			HalfEdgeDataStructure<?, ?, ?> hds = getLayer().get();
			
			AdapterSet aSet = getLayer().getEffectiveAdapters();

			Adapter<?> circleAdapter = getSource();

			List<? extends Node<?,?,?>> nodes = null;
			switch (getType()) {
			case Vertex:
				nodes = hds.getVertices();
				break;
			case Edge:
				nodes = hds.getEdges();
				break;
			default:
				nodes = hds.getFaces();
				break;
			}

			circlesComponent.removeAllChildren();
			if(useDisks) {
				geometry = new Disk(circleRes,diskHeight);
			} else {
				geometry = IndexedLineSetUtility.circle(circleRes);
			}
			
			for(Node<?, ?, ?> n : nodes) {
				Circle3d circle = (Circle3d) circleAdapter.get(n, aSet);
				if(useDisks) {
					circlesComponent.addChild(createDiscComponent(circle));
				} else {
					circlesComponent.addChild(createTubeComponent(circle));
				}
			}

			circlesComponent.setName(getName());
			circlesComponent.setVisible(true);

			if(useDisks) {
				app.setAttribute(CommonAttributes.EDGE_DRAW, showDiskEdges);
				app.setAttribute(LINE_SHADER + "." + TUBES_DRAW, showDiskEdges);
				app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, tubeRadius);
			} else {
				app.setAttribute(CommonAttributes.EDGE_DRAW, true);
				app.setAttribute(LINE_SHADER + "." + TUBES_DRAW, true);
				app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, tubeRadius);
				
			}
			app.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR,	color);
			app.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.SMOOTH_SHADING, true);
			getLayer().addTemporaryGeometry(circlesComponent);
		}

		private SceneGraphComponent createTubeComponent(Circle3d circle) {
			SceneGraphComponent sgc = new SceneGraphComponent();
			sgc.setGeometry(geometry);
			Matrix T = MatrixBuilder.euclidean().translate(circle.center).scale(circle.radius).rotateFromTo(new double[]{0,0,1}, circle.normal).getMatrix();
			T.assignTo(sgc);
			return sgc;
		}

		private SceneGraphComponent createDiscComponent(Circle3d circle) {
			SceneGraphComponent sgc = new SceneGraphComponent();
			sgc.setGeometry(geometry);
			Matrix T = MatrixBuilder.euclidean().translate(circle.center).scale(circle.radius,circle.radius,diskHeight/2.0).rotateFromTo(new double[]{0,0,1}, circle.normal).getMatrix();
			T.assignTo(sgc);
			return sgc;
		}

		@Override
		public void dispose() {
			getLayer().removeTemporaryGeometry(circlesComponent);
		}
		
	}
	
	
	public static class Circle3d {
		private double[]
			center = {0.0, 0.0, 0.0},
			normal = {1.0, 0.0, 0.0};
		
		private double
			radius = 1.0;
		
		public Circle3d(double[] center, double r, double[] normal) {
			if(center.length != 3) {
				throw new RuntimeException(Circle3d.class.getSimpleName() + ": Dimension of center must be 3.");
			}
			System.arraycopy(center, 0, this.center, 0, 3);
			if(normal.length != 3) {
				throw new RuntimeException(Circle3d.class.getSimpleName() + ": Dimension of normal must be 3.");
			}
			System.arraycopy(normal, 0, this.normal, 0, 3);
			radius = r;
		}
		
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(listenersDisabled) {
			return;
		}
		updateGeometry();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(listenersDisabled) {
			return;
		}
		updateGeometry();
	}

	private void updateGeometry() {
		if(actVis == null) {
			return;
		}
		actVis.useDisks = useDisks.isSelected();
		actVis.circleRes = circleResModel.getNumber().intValue();
		actVis.tubeRadius = tubeRadiusModel.getNumber().doubleValue();
		actVis.diskHeight = diskHeightModel.getNumber().doubleValue();
		actVis.showDiskEdges = showEdges.isSelected();
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				actVis.update();
			}
		};
		EventQueue.invokeLater(r);
	}
}
