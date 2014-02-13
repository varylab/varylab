package de.varylab.varylab.plugin.hyperbolicnets;

import java.util.HashMap;
import java.util.HashSet;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;

public class StripDecomposition<
	E extends Edge<?,E,F>,
	F extends Face<?,E,F>,
	HDS extends HalfEdgeDataStructure<?, E, F>
> {

	private HashMap<E, Strip<E,F>> 
		stripMap = new HashMap<E, Strip<E,F>>();
	
	private HashSet<Strip<E,F>>
		strips = new HashSet<Strip<E,F>>();
	
	public StripDecomposition(HDS hds) {
		HashSet<E> edgesDone = new HashSet<E>();
		for(E e: hds.getPositiveEdges()) {
			if(!edgesDone.contains(e)) {
				Strip<E,F> eStrip = new Strip<E, F>(e);
				strips.add(eStrip);
				edgesDone.addAll(eStrip.getEdges());
				for(E se: eStrip.getEdges()) {
					stripMap.put(se, eStrip);
				}
			}
		}
	}
	
	public Strip<E,F> getStrip(E e) {
		return stripMap.get(e);
	}
	
	public int getNumberOfStrips() {
		return strips.size();
	}
	
	public HashSet<Strip<E, F>> getStrips() {
		return strips;
	}
}
