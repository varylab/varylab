package de.varylab.varylab;

import de.jreality.plugin.JRViewer;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.plugin.buildin.topology.TopologyOperations;
import de.jtem.halfedgetools.symmetry.adapters.DebugBundleAdapter;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.VertexPositionAdapter;
import de.varylab.varylab.plugin.editor.HeightFieldEditor;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.meshoptimizer.EdgeLengthOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.OptimizationManager;
import de.varylab.varylab.plugin.meshoptimizer.PlanarQuadsOptimizer;

public class VaryLab {

	private static void addVaryLabPlugins(JRViewer v) {
		v.registerPlugin(new HalfedgeInterfacePlugin<VVertex, VEdge, VFace, VHDS>(
				VHDS.class, 
				new VertexPositionAdapter(),
				new DebugBundleAdapter(AdapterType.EDGE_ADAPTER)));
		v.registerPlugins(TopologyOperations.topologicalEditingJR(new VVertex()));
		v.registerPlugin(new QuadMeshGenerator());
		v.registerPlugin(new HeightFieldEditor());
		v.registerPlugin(new OptimizationManager());
		v.registerPlugin(new EdgeLengthOptimizer());
		v.registerPlugin(new PlanarQuadsOptimizer());
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
