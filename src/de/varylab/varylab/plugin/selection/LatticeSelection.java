package de.varylab.varylab.plugin.selection;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.SelectionInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.icon.ImageHook;

public class LatticeSelection extends AlgorithmPlugin {

	private final Integer
		CHANNEL_LATTICE_VERTICES = 182742;
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Lattice";
	}
	
	@Override
	public double getPriority() {
		return 1;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		Selection hes = hif.getSelection();
		Set<V> all = new HashSet<V>();
		for(V v : hif.getSelection().getVertices(hds)) {
			all.addAll(selectSublattice(v));
		}
		hes.addAll(all, CHANNEL_LATTICE_VERTICES);
		hif.addSelection(hes);
	}
	
	
	private  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> Set<V> selectSublattice(V v) {
		Set<V> sl = new HashSet<V>();
		Stack<V> queue = new Stack<V>();
		queue.add(v);
		while(!queue.isEmpty()) {
			V av = queue.pop();
			if(sl.contains(av)) {
				continue;
			}
			for(E e : HalfEdgeUtilsExtra.get1Ring(av)) {
				if(HalfEdgeUtils.isBoundaryEdge(e)) {
					continue;
				}
				V tv = e.getOppositeEdge().getNextEdge().getTargetVertex();
				queue.add(tv);
			}
			sl.add(av);
		}
		return sl;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("latticeSel.png",16,16);
		return info;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		SelectionInterface sif = c.getPlugin(SelectionInterface.class);
		sif.registerChannelName(CHANNEL_LATTICE_VERTICES, "Lattice Vertices");
	}
	
}
