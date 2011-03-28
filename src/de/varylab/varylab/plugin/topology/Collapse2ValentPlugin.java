package de.varylab.varylab.plugin.topology;

import static de.jtem.halfedge.util.HalfEdgeUtils.incomingEdges;

import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class Collapse2ValentPlugin extends AlgorithmPlugin{

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.TextureRemeshing;
	}

	@Override
	public double getPriority() {
		return 10.0;
	}
	
	@Override
	public String getAlgorithmName() {
		return "Collapse 1,2-valent Vertices";
	}
	
	
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> List<V> getVerticesByValence(HDS hds, int valence) {
		List<V> vList = new LinkedList<V>();
		for(V v : hds.getVertices()) {
			List<E> in = incomingEdges(v);
			if (in.size() == valence) {
				vList.add(v);
			}
		}
		return vList;
	}
	

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		// delete all vertices of valence one
		List<V> vList = getVerticesByValence(hds, 1);
		while (!vList.isEmpty()) {
			for (V v : vList) {
				TopologyAlgorithms.removeVertexFill(v);
			}
			vList = getVerticesByValence(hds, 1);
		}
		// collapse vertices of valence 2
		vList = getVerticesByValence(hds, 2);
		List<E> collapseList = new LinkedList<E>();
		for(V v : vList) {
			List<E> in = incomingEdges(v);
			if (in.size() == 2) {
				collapseList.add(v.getIncomingEdge());
			}
		}
		for (E e : collapseList) {
			TopologyAlgorithms.collapseEdge(e, e.getStartVertex());
		}
		hi.set(hds);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("collapseToNeighbor.png",16,16);
		return info;
	}
}
