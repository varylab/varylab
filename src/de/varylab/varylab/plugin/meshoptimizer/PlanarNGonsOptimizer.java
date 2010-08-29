package de.varylab.varylab.plugin.meshoptimizer;

import de.jreality.plugin.JRViewerUtility;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.AdaptedFaceWeightFunction;
import de.varylab.varylab.math.functional.PlanarFacesAdapters.VolumeWeight;
import de.varylab.varylab.math.functional.PlanarNgonsFunctional;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class PlanarNGonsOptimizer extends OptimizerPlugin {

	private PlanarNgonsFunctional<VVertex, VEdge, VFace>
		functional = new PlanarNgonsFunctional<VVertex, VEdge, VFace>(new ConstantWeight(1.0), 1, 1.0);
	private HalfedgeInterface 
		hif = null;
	
	
	public class ConstantWeight implements VolumeWeight<VFace> {
		private double 
			a = 1.0;
		
		public ConstantWeight(double a) {
			this.a = a;
		}

		@Override
		public Double getWeight(VFace f) {
			return a;
		}
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		AdapterSet aSet = hif.getAdapters();
		functional.setWeight(new AdaptedFaceWeightFunction(aSet));
		return functional;
	}
	

	@Override
	public String getName() {
		return "Planar N-Gons";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Planar N-Gons Optimizer", "Stefan Sechelmann");
		info.icon = ImageHook.getIcon("combinatorics.png");
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
