package de.varylab.varylab.plugin.ddg;

import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryVertices;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.math.Rn;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class ChristoffelTransfom extends AlgorithmDialogPlugin {

	private CentralExtensionSubdivision
		ceSubdivider = null;
	private JPanel
		panel = new JPanel();
	private JCheckBox
		useCentralExtensionChecker = new JCheckBox("Use Central Extension");
	private SpinnerNumberModel
		associateModel = new SpinnerNumberModel(0.0, 0.0, 360.0, 0.1);
	private JSpinner
		associateSpinner = new JSpinner(associateModel);
	private HalfedgeInterface hif = null;
	
	public ChristoffelTransfom() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();
		panel.add(useCentralExtensionChecker, cr);
		panel.add(new JLabel("Associaled Family"), cl);
		panel.add(associateSpinner, cr);
	}
	
	@Override
	public JPanel getDialogPanel() {
		return panel;
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface EdgeSign {}
	
	@EdgeSign
	private class EdgeSignAdapter extends AbstractAdapter<Boolean> {
		
		private Map<Object, Boolean>
			signMap = new HashMap<Object, Boolean>();
		
		public EdgeSignAdapter() {
			super(Boolean.class, true, true);
		}

		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Boolean getE(E e, AdapterSet a) {
			Boolean s = signMap.get(e);
			return s == null ? false : s;
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> void setE(E e, Boolean value, AdapterSet a) {
			signMap.put(e, value);
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Edge.class.isAssignableFrom(nodeClass);
		}

	}
	
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  void labelFaceOf(E edge, AdapterSet a) {
		E actEdge = edge.getNextEdge();
		while (actEdge != edge){
			E prev = actEdge.getPreviousEdge();
			boolean prevLabel = a.get(EdgeSign.class, prev, Boolean.class);
			a.set(EdgeSign.class, actEdge, !prevLabel);
			a.set(EdgeSign.class, actEdge.getOppositeEdge(), !prevLabel);
			actEdge = actEdge.getNextEdge();
		}
		boolean edgeLabel = a.get(EdgeSign.class, edge, Boolean.class);
		boolean prevLabel = a.get(EdgeSign.class, edge.getPreviousEdge(), Boolean.class);
		if (prevLabel == edgeLabel){
			System.err.println("could not label face " + edge.getLeftFace() + " correctly, continuing...");
		}
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createEdgeLabels(HDS hds, AdapterSet a) {
		HashSet<E> pendingEdges = new HashSet<E>();
		Stack<E> edgeStack = new Stack<E>();
		for (E e : hds.getEdges()) {
			pendingEdges.add(e);
		}
		E edge0 = hds.getEdge(0);
		if (edge0.getLeftFace() != null) {
			edgeStack.push(edge0);
		}
		if (edge0.getRightFace() != null) {
			edgeStack.push(edge0.getOppositeEdge());
		}
		int count = 0;
		while (!edgeStack.isEmpty()){
			E edge = edgeStack.pop();
			labelFaceOf(edge, a);
			for (E e : HalfEdgeUtils.boundaryEdges(edge.getLeftFace())){
				if (pendingEdges.contains(e)){
					if (e.getRightFace() != null) {
						edgeStack.push(e.getOppositeEdge());
					}
					pendingEdges.remove(e);
				}
			}
			count++;
		}
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void dualize(HDS hds, AdapterSet a) {
		HashMap<V, double[]> newCoordsMap = new HashMap<V, double[]>();
		HashSet<V> readyVertices = new HashSet<V>();
		LinkedList<V> vertexQueue = new LinkedList<V>();
		V v0 = hds.getVertex(0);
		vertexQueue.offer(v0);
		//vertex 0 in 0.0;
		double[] v0Pos = a.getD(Position3d.class, v0);
		newCoordsMap.put(v0, v0Pos.clone());
		while (!vertexQueue.isEmpty()){
			V v = vertexQueue.poll();
			double[] startCoord = newCoordsMap.get(v);
			List<E> star = HalfEdgeUtils.incomingEdges(v);
			for (E e : star){
				V v2 = e.getStartVertex();
				if (readyVertices.contains(v2))
					continue;
				else {
					vertexQueue.offer(v2);
					readyVertices.add(v2);
				}
				double[] pv = a.getD(Position3d.class, v);
				double[] pv2 = a.getD(Position3d.class, v2);
				double[] vec = Rn.subtract(null, pv2, pv);
				//double factor = Rn.euclideanDistanceSquared(pv, pv2);
				E de = e.getLeftFace() != null ? e : e.getOppositeEdge();
				double[] r12 = decomposeEdgeLength(de, a);
				double factor = r12[0] * r12[1];
				boolean edgeSign = a.get(EdgeSign.class, e, Boolean.class);
				double scale = (edgeSign ? -1 : 1) / factor;
				vec[0] *= scale;
				vec[1] *= scale;
				vec[2] *= scale;
				Rn.add(vec, vec, startCoord);
				newCoordsMap.put(v2, vec);
			}
		}
		for (V v : hds.getVertices()){
			double[] p = newCoordsMap.get(v);
			if (p != null) {
				a.set(Position.class, v, p);
			}
		}	
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void transfom(HDS hds, AdapterSet a) {
		EdgeSignAdapter esa = new EdgeSignAdapter();
		a.add(esa);
		if (useCentralExtensionChecker.isSelected()) {
			Map<F, V> oldFnewVMap = new HashMap<F, V>();
			Map<E, V> oldEnewVMap = new HashMap<E, V>();
			Map<V, V> oldVnewVMap = new HashMap<V, V>();
			HDS ce = ceSubdivider.subdivide(hds, a, oldFnewVMap, oldEnewVMap, oldVnewVMap);
			createEdgeLabels(ce, a);
			dualize(ce, a);
			for (V v : hds.getVertices()) {
				V newV = oldVnewVMap.get(v);
				a.set(Position.class, v, a.getD(Position3d.class, newV));
			}
		} else {
			createEdgeLabels(hds, a);
			dualize(hds, a);
			hif.addLayerAdapter(esa, false);
		}
	}
	
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		transfom(hds, a);
		hif.update();
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.DDG;
	}

	@Override
	public String getAlgorithmName() {
		return "Christoffel Transfom";
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		ceSubdivider = c.getPlugin(CentralExtensionSubdivision.class);
		hif = c.getPlugin(HalfedgeInterface.class);
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
	
	public static < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] decomposeEdgeLength(E e, AdapterSet a) {
		F f = e.getLeftFace();
		double[] c = ChristoffelTransfom.getIncircle(f, a);
		double r = c[3];
		double[] p0 = a.getD(Position3d.class, e.getStartVertex());
		double[] p1 = a.getD(Position3d.class, e.getTargetVertex());
		double[] p2 = a.getD(Position3d.class, e.getPreviousEdge().getStartVertex());
		double[] p3 = a.getD(Position3d.class, e.getNextEdge().getTargetVertex());
		double[] vec1 = Rn.subtract(null, p1, p0);
		double[] vec2 = Rn.subtract(null, p2, p0);
		double alpha = Rn.euclideanAngle(vec1, vec2);
		double r1 = r / Math.tan(alpha / 2);
		
		vec1 = Rn.subtract(null, p0, p1);
		vec2 = Rn.subtract(null, p3, p1);
		alpha = Rn.euclideanAngle(vec1, vec2);
		double r2 = r / Math.tan(alpha / 2);
		return new double[] {r1, r2};
	}
	

}
