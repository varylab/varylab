package de.varylab.varylab.plugin.subdivision;

import java.util.HashSet;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.ui.image.ImageHook;
import de.varylab.varylab.utilities.SelectionUtility;

public class RemoveGeodesicPlugin extends HalfedgeAlgorithmPlugin {

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(
		HDS hds, 
		CalculatorSet c, 
		HalfedgeInterface hcp) throws CalculatorException 
	{
		HashSet<E> edges = new HashSet<E>(hcp.getSelection().getEdges(hds));
		HashSet<E> removedEdges = new HashSet<E>();
		HashSet<V> removedVertices = new HashSet<V>();
		for(E e : edges) {
			if(removedEdges.contains(e)) continue;
			
			Set<E> geodesic = SelectionUtility.selectGeodesic(e, hds);
			removedEdges.addAll(geodesic);
			for(E ge : geodesic) {
				if(ge.isPositive()) {
					removedVertices.add(ge.getStartVertex());
					removedVertices.add(ge.getTargetVertex());
					TopologyAlgorithms.removeEdgeFill(ge);
				}
			}
		}
		for(V rv : removedVertices) {
			int neighs = HalfEdgeUtils.neighboringVertices(rv).size();
			if(neighs == 0) {
				hds.removeVertex(rv);
			}
			if(neighs == 2 && !HalfEdgeUtils.isBoundaryVertex(rv)) {
				E ie = rv.getIncomingEdge();
				TopologyAlgorithms.collapseEdge(ie, ie.getStartVertex());
			}
		}
		hcp.set(hds);
	}

	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Remove Geodesic", "Thilo Roerig");
		info.icon = ImageHook.getIcon("removeGeodesic.png", 16, 16);
		return info;
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Custom;
	}

	@Override
	public String getAlgorithmName() {
		return "Remove Geodesic";
	}
}
