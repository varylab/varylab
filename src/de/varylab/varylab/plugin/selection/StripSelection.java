package de.varylab.varylab.plugin.selection;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
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
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hif) throws CalculatorException {
		HalfedgeSelection hes = hif.getSelection();
		HashSet<E> edges = new HashSet<E>(hes.getEdges(hds));
		Set<F> faces = hes.getFaces(hds);
		LinkedList<F> stripFaces = new LinkedList<F>();
		LinkedList<E> stripEdges = new LinkedList<E>();
		for(F f:faces) {
			for(E e : HalfEdgeUtils.boundaryEdges(f)) {
				stripFaces.clear();
				stripEdges.clear();
				if(edges.contains(e)) {
					SelectionUtility.generateStrip1D(f, e, stripFaces, stripEdges);
					hes.addAll(stripFaces);
					hes.addAll(stripEdges);
				}
			}
		}
		hif.setSelection(hes);
	}
	
}
