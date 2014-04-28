package de.varylab.varylab.plugin.selection;

import static de.jtem.halfedge.util.HalfEdgeUtils.isBoundaryEdge;
import static de.jtem.halfedge.util.HalfEdgeUtils.isBoundaryVertex;
import static de.jtem.halfedgetools.util.HalfEdgeUtilsExtra.get1Ring;
import static javax.swing.JOptionPane.YES_NO_OPTION;

import java.awt.EventQueue;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.swing.JOptionPane;

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

public class LatticeSelection extends AlgorithmPlugin {

	private final Integer
		CHANNEL_LATTICE_VERTICES = 1822344;
	private boolean
		ignoreBoundary = false;
	
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
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) throws Exception {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				int result = JOptionPane.showConfirmDialog(
					getOptionParent(), 
					"Ignore Boundary Vertices?", 
					"Lattice Selection", 
					YES_NO_OPTION
				);
				ignoreBoundary = result == JOptionPane.YES_OPTION;
			}
		};
		EventQueue.invokeAndWait(r);
		Selection hes = hif.getSelection();
		Set<V> all = new HashSet<V>();
		for(V v : hif.getSelection().getVertices(hds)) {
			all.addAll(selectSublattice(v, ignoreBoundary));
		}
		hes.addAll(all, CHANNEL_LATTICE_VERTICES);
		hif.addSelection(hes);
	}
	
	
	private  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> Set<V> selectSublattice(V v, boolean ignoreBoundary) {
		Set<V> sl = new HashSet<V>();
		Stack<V> queue = new Stack<V>();
		queue.add(v);
		while(!queue.isEmpty()) {
			V av = queue.pop();
			if(sl.contains(av)) {
				continue;
			}
			for(E e : get1Ring(av)) {
				if(isBoundaryEdge(e)) {
					continue;
				}
				V tv = e.getOppositeEdge().getNextEdge().getTargetVertex();
				if (ignoreBoundary && isBoundaryVertex(tv)) {
					continue;
				}
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
