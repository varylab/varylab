package de.varylab.varylab.plugin;

import javax.swing.JPanel;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.meshoptimizer.OptimizationManager;

public abstract class OptimizerPlugin extends Plugin {

	protected OptimizationManager
		manager = null;
	
	public JPanel getOptionPanel() {
		return null;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		manager = c.getPlugin(OptimizationManager.class);
		manager.addOptimizerPlugin(this);
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		manager.removeOptimizerPlugin(this);
	}
	
	public abstract Functional<VVertex, VEdge, VFace> createFunctional(VHDS hds);
	
	public abstract String getName();
	
	@Override
	public String toString() {
		return getName();
	}
	
}
