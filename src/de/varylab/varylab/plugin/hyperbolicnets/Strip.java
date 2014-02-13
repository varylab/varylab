package de.varylab.varylab.plugin.hyperbolicnets;

import java.util.LinkedList;
import java.util.ListIterator;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.varylab.varylab.utilities.SelectionUtility;

public class Strip<E extends Edge<?,E,F>, F extends Face<?,E,F>> {

	LinkedList<F> faces = new LinkedList<F>();
	LinkedList<E> edges = new LinkedList<E>();
	
	// TODO: Fix for closed manifolds.
	
	public Strip(E e) {
		SelectionUtility.generateStrip1D(e, faces, edges);
	}
	
	public ListIterator<F> getIterator(F f) {
		ListIterator<F> it = faces.listIterator();
		while(it.hasNext() && (it.next() != f)) {
			F next = it.next();
			if(next == f) {
				it.previous();
				break;
			}
		}
		return it;
	}
	
	public ListIterator<F> getIterator() {
		return faces.listIterator();
	}
	
	public E getLeftEdge(F f) {
		int i = faces.indexOf(f);
		return edges.get(2*i + 1);
	}
	
	public E getRightEdge(F f) {
		int i = faces.indexOf(f);
		return edges.get(2*(i + 1));
	}

	public E getTopEdge(F f) {
		E e = getRightEdge(f);
		return e.getNextEdge();
	}
	
	public E getBottomEdge(F f) {
		E e = getLeftEdge(f);
		return e.getNextEdge();
	}
	
	public LinkedList<F> getFaces() {
		return faces;
	}
	
	public LinkedList<E> getEdges() {
		return edges;
	}
	
	@Override
	public String toString() {
		String str = new String();
		str += edges.getFirst() + ", ";
		for(int i = 0; i < faces.size(); ++i) {
			F f = faces.get(i);
			str +=
				getLeftEdge(f) + "|" +
				f + "|" +
				getRightEdge(f) + ", ";
		}
		str += edges.getLast();
		return str;
	}
}
