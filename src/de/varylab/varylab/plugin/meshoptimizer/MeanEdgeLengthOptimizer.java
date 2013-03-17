package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.ConstantLengthAdapter;
import de.varylab.varylab.hds.adapter.ConstantWeight;
import de.varylab.varylab.math.functional.MeanEdgeLengthFunctional;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;
import de.varylab.varylab.plugin.image.ImageHook;

public class MeanEdgeLengthOptimizer extends VarylabOptimizerPlugin {

	private JPanel
		panel = new JPanel();
	private JCheckBox
		ignoreBoundaryChecker = new JCheckBox("Ignore Boundary");
	
	public MeanEdgeLengthOptimizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		panel.add(ignoreBoundaryChecker, c);
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		double l = 0.0;
		for (VEdge e : hds.getPositiveEdges()) {
			double[] s = e.getStartVertex().P;
			double[] t = e.getTargetVertex().P;
			l += Rn.euclideanDistance(s, t);
		}
		l /= hds.numEdges() / 2.0;
		
		ConstantLengthAdapter la = new ConstantLengthAdapter(l);
		ConstantWeight wa = new ConstantWeight(1.0, ignoreBoundaryChecker.isSelected());
		return new MeanEdgeLengthFunctional<VVertex, VEdge, VFace>(la, wa);
	}
	
	@Override
	public String getName() {
		return "Mean Edge Length";
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Mean Edge Length Optimizer", "Stefan Sechelmann");
		info.icon = ImageHook.getIcon("edgelength.png");
		return info;
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
}
