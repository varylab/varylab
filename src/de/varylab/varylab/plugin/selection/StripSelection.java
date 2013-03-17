package de.varylab.varylab.plugin.selection;

import java.util.LinkedList;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.image.ImageHook;
import de.varylab.varylab.utilities.SelectionUtility;

public class StripSelection extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Strip";
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
		HalfedgeSelection hes = hif.getSelection();
		LinkedList<F> stripFaces = new LinkedList<F>();
		LinkedList<E> stripEdges = new LinkedList<E>();
		for(E e : hes.getEdges(hds)) {
			stripFaces.clear();
			stripEdges.clear();
			SelectionUtility.generateStrip1D(e, stripFaces, stripEdges);
			hes.addAll(stripFaces);
			hes.addAll(stripEdges);
		}
		hif.setSelection(hes);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("stripSel.png",16,16);
		return info;
	}
}
