package de.varylab.varylab.plugin.ui;

import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.hds.VHDS;

public class VarylabMain extends Plugin {

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		HalfedgeInterface hif = c.getPlugin(HalfedgeInterface.class);
		hif.setTemplateHDS(new VHDS());
		hif.set(new VHDS());
	}
	
}
