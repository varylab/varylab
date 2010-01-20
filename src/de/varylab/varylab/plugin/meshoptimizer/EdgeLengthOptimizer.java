package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthFunctional;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.Length;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.WeightFunction;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.ui.image.ImageHook;

public class EdgeLengthOptimizer extends OptimizerPlugin {

	private JPanel
		panel = new JPanel();
	private JCheckBox
		ignoreBoundaryChecker = new JCheckBox("Ignore Boundary");
	
	public EdgeLengthOptimizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		panel.add(ignoreBoundaryChecker, c);
	}
	
	public static class LengthAdapter implements Length<VEdge> {

		private double 
			length = 0.0;
		
		public LengthAdapter(double l0) {
			this.length = l0;
		}
		
		@Override
		public Double getTargetLength(VEdge e) {
			return length;
		}
		
		public void setL0(double l0) {
			this.length = l0;
		}
		
	}
	
	public static class ConstantWeight implements WeightFunction<VEdge> {

		public double 
			w = 1.0;
		public boolean 
			ignoreBoundary = false;
		
		public ConstantWeight(double w, boolean ignoreBoundary) {
			this.w = w;
		}
		
		@Override
		public Double getWeight(VEdge e) {
			if (HalfEdgeUtils.isBoundaryEdge(e) && ignoreBoundary) {
				return 0.0;
			} else {
				return w;
			}
		}
		
	}
	
	
	@Override
	public Functional<VVertex, VEdge, VFace> createFunctional(VHDS hds) {
		double l = 0.0;
		for (VEdge e : hds.getPositiveEdges()) {
			double[] s = e.getStartVertex().position;
			double[] t = e.getTargetVertex().position;
			l += Rn.euclideanDistance(s, t);
		}
		l /= hds.numEdges() / 2.0;
		
		LengthAdapter la = new LengthAdapter(l);
		ConstantWeight wa = new ConstantWeight(1.0, ignoreBoundaryChecker.isSelected());
		return new EdgeLengthFunctional<VVertex, VEdge, VFace>(la, wa);
	}
	
	@Override
	public String getName() {
		return "Edge Length Equalizer";
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Edge Length Optimizer", "Stefan Sechelmann");
		info.icon = ImageHook.getIcon("edgelength.png");
		return info;
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
}
