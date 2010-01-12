package de.varylab.varylab;

import de.jreality.plugin.JRViewer;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;

public class VaryLab {

	private static void addVaryLabPlugins(JRViewer v) {
		v.registerPlugin(new QuadMeshGenerator());
	}
	
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.setPropertiesResource(VaryLab.class, "VaryLab.xml");
		v.setShowPanelSlots(true, true, true, true);
		v.setShowToolBar(true);
		v.setShowMenuBar(true);
		v.addBasicUI();
		v.addContentUI();
		addVaryLabPlugins(v);
		v.startup();
	}

}
