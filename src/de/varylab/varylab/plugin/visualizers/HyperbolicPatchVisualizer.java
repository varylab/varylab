package de.varylab.varylab.plugin.visualizers;

import hyperbolicnets.core.DataModel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.converter.UVMeshGenerator;

public class HyperbolicPatchVisualizer extends VisualizerPlugin implements ActionListener {

	HalfedgeInterface 
		hif = null;
	
	private JPanel
		panel = new JPanel();
	
	private JButton
		updateButton = new JButton("Update");
	
	private SpinnerNumberModel
		resolutionModel = new SpinnerNumberModel(11,2,51,1),
		parameterModel = new SpinnerNumberModel(0.5,0,1,0.05);
	
	private JSpinner
		resolutionSpinner = new JSpinner(resolutionModel),
		parameterSpinner = new JSpinner(parameterModel);

	private int resolution = 11;

	private double parameter = 0.5;

	public HyperbolicPatchVisualizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = 1;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		
		
		panel.add(new JLabel("Resolution"), gbc1);
		panel.add(resolutionSpinner, gbc2);
		panel.add(new JLabel("Parameter"), gbc1);
		panel.add(parameterSpinner, gbc2);
		updateButton.addActionListener(this);
		panel.add(updateButton,gbc2);
	}
	
	@Override
	public String getName() {
		return "Hyperbolic Patch Visualizer";
	}

	@Override
	public void install(Controller c) throws Exception {
		hif = c.getPlugin(HalfedgeInterface.class);
		super.install(c);
	}
	
	@Override
	public void updateContent() {
		manager.updateContent();
	}

	@Override
	public SceneGraphComponent getComponent() {
		VHDS hds = new VHDS();
		hds = hif.get(hds);
		UVMeshGenerator uvGenerator = new UVMeshGenerator(hds);
		double[][][][] xyzcoord = uvGenerator.getArray();
		DataModel patchModel = new DataModel();
		patchModel.setResolution(resolution);
		DataModel.setVertices(xyzcoord);
		patchModel.initPatches(parameter);
		return patchModel.getGeometricObjects();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		resolution = resolutionModel.getNumber().intValue();
		parameter = parameterModel.getNumber().doubleValue();
		manager.updateContent();
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
}
