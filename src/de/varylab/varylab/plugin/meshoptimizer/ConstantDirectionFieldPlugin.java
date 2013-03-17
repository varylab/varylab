package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.plugin.JRViewerUtility;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.ConstantDirectionFieldFunctional;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;
import de.varylab.varylab.plugin.image.ImageHook;

public class ConstantDirectionFieldPlugin extends VarylabOptimizerPlugin implements ChangeListener, ActionListener{
	
	private JPanel
		panel = new JPanel();
	
	private SpinnerNumberModel
		strengthModel = new SpinnerNumberModel(9.81,0,100,1.0);
	private JSpinner
		strengthSpinner = new JSpinner(strengthModel);
	
	private NumberFormat 
		coordFormat = NumberFormat.getNumberInstance();
	
	private JFormattedTextField
		dirX = new JFormattedTextField(coordFormat),
		dirY = new JFormattedTextField(coordFormat),
		dirZ = new JFormattedTextField(coordFormat);

	private double
		scale = 1E-3;
	
	private ConstantDirectionFieldFunctional<VVertex, VEdge, VFace>
		functional = new ConstantDirectionFieldFunctional<VVertex, VEdge, VFace>(
			9.81*scale,
			new double[]{0.0,0.0,1.0});

	
	
	public ConstantDirectionFieldPlugin() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.0;
		
		gbc.insets = new Insets(2, 2, 2, 2);
		panel.setLayout(new GridBagLayout());
		
		gbc.gridwidth = 3;
		panel.add(new JLabel("Strength"),gbc);
		gbc.weightx = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(strengthSpinner,gbc);

		gbc.weightx = 0.0;
		gbc.gridwidth = 3;
		panel.add(new JLabel("Direction"),gbc);
		
		gbc.weightx = 1.0;
		dirX.setColumns(4);
		dirX.setText("0.0");
		panel.add(dirX,gbc);
		
		dirY.setText("1.0");
		dirY.setColumns(4);
		panel.add(dirY,gbc);

		dirZ.setText("0.0");
		dirZ.setColumns(4);
		panel.add(dirZ,gbc);
		
		
		strengthSpinner.addChangeListener(this);
		dirX.addActionListener(this);
		dirY.addActionListener(this);
		dirZ.addActionListener(this);
		
		
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}

	@Override
	public String getName() {
		return "Direction Field";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Constant Direction Field Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("direction.png");
		return info;
	}

	@Override
	public JPanel getOptionPanel() {
		return panel;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if(strengthSpinner == src) {
			functional.setStrength(strengthModel.getNumber().doubleValue()*scale);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(dirX == src || dirY == src || dirZ == src) {
			functional.setDirection(new double[]{
					Double.parseDouble(dirX.getText()),
					Double.parseDouble(dirY.getText()),
					Double.parseDouble(dirZ.getText())});
		} 
	}
	
	@Override
	public void install(Controller c) throws Exception {
		c.getPlugin(HalfedgeInterface.class);
		JRViewerUtility.getContentPlugin(c);
		super.install(c);
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "strength", strengthModel.getNumber().doubleValue());
		c.storeProperty(getClass(), "dirX", dirX.getText());
		c.storeProperty(getClass(), "dirY", dirY.getText());
		c.storeProperty(getClass(), "dirZ", dirZ.getText());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		strengthModel.setValue(c.getProperty(getClass(), "strength", strengthModel.getNumber().doubleValue()));
		dirX.setText(c.getProperty(getClass(), "dirX", dirX.getText()));
		dirY.setText(c.getProperty(getClass(), "dirY", dirY.getText()));
		dirZ.setText(c.getProperty(getClass(), "dirZ", dirZ.getText()));
		functional.setDirection(new double[]{
				Double.parseDouble(dirX.getText()),
				Double.parseDouble(dirY.getText()),
				Double.parseDouble(dirZ.getText())});
	}
}
