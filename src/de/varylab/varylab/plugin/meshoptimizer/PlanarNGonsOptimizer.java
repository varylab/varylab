package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.PlanarFacesAdapters.VolumeWeight;
import de.varylab.varylab.math.functional.PlanarNgonsFunctional;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class PlanarNGonsOptimizer extends OptimizerPlugin {

	private PlanarNgonsFunctional<VVertex, VEdge, VFace>
		functional = new PlanarNgonsFunctional<VVertex, VEdge, VFace>(new ConstantWeight(1.0), 1, 1.0);
	
	
	public class ConstantWeight implements VolumeWeight<VFace> {
		private double 
			a = 1.0;
		
		public ConstantWeight(double a) {
			this.a = a;
		}

		@Override
		public double getWeight(VFace f) {
			return a;
		}
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
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

}
