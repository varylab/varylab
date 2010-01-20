package de.varylab.varylab.plugin;

import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.jrworkspace.plugin.Plugin;

public abstract class VisualizerPlugin extends Plugin {

	public abstract Adapter getVisualizerAdapter();
	
	public abstract AdapterType getType();
	
}
