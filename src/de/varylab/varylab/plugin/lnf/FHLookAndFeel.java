package de.varylab.varylab.plugin.lnf;

import de.jtem.jrworkspace.plugin.lnfswitch.LookAndFeelPlugin;

public class FHLookAndFeel extends LookAndFeelPlugin {

	@Override
	public String getLnFClassName() {
		return "com.shfarr.ui.plaf.fh.FhLookAndFeel";
	}

	@Override
	public String getLnFName() {
		return "FH Look And Feel";
	}

	@Override
	public boolean isSupported() {
		try {
			Class.forName("com.shfarr.ui.plaf.fh.FhLookAndFeel");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

}
