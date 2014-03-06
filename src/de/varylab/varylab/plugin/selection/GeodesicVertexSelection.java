package de.varylab.varylab.plugin.selection;

import java.util.HashSet;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.utilities.SelectionUtility;

public class GeodesicVertexSelection extends AlgorithmPlugin {

	@Override
	public <
	V extends Vertex<V, E, F>, 
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>, 
	HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		HalfedgeSelection hes = hif.getSelection();
		Set<V> all = new HashSet<V>();
		for(E e : hif.getSelection().getEdges(hds)) {
			Set<E> edges = new HashSet<E>();
			edges.addAll(SelectionUtility.selectGeodesic(e));
			for (E edge : edges) {
				V v = edge.getStartVertex();
				if(!HalfEdgeUtils.isBoundaryVertex(v)){
					all.add(v);
				}
			}
		}
		hes.addAll(all);
		hif.setSelection(hes);
	}
	
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Geodesic Vertex";
	}
	
	public double getPriority() {
		return 1.0;
	}
	
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("geoSel.png",16,16);
		return info;
	}

}
