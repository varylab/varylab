package de.varylab.varylab.plugin.editor;

import java.awt.event.InputEvent;
import java.util.Iterator;
import java.util.Set;

import javax.swing.KeyStroke;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class EdgeCreatorPlugin extends AlgorithmPlugin {

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		Set<V> selectedVerts = hif.getSelection().getVertices(hds);
		
		if(selectedVerts.size() ==  2) {
			Iterator<V> it = selectedVerts.iterator();
			V v1 = it.next(), v2 = it.next();
			E e1 = hds.addNewEdge(),
		      e2 = hds.addNewEdge();
			e1.setIsPositive(true);
			e1.linkOppositeEdge(e2);
			e1.linkNextEdge(e2);
			e2.linkNextEdge(e1);
			e1.setTargetVertex(v1);
			e2.setTargetVertex(v2);
		}
		hif.update();
	}

	@Override
	public String getAlgorithmName() {
		return "Create Edge";
	}
	
	@Override
	public KeyStroke getKeyboardShortcut() {
		return KeyStroke.getKeyStroke('E', InputEvent.META_DOWN_MASK);
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Editing;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Edge Creator", "Thilo Roerig");
	}

}
