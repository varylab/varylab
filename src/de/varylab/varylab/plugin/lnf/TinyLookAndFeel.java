package de.varylab.varylab.plugin.lnf;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.sf.tinylaf.controlpanel.ControlPanel;
import de.jtem.jrworkspace.plugin.lnfswitch.LookAndFeelPlugin;

public class TinyLookAndFeel extends LookAndFeelPlugin implements ActionListener {

	private JPanel
		panel = new JPanel();
	private JButton
		openControlsButton = new JButton("Properties...");
	
	public TinyLookAndFeel() {
		panel.setLayout(new FlowLayout());
		panel.add(openControlsButton);
		openControlsButton.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		ControlPanel.main(new String[] {});
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	@Override
	public String getLnFClassName() {
		return "net.sf.tinylaf.TinyLookAndFeel";
	}

	@Override
	public String getLnFName() {
		return "Tiny Look And Feel";
	}

	@Override
	public boolean isSupported() {
		try {
			Class.forName("net.sf.tinylaf.TinyLookAndFeel");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

}
