package de.varylab.varylab.plugin.lnf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.skin.SkinInfo;

import de.jtem.jrworkspace.plugin.lnfswitch.LookAndFeelPlugin;

public class SubstanceLnF extends LookAndFeelPlugin implements ActionListener {

	private JComboBox
		skinCombo = new JComboBox();
	
	
	public SubstanceLnF() {
		skinCombo.addActionListener(this);
	}
	
	
	@Override
	public String getLnFClassName() {
		return "org.jvnet.substance.skin.SubstanceRavenGraphiteGlassLookAndFeel";
	}

	@Override
	public String getLnFName() {
		return "Substance Look and Feel";
	}

	@Override
	public boolean isSupported() {
		try {
			Class.forName("org.jvnet.substance.skin.SubstanceRavenGraphiteGlassLookAndFeel");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}
	
	
//	@Override
//	public JPanel getOptionPanel() {
//		Map<String, SkinInfo> skins = SubstanceLookAndFeel.getAllSkins();
//		for (String skin : skins.keySet()) {
//			SkinInfo info = skins.get(skin);
//			skinCombo.addItem(info.getDisplayName());
//		}
//		skinCombo.setSelectedIndex(0);
//		JPanel optionsPanel = new JPanel();
//		optionsPanel.add(skinCombo);
//		return optionsPanel;
//	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object nameObject = skinCombo.getSelectedItem();
		if (nameObject != null) {
			String name = (String)nameObject;
			SkinInfo info = SubstanceLookAndFeel.getAllSkins().get(name);
			SubstanceLookAndFeel.setSkin(info.getClassName());
		}
	}
	
}
