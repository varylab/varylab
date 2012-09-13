package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.jreality.math.Pn;
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
import de.varylab.varylab.math.functional.DiagonalDistanceFunctional;
import de.varylab.varylab.math.functional.PlanarFacesAdapters.VolumeWeight;
import de.varylab.varylab.math.functional.VolumeFunctional;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class PlanarQuadsOptimizer extends VarylabOptimizerPlugin {

	private VolumeFunctional<VVertex, VEdge, VFace>
		volFunctional = new VolumeFunctional<VVertex, VEdge, VFace>(new ConstantWeight(1.0), 1, 1.0);
	
	private DiagonalDistanceFunctional<VVertex, VEdge, VFace>
		diagFunctional = new DiagonalDistanceFunctional<VVertex, VEdge, VFace>(new ConstantWeight(1.0),1.0);

	private JPanel panel = new JPanel(); 
	
	private JRadioButton
		volButton = new JRadioButton("Volume", false),
		diagButton = new JRadioButton("Diagonal Distance", true);
	
	private ButtonGroup
		functionalButtonGroup = new ButtonGroup();

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
	
	public PlanarQuadsOptimizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(2, 2, 2, 2);
		
		functionalButtonGroup.add(volButton);
		functionalButtonGroup.add(diagButton);
		
		panel.add(volButton,gbc);
		panel.add(diagButton,gbc);
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		double scale = getShortestEdgeLength(hds);
		volFunctional.setScale(scale);
		diagFunctional.setScale(scale);
		AdapterSet aSet = hif.getAdapters();
		volFunctional.setWeight(new AdaptedFaceWeightFunction(aSet));
		diagFunctional.setWeight(new AdaptedFaceWeightFunction(aSet));
		if(volButton.isSelected()) {
			return volFunctional;
		} else if(diagButton.isSelected()) {
			return diagFunctional;
		} else { // this should not happen!!!
			return volFunctional;
		}
			
	}
	
	public static double getShortestEdgeLength(VHDS hds) {
		double r = Double.MAX_VALUE;
		for (VEdge e : hds.getEdges()) {
			double tmp = Pn.distanceBetween(e.getStartVertex().P, e.getTargetVertex().P, Pn.EUCLIDEAN);
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

	@Override
	public JPanel getOptionPanel() {
		return panel ;
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "useVolumeFunctional", volButton.isSelected());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		volButton.setSelected(c.getProperty(getClass(), "useVolumeFunctional", false));
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		c.getPlugin(HalfedgeInterface.class);
		JRViewerUtility.getContentPlugin(c);
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
	}
	
}
