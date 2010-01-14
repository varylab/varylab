package de.varylab.varylab.plugin.meshoptimizer;

import de.jreality.math.Pn;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.planarfaces.VolumeFuctional;
import de.jtem.halfedgetools.functional.planarfaces.PlanarFacesAdapters.VolumeWeight;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.ui.image.ImageHook;

public class PlanarQuadsOptimizer extends OptimizerPlugin {

	
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
	public Functional<VVertex, VEdge, VFace> createFunctional(VHDS hds) {
		double scale = getShortestEdgeLength(hds);
		return new VolumeFuctional<VVertex, VEdge, VFace>(new ConstantWeight(1.0), scale, 1.0);
	}
	
	public static double getShortestEdgeLength(VHDS hds) {
		double r = Double.MAX_VALUE;
		for (VEdge e : hds.getEdges()) {
			double tmp = Pn.distanceBetween(e.getStartVertex().position, e.getTargetVertex().position, Pn.EUCLIDEAN);
			if (tmp < r) {
				r = tmp;
			}
		}
		return r;
	}
	

	@Override
	public String getName() {
		return "Planar Quads";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Planar Quads Optimizer", "Stefan Sechelmann");
		info.icon = ImageHook.getIcon("combinatorics.png");
		return info;
	}

}
