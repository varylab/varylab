package de.varylab.varylab.plugin.ddg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;

@EdgeSign
public class EdgeSignAdapter extends AbstractAdapter<Boolean> {
	
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
		if(signMap.get(e) == null) {
			createEdgeLabels(e.getHalfEdgeDataStructure(), a);
		}
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

	@Override
	public void update() {
		super.update();
		signMap.clear();
	}
	
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  void labelFaceOf(E edge, AdapterSet a) {
		E actEdge = edge.getNextEdge();
		while (actEdge != edge){
			E prev = actEdge.getPreviousEdge();
			boolean prevLabel = signMap.get(prev);
			signMap.put(actEdge, !prevLabel);
			signMap.put(actEdge.getOppositeEdge(), !prevLabel);
			actEdge = actEdge.getNextEdge();
		}
		boolean edgeLabel = a.get(EdgeSign.class, edge, Boolean.class);
		boolean prevLabel = a.get(EdgeSign.class, edge.getPreviousEdge(), Boolean.class);
		if (prevLabel == edgeLabel){
			System.err.println("could not label face " + edge.getLeftFace() + " correctly, continuing...");
		}
	}


	private <
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
			signMap.put(edge0,true);
		}
		if (edge0.getRightFace() != null) {
			edgeStack.push(edge0.getOppositeEdge());
			signMap.put(edge0.getOppositeEdge(),true);
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
}