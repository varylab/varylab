package de.varylab.varylab.plugin.ui;

import de.jreality.plugin.menu.BackgroundColor;
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
		String color = c.getProperty(BackgroundColor.class, "color", "");
		if (color.equals("")) {
			BackgroundColor bgColorPlugin = c.getPlugin(BackgroundColor.class);
			bgColorPlugin.setColor("UI Background");
		}
	}
	
	
}
