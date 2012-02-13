package de.varylab.varylab.utilities;

import java.util.Comparator;

import de.jtem.halfedge.Node;

public class NodeIndexComparator <T extends Node<?,?,?>> implements Comparator<T>{

	@Override
	public int compare(T o1, T o2) {
		return o1.getIndex() - o2.getIndex();
	}
	
}