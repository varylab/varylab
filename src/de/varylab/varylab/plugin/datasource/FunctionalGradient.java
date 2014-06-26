package de.varylab.varylab.plugin.datasource;

import java.util.List;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jpetsc.Vec;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.optimization.VaryLabFunctional;
import de.varylab.varylab.optimization.constraint.Constraint;
import de.varylab.varylab.optimization.tao.TaoGradient;
import de.varylab.varylab.optimization.util.PetscTaoUtility;
import de.varylab.varylab.plugin.optimization.OptimizationPanel;

public class FunctionalGradient extends Plugin implements DataSourceProvider {

	private OptimizationPanel 
		optimizationPanel = null;
	private HalfedgeInterface 
		hif = null;
	
	private class GradientAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {
		
		private Gradient G = null;
		
		public GradientAdapter() {
			super(VVertex.class, null, null, double[].class, true, false);
			
		}
		
		private void initGradient() {
			VHDS hds = hif.get(new VHDS());
			VaryLabFunctional functional = optimizationPanel.createFunctional(hds);
			PetscTaoUtility.initializePetscTao();
			functional.initializeTaoVectors(hds);
			DomainValue x = optimizationPanel.createPositionValue(hds);
			G = new TaoGradient(new Vec(hds.numVertices()*3));
			functional.evaluate(hds, x, null, G, null);
			List<Constraint> constraints = optimizationPanel.createConstraints(hds);
			for(Constraint c : constraints) {
				c.editGradient(hds, hds.numVertices()*3, x, G);
			}
		}
		
		@Override
		public double[] getVertexValue(VVertex v, AdapterSet a) {
			if(G == null) {
				initGradient();
			}
			int off = v.getIndex()*3;
			double[] gradv = new double[]{G.get(off), G.get(off+1), G.get(off+2)};
			return gradv;
		}
		
		@Override
		public String toString() {
			return "Functional Gradient";
		}
		
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		optimizationPanel = c.getPlugin(OptimizationPanel.class);
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(new GradientAdapter());
	}



}
