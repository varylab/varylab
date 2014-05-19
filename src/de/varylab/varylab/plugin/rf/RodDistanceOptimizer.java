package de.varylab.varylab.plugin.rf;


import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.ConstantLengthAdapter;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;
import de.varylab.varylab.plugin.optimization.OptimizationPanel;

public class RodDistanceOptimizer extends VarylabOptimizerPlugin {

	private HalfedgeInterface hif = null;

	private SpinnerNumberModel
		rodsModel = new SpinnerNumberModel(0.1, 0.0, 100, 1.0);
	
	private JSpinner
		rodsSpinner = new JSpinner(rodsModel); 
	
	private JPanel
		optionsPanel = new JPanel();
	
	public RodDistanceOptimizer() {
		optionsPanel.add(new JLabel("Rod thickness"));
		optionsPanel.add(rodsSpinner);
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		RodConnectivityAdapter rca = hif.getAdapters().query(RodConnectivityAdapter.class);
		if(rca == null) {
			return null;
		} else {
			return new RodDistanceFunctional(rca, new ConstantLengthAdapter(rodsModel.getNumber().doubleValue()));
		}
	}

	@Override
	public String getName() {
		return "Rod distance";
	}
	
	@Override
	public void install(Controller c) throws Exception {
		hif = c.getPlugin(HalfedgeInterface.class);
		c.getPlugin(OptimizationPanel.class);
		super.install(c);
	}

	@Override
	public JPanel getOptionPanel() {
		return optionsPanel;
	}
	
}
