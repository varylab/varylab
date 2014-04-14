package de.varylab.varylab.plugin.topology;

import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.icon.ImageHook;

public class CollapseToNeighborPlugin extends AlgorithmPlugin{

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
		return "Collapse To Neighbor";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		Selection selection = hi.getSelection();
		Set<V> vertices = selection.getVertices(hds);
		
		for(V v : vertices) {
			E inEdge = null;
			for(E e : HalfEdgeUtils.incomingEdges(v)) {
				if(HalfEdgeUtils.isBoundaryEdge(e)) {
					inEdge = e;
				}
			}
			if(inEdge != null) {
				TopologyAlgorithms.collapseEdge(inEdge,inEdge.getStartVertex());
			}
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
