package de.varylab.varylab.plugin.visualizers;

import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryVertices;

import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.varylab.varylab.plugin.ddg.ChristoffelTransfom;

public class IncircleVisualizer extends VisualizerPlugin {

	private JPanel
		panel = new JPanel();
	private SceneGraphComponent
		root = new SceneGraphComponent();
	private Appearance
		app = new Appearance("Circle Appearance");
	private int
		circleRes = 40;
	
	public IncircleVisualizer() {
		panel.setLayout(new GridBagLayout());
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
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
		IndexedLineSet ils = IndexedLineSetUtility.circle(circleRes);
		for (F f : hds.getFaces()) {
			List<V> bd = boundaryVertices(f);
			if (bd.size() != 4) continue;
			SceneGraphComponent comp = new SceneGraphComponent("circle " + f.getIndex());
			comp.setGeometry(ils);
			double[] c = ChristoffelTransfom.getIncircle(f, a);
			double[] N = a.getD(Normal.class, f);
			MatrixBuilder mb = MatrixBuilder.euclidean();
			mb.translate(c[0], c[1], c[2]);
			mb.scale(c[3]);
			mb.rotateFromTo(new double[] {0,0,1}, N);
			mb.assignTo(comp);
			root.addChild(comp);
		}
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
