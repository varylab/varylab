package de.varylab.varylab.plugin.selection;

import java.util.HashSet;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.SelectionInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.utilities.SelectionUtility;

public class GeodesicSelection extends AlgorithmPlugin {

	private final Integer
		CHANNEL_GEODESIC_EDGES = 234234;
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Geodesic";
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
		Set<E> all = new HashSet<E>();
		for(E e : hif.getSelection().getEdges(hds)) {
			all.addAll(SelectionUtility.selectGeodesic(e));
		}
		hes.addAll(all, CHANNEL_GEODESIC_EDGES);
		hif.addSelection(hes);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("geoSel.png",16,16);
		return info;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		SelectionInterface sif = c.getPlugin(SelectionInterface.class);
		sif.registerChannelName(CHANNEL_GEODESIC_EDGES, "Geodesic Edges");
	}
	
}
