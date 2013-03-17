package de.varylab.varylab.plugin.nodeeditor;

import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;

public class BooleanValueContainer {
	
	protected List<Node<?,?,?>>
		editedNodes = new LinkedList<Node<?,?,?>>();
	protected Adapter<Object>
		adapter = null;
	protected AdapterSet
		auxAdapters = new AdapterSet();
	protected boolean
		value = false;
	
	public BooleanValueContainer(boolean value, List<Node<?,?,?>> nodes, Adapter<Object> adapter, AdapterSet auxAdapters) {
		this.editedNodes = nodes;
		this.value = value;
		this.adapter = adapter;
		this.auxAdapters = auxAdapters;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public void setValue(boolean value) {
		this.value = value;
		for (Node<?,?,?> n : editedNodes) {
			adapter.set(n, value, auxAdapters);
		}
	}
	
}