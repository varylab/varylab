package de.varylab.varylab.plugin.ui;

import javax.swing.JComponent;

import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public abstract class WidgetPlugin extends Plugin {

	private VarylabCustomGUI
		customGUI = null;
	
	public abstract JComponent getWidgetComponent();
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		customGUI = c.getPlugin(VarylabCustomGUI.class);
		customGUI.addWidget(this);
	}
	
}
