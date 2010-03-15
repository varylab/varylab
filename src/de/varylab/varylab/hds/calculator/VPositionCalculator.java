package de.varylab.varylab.hds.calculator;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.varylab.varylab.hds.VVertex;

public class VPositionCalculator implements VertexPositionCalculator {

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return VVertex.class == nodeClass;
	}
	
	@Override
	public double getPriority() {
		return 1;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(V v) {
		return ((VVertex)v).position;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> void set(V v, double[] c) {
		((VVertex)v).position = c;
	}

}
