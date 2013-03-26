package de.varylab.varylab.plugin.meshoptimizer;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.CircumcircleCurvatureFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.CoordinateArrayAdapter;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.optimization.IterationProtocol;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;
import de.varylab.varylab.plugin.datasource.GeodesicCircumcircleCurvature;

public class CircumcircleCurvatureOptimizer extends VarylabOptimizerPlugin {

	private HalfedgeInterface	
		hif = null;
	private GeodesicCircumcircleCurvature
		curvatureDatasource = null;
	private CircumcircleCurvatureFunctional<VVertex, VEdge, VFace>
		functional = new CircumcircleCurvatureFunctional<VVertex, VEdge, VFace>();
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		functional.setAdapters(hif.getAdapters());
		return functional;
	}

	@Override
	public String getName() {
		return "Circumcircle Curvature";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Circumcircle Curvature Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("angle.png");
		return info;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		curvatureDatasource = c.getPlugin(GeodesicCircumcircleCurvature.class);
	}
	
	@Override
	public IterationProtocol getIterationProtocol(double[] solution, VHDS hds) {
		IterationProtocol p = super.getIterationProtocol(solution, hds);
		CoordinateArrayAdapter coords = new CoordinateArrayAdapter(solution, 1.0);
		AdapterSet aSet = AdapterSet.createGenericAdapters();
		aSet.add(coords);
		aSet.addAll(curvatureDatasource.getDataSources());
//		for (VEdge e : hds.getEdges()) {
//			aSet.get(GeodesicCircumcircleCurvatureAdapter.class, null, null, null)
//		}
		return p;
	}


}
