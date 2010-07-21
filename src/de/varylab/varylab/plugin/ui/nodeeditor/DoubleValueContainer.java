package de.varylab.varylab.plugin.ui.nodeeditor;

import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;

public class DoubleValueContainer {
	
	protected List<Node<?,?,?>>
		editedNodes = new LinkedList<Node<?,?,?>>();
	protected Adapter<Object>
		adapter = null;
	protected AdapterSet
		auxAdapters = new AdapterSet();
	protected double
		value = 0.0;
	
	public DoubleValueContainer(double value, List<Node<?,?,?>> nodes, Adapter<Object> adapter, AdapterSet auxAdapters) {
		this.editedNodes = nodes;
		this.value = value;
		this.adapter = adapter;
		this.auxAdapters = auxAdapters;
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
		for (Node<?,?,?> n : editedNodes) {
			adapter.set(n, value, auxAdapters);
		}
	}
	
}