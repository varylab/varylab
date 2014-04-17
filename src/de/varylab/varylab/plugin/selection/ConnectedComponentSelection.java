package de.varylab.varylab.plugin.selection;

import java.util.List;
import java.util.TreeSet;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.TypedSelection;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class ConnectedComponentSelection extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Connected Component";
	}
	
	@Override
	public double getPriority() {
		return 1.0;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		Selection hes = hif.getSelection();
		TypedSelection<V> vertices = hes.getVertices(hds);
		TreeSet<V> queue = new TreeSet<>(vertices);
		while(!queue.isEmpty()) {
			V v = queue.pollFirst();
			List<V> neighs = HalfEdgeUtils.neighboringVertices(v);
			for(V n : neighs) {
				if(!hes.getVertices(hds).contains(n)) {
					queue.add(n);
				}
			}
			hes.add(v);
		}
		hif.setSelection(hes);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
//		info.icon = ImageHook.getIcon("geoSel.png",16,16);
		return info;
	}
}
