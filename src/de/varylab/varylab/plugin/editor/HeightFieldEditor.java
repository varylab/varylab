package de.varylab.varylab.plugin.editor;

import static java.lang.Math.cosh;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.plugin.basic.Content;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.EditorPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class HeightFieldEditor extends EditorPlugin {

	private JPanel
		panel = new JPanel();
	private SpinnerNumberModel
		aModel = new SpinnerNumberModel(1.2, 0.001, 1000.0, 0.01);
	private JSpinner
		aSpinner = new JSpinner(aModel);
	private ButtonGroup
		buttons = new ButtonGroup();
	private JRadioButton
		coshButton = new JRadioButton("Cosh"),
		knickButton = new JRadioButton("Knick");
	
	public HeightFieldEditor() {
		coshButton.setSelected(true);
		buttons.add(coshButton);
		buttons.add(knickButton);
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = GridBagConstraints.RELATIVE;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		panel.add(coshButton, gbc1);
		panel.add(aSpinner, gbc2);
		panel.add(knickButton,gbc2);
	}
	
	
	@Override
	protected void edit(
		Content content,
		HalfedgeInterface hif
	) {
		double a = aModel.getNumber().doubleValue();
		VHDS hds = hif.get(new VHDS(), new AdapterSet());
		for (VVertex v : hds.getVertices()) {
			if (v.position == null) continue;
			double x = v.position[0];
			double y = v.position[1];
			double z = 0.0;
			if(coshButton.isSelected()) {
				z = f(2*(x - 0.5), a) * f(2*(y - 0.5), a);
				
			}
			if(knickButton.isSelected()) {
				z = (x+y>=0)?x+y:-x-y;
			}
			v.position[2] = z;
		}
		hif.set(hds, new AdapterSet());
	}
	
	private double f(double x, double a) {
		return a * (cosh(1/a) - cosh(x/a)); 
	}
	

	@Override
	protected String[] getMenuPath() {
		return new String[] {};
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Hightfield Editor", "Stefan Sechelmann");
		info.icon = ImageHook.getIcon("hfe.png", 16, 16);
		return info;
	}

}
