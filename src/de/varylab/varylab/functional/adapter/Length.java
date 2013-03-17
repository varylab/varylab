package de.varylab.varylab.functional.adapter;

import de.jtem.halfedge.Edge;

public interface Length<E extends Edge<?, E, ?>> {
	public Double getTargetLength(E e);
}