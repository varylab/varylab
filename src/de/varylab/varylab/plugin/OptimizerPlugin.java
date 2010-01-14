package de.varylab.varylab.plugin;

import javax.swing.JPanel;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public abstract class OptimizerPlugin extends Plugin {

	public JPanel getOptionPanel() {
		return null;
	}
	
	public abstract Functional<VVertex, VEdge, VFace> createFunctional(VHDS hds);
	
	public abstract String getName();
	
	@Override
	public String toString() {
		return getName();
	}
	
}
