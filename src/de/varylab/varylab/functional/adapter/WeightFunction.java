package de.varylab.varylab.functional.adapter;

import de.jtem.halfedge.Edge;

public interface WeightFunction<E extends Edge<?, E, ?>> {
	public Double getWeight(E e);
}