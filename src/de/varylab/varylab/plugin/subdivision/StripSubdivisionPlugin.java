package de.varylab.varylab.plugin.subdivision;

import java.awt.event.InputEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.KeyStroke;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter4d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.remeshing.RemeshingUtility;
import de.varylab.varylab.utilities.SelectionUtility;

public class StripSubdivisionPlugin extends AlgorithmPlugin {

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(
		HDS hds, 
		AdapterSet a, 
		HalfedgeInterface hcp)  
	{
		List<E> edges = hcp.getSelection().getEdges(hds);
		for(E e : edges) {
			if(e.isPositive()) {
				subdivideStrip1D(e, a);
			}
		}
		hcp.set(hds);	
	}

	private  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> void subdivideStrip1D(E fe, AdapterSet a)
	{
		LinkedList<F> stripFaces = new LinkedList<F>();
		LinkedList<E> stripEdges = new LinkedList<E>();
		LinkedList<V> stripVertices = new LinkedList<V>();
		
		SelectionUtility.generateStrip1D(fe, stripFaces, stripEdges);
		
		for(E se: stripEdges) {
			if(se.isPositive()) {
				double[] b = a.get(BaryCenter4d.class, se, double[].class); 
				V v = TopologyAlgorithms.splitEdge(se);
				a.set(Position.class, v, b);
				stripVertices.addLast(v);
			}
		}
		for(int i = 0; i < stripFaces.size(); ++i) {
			RemeshingUtility.splitFaceAt(
					stripFaces.get(i),
					stripVertices.get(i),
					stripVertices.get(i+1));
		}
	}
	
	@Override
	public KeyStroke getKeyboardShortcut() {
		return KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Strip Subdivider", "Thilo Roerig");
		info.icon = ImageHook.getIcon("stripSubd.png", 16, 16);
		return info;
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Custom;
	}

	@Override
	public String getAlgorithmName() {
		return "Strip Subdivision";
	}

}
