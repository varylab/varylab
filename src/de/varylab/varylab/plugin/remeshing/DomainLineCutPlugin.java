package de.varylab.varylab.plugin.remeshing;

import java.awt.Window;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.selection.Selection;

public class DomainLineCutPlugin extends AlgorithmPlugin {

	@Override
	public String getCategory() {
		return "Texture";
	}
	
	@Override
	public String getAlgorithmName() {
		return "Cut texture line";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		
		Set<V> vSet = hif.getSelection().getVertices(hds);
		if (vSet.isEmpty()) {
			Window w = SwingUtilities.getWindowAncestor(hif.getShrinkPanel());
			JOptionPane.showMessageDialog(w, "Please select vertices:\n - one vertex to define direction cut\n - two to define cut along line");
			return;
		}
		
		
		if(vSet.size() < 2) {
			Window w = SwingUtilities.getWindowAncestor(hif.getShrinkPanel());
			JOptionPane.showMessageDialog(w, "Please select vertices:\n - one vertex to define direction cut\n - two to define cut along line");
			return;
		}
		
		Selection cutSelection = TextureUtility.cutLine(hds, vSet, hif.getAdapters());

		hif.setSelection(cutSelection);
		hif.update();
	}
}
