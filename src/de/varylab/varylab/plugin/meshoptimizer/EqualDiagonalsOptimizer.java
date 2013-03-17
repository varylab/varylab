package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.EqualDiagonalsFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class EqualDiagonalsOptimizer extends VarylabOptimizerPlugin {

	private HalfedgeInterface
		hi = null;
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		AdapterSet aSet = hi.getAdapters();
		return new EqualDiagonalsFunctional<VVertex, VEdge, VFace>(aSet);
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hi = c.getPlugin(HalfedgeInterface.class);
	}
	
	@Override
	public String getName() {
		return "Equal Diagonals";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Equal Diagonals Optimizer", "Stefan Sechelmann");
		return info;
	}


}
