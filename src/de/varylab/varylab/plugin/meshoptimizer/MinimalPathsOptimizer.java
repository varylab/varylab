package de.varylab.varylab.plugin.meshoptimizer;

import de.jreality.plugin.JRViewerUtility;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.MinimalPathsFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class MinimalPathsOptimizer extends VarylabOptimizerPlugin { 
	
	private HalfedgeInterface hif;

	private MinimalPathsFunctional<VVertex, VEdge, VFace>
		functional = new MinimalPathsFunctional<VVertex, VEdge, VFace>();
	
	public MinimalPathsOptimizer() {
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}

	@Override
	public String getName() {
		return "Minimal Paths";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Minimal Paths Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("minimalPaths.png",16,16);
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		c.getPlugin(HalfedgeInterface.class);
		JRViewerUtility.getContentPlugin(c);
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
	}
}
