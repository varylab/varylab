package de.varylab.varylab.plugin.visualizers;

import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryVertices;

import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
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
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

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
			double[] c = getIncircle(f, a);
			double[] N = a.getD(Normal.class, f);
			MatrixBuilder mb = MatrixBuilder.euclidean();
			mb.translate(c[0], c[1], c[2]);
			mb.scale(c[3]);
			mb.rotateFromTo(new double[] {0,0,1}, N);
			mb.assignTo(comp);
			root.addChild(comp);
		}
	}
	
	
	
	public static < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getIncircle(F f, AdapterSet as) {
		List<V> bd = boundaryVertices(f);
		if (bd.size() != 4) return new double[] {0,0,0,1};
		double[] p1 = as.getD(Position3d.class, bd.get(0));
		double[] p2 = as.getD(Position3d.class, bd.get(1));
		double[] p3 = as.getD(Position3d.class, bd.get(2));
		double[] p4 = as.getD(Position3d.class, bd.get(3));
		double p = Rn.euclideanDistance(p1, p3);
		double q = Rn.euclideanDistance(p2, p4);
		double a = Rn.euclideanDistance(p1, p2);
		double b = Rn.euclideanDistance(p2, p3);
		double c = Rn.euclideanDistance(p3, p4);
		double d = Rn.euclideanDistance(p4, p1);
		double[] v2 = Rn.subtract(null, p2, p1);
		double[] v4 = Rn.subtract(null, p4, p1);
		double alpha = Rn.euclideanAngle(v2, v4) / 2;
		double s = 0.5 * (a+b+c+d);
		double r = p*p*q*q - (a-b)*(a-b)*(a+b-s)*(a+b-s);
		r = Math.sqrt(r) / (2*s);
		double len = r / Math.sin(alpha);
		Rn.normalize(v2, v2);
		Rn.normalize(v4, v4);
		double[] dir = Rn.average(null, new double[][] {v2, v4});
		Rn.setToLength(dir, dir, len);
		double[] m = Rn.add(null, p1, dir);
		return new double[] {m[0], m[1], m[2], r};
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
