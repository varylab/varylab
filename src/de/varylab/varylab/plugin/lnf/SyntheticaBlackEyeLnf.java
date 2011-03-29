package de.varylab.varylab.plugin.lnf;

import de.jtem.jrworkspace.plugin.lnfswitch.LookAndFeelPlugin;

public class SyntheticaBlackEyeLnf extends LookAndFeelPlugin {

	@Override
	public String getLnFClassName() {
		return "de.javasoft.plaf.synthetica.SyntheticaBlackEyeLookAndFeel";
	}

	@Override
	public String getLnFName() {
		return "Synthetica BlackEye L&F";
	}


	@Override
	public boolean isSupported() {
		try {
			Class.forName("de.javasoft.plaf.synthetica.SyntheticaBlackEyeLookAndFeel");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

}
