package de.varylab.varylab;

import de.jreality.plugin.JRViewer;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.VertexPositionAdapter;
import de.varylab.varylab.plugin.editor.HeightFieldEditor;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;

public class VaryLab {

	private static void addVaryLabPlugins(JRViewer v) {
		v.registerPlugin(new HalfedgeInterfacePlugin<VVertex, VEdge, VFace, VHDS>(VHDS.class, new VertexPositionAdapter()));
		v.registerPlugin(new QuadMeshGenerator());
		v.registerPlugin(new HeightFieldEditor());
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
