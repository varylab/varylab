package de.varylab.varylab.plugin.meshoptimizer;

import javax.swing.JPanel;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.RegularNgonsFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class NgonRegularizer extends VarylabOptimizerPlugin { 
	
	private RegularNgonsFunctional<VVertex, VEdge, VFace>
		functional = new RegularNgonsFunctional<VVertex, VEdge, VFace>();
	
	private JPanel
		panel = new JPanel();
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		functional.setSizes(6);
		return functional;
	}

	@Override
	public String getName() {
		return "Regular n-gons";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("N-gon regularizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("ngons.png",16,16);
		return info;
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	};

}
