package de.varylab.varylab.plugin.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class AngleCalculatorPlugin extends ShrinkPanelPlugin implements ActionListener {

	private JPanel
		panel = new JPanel();
	
	private JTextField
		fractionInput = new JTextField(5),
		fractionOutput = new JTextField(15),
		tanInput = new JTextField(5),
		tanOutput = new JTextField(15);
	
	public AngleCalculatorPlugin() {
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
		
		
		fractionInput.addActionListener(this);
		fractionOutput.setEditable(false);
//		fractionInput.setMinimumSize(new Dimension(40,10));
//		fractionOutput.setMinimumSize(new Dimension(160,10));
		panel.add(fractionInput,gbc1);
		panel.add(new JLabel("* pi ="),gbc1);
		panel.add(fractionOutput,gbc2);
		
		tanInput.addActionListener(this);
		tanOutput.setEditable(false);
		tanInput.setMinimumSize(new Dimension(40,10));
		tanOutput.setMinimumSize(new Dimension(160,10));
		panel.add(new JLabel("tan^(-1)("),gbc1);
		panel.add(tanInput,gbc1);
		panel.add(new JLabel(") = "),gbc1);
		panel.add(tanOutput,gbc2);
		shrinkPanel.add(panel);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			Object src = e.getSource();
			if(fractionInput == src) {
				StringTokenizer st = new StringTokenizer(fractionInput.getText(),"/");
				double numerator = Double.parseDouble(st.nextToken());
				double denominator = 1.0;
				if(st.hasMoreTokens()) {
					denominator = Double.parseDouble(st.nextToken());
				}
				double fraction = numerator/denominator;
				fractionOutput.setText(""+fraction*Math.PI);
			}
			if(tanInput == src) {
				StringTokenizer st = new StringTokenizer(tanInput.getText(),"/");
				double numerator = Double.parseDouble(st.nextToken());
				double denominator = 1.0;
				if(st.hasMoreTokens()) {
					denominator = Double.parseDouble(st.nextToken());
				}
				double fraction = numerator/denominator;
				fractionOutput.setText(""+Math.atan(fraction));
			}
		} catch(NumberFormatException ex) {	
			fractionInput.setText("");
			fractionOutput.setText(":-p");
			tanInput.setText("");
			tanOutput.setText(":-p");
		}
			
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Angle calculator","Thilo Roerig");
		return info;
	}
}
