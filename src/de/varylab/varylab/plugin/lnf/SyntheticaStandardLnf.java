package de.varylab.varylab.plugin.lnf;

import de.jtem.jrworkspace.plugin.lnfswitch.LookAndFeelPlugin;

public class SyntheticaStandardLnf extends LookAndFeelPlugin {

	@Override
	public String getLnFClassName() {
		return "de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel";
	}

	@Override
	public String getLnFName() {
		return "Synthetica Standard L&F";
	}


	@Override
	public boolean isSupported() {
		try {
			Class.forName("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

}
