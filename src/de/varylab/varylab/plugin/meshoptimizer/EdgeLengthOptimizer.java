package de.varylab.varylab.plugin.meshoptimizer;

import de.jreality.math.Rn;
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

	public EdgeLengthOptimizer() {
	}
	
	public static class LengthAdapter implements Length {

		private double 
			length = 0.0;
		
		public LengthAdapter(double l0) {
			this.length = l0;
		}
		
		@Override
		public Double getL0() {
			return length;
		}
		
		public void setL0(double l0) {
			this.length = l0;
		}
		
	}
	
	public static class ConstantWeight implements WeightFunction {

		public double 
			w = 1.0;
		
		public ConstantWeight(double w) {
			this.w = w;
		}
		
		@Override
		public Double evalWeight(Double l) {
			return w;
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
		
		return new EdgeLengthFunctional<VVertex, VEdge, VFace>(new LengthAdapter(l), new ConstantWeight(1.0));
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
	
}
