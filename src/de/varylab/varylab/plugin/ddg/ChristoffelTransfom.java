package de.varylab.varylab.plugin.ddg;

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

import de.jreality.math.Rn;
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
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class ChristoffelTransfom extends AlgorithmPlugin {

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
		newCoordsMap.put(v0, new double[3]);
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
				double norm2 = Rn.euclideanDistanceSquared(pv, pv2);
				boolean edgeSign = a.get(EdgeSign.class, e, Boolean.class);
				double scale = (edgeSign ? -1 : 1) / norm2;
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
		a.add(new EdgeSignAdapter());
		createEdgeLabels(hds, a);
		dualize(hds, a);
	}
	
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		transfom(hds, a);
		hif.update();
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Geometry;
	}

	@Override
	public String getAlgorithmName() {
		return "Christoffel Transfom";
	}

}
