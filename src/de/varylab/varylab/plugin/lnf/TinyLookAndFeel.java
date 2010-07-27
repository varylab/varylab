package de.varylab.varylab.plugin.lnf;

import de.jtem.jrworkspace.plugin.lnfswitch.LookAndFeelPlugin;

public class TinyLookAndFeel extends LookAndFeelPlugin {

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
