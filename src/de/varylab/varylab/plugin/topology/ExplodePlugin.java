package de.varylab.varylab.plugin.topology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jreality.math.Pn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class ExplodePlugin extends AlgorithmPlugin{

	private class ReferencePointAdapter extends AbstractAdapter<double[]> {

		Map<Vertex<?,?,?>, double[]> referencePointMap = new HashMap<>();
		
		public ReferencePointAdapter() {
			super(double[].class, true, true);
		}

		@Override
		public boolean canAccept(Class nodeClass) {
			return Vertex.class.isAssignableFrom(nodeClass);
		}

		@Override
		public <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>
		> void setV(V v, double[] value, AdapterSet a) {
			referencePointMap.put(v,value);
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>
		> double[] getV(V v, AdapterSet a) {
			return referencePointMap.get(v);
		}
	}
	
	private class ReferencePointDistanceAdapter extends AbstractAdapter<Double> {
		public ReferencePointDistanceAdapter() {
			super(Double.class, true, true);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Vertex.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>
		> Double getV(V v, AdapterSet a) {
			ReferencePointAdapter rpa = a.query(ReferencePointAdapter.class);
			if(rpa == null) {
				return null;
			} else {
				return Pn.distanceBetween(a.getD(Position4d.class,v), rpa.getV(v, a), Pn.EUCLIDEAN);
			}
		}
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Topology;
	}

	@Override
	public String getAlgorithmName() {
		return "Explode";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		ReferencePointAdapter rpa = new ReferencePointAdapter();
		HDS newHDS = hi.createEmpty(hds);
		HalfedgeLayer layer = new HalfedgeLayer(hi);
		layer.setName(hi.getActiveLayer().getName() + "--explode");
		for(F face : hds.getFaces()) {
			F newFace = addNGon(newHDS, HalfEdgeUtils.boundaryEdges(face).size());
			copyCoords(face, a, newFace, layer.getEffectiveAdapters(), rpa);
		}
		
		layer.set(newHDS);
		hi.addLayer(layer);
		hi.activateLayer(layer);
		hi.addLayerAdapter(rpa, true);
		hi.addLayerAdapter(new ReferencePointDistanceAdapter(), true);
	}
	
	private static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void copyCoords(F oldFace, AdapterSet oldAs,F newFace, AdapterSet newAs, ReferencePointAdapter rpa) {
		E ne = newFace.getBoundaryEdge();
		for(E e : HalfEdgeUtils.boundaryEdges(oldFace)) {
			double[] position = oldAs.getD(Position4d.class, e.getTargetVertex());
			V newVertex = ne.getTargetVertex();
			newAs.set(Position.class, newVertex, position);
			rpa.setV(newVertex, position, null);
			ne = ne.getNextEdge();
		}
	}
	
	private static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> F addNGon(HDS hds, int n ) {
		F newFace = hds.addNewFace();
		List<V> newVertices = hds.addNewVertices(n);
		List<E> newEdges = hds.addNewEdges(2*n);
		for (int i = 0; i < n; i++) {
			newEdges.get(i).setLeftFace(newFace);
			
			newEdges.get(i).linkOppositeEdge(newEdges.get(i+n));
			
			newEdges.get(i).linkNextEdge(newEdges.get((i+1)%n));
			newEdges.get(i).linkPreviousEdge(newEdges.get((i-1+n)%n));
			
			newEdges.get(i+n).linkOppositeEdge(newEdges.get(i));
			
			newEdges.get(i+n).linkNextEdge(newEdges.get((i-1+n)%n + n));
			newEdges.get(i+n).linkPreviousEdge(newEdges.get((i+1)%n + n));
			
			newEdges.get(i).setTargetVertex(newVertices.get((i+1)%n));
			newEdges.get(n+i).setTargetVertex(newVertices.get(i));
		}
		return newFace;
	}
}
